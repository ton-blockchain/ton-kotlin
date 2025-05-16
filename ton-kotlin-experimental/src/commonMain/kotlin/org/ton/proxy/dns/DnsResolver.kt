package org.ton.proxy.dns

import io.github.reactivecircus.cache4k.Cache
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import org.ton.kotlin.adnl.tonnode.TonNodeBlockIdExt
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.block.*
import org.ton.kotlin.boc.BagOfCells
import org.ton.kotlin.cell.Cell
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.crypto.hex
import org.ton.kotlin.hashmap.HashMapEdge
import org.ton.kotlin.lite.api.liteserver.LiteServerAccountId
import org.ton.kotlin.lite.client.LiteClient
import org.ton.logger.PrintLnLogger
import org.ton.kotlin.tlb.constructor.tlbCodec
import org.ton.kotlin.tlb.parse
import kotlin.coroutines.CoroutineContext
import kotlin.jvm.JvmStatic
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class DnsResolver constructor(
    val liteClient: LiteClient,
    resolverAddress: LiteServerAccountId?,
    cacheTtl: Duration = 5.minutes
) : CoroutineScope {
    override val coroutineContext: CoroutineContext =
        DISPATCHER + CoroutineName("DnsResolver: $resolverAddress")
    private val logger = PrintLnLogger("DnsResolver")

    constructor(liteClient: LiteClient) : this(liteClient, null, 24.hours)

    private val dnsCache = Cache.Builder()
        .expireAfterWrite(cacheTtl)
        .build<String, Deferred<Map<BitString, DnsRecord>?>>()
    private val dnsResolversCache = Cache.Builder()
        .expireAfterWrite(cacheTtl)
        .build<LiteServerAccountId, DnsResolver>()
    private val blockIdCache = Cache.Builder()
        .expireAfterWrite(60.seconds)
        .build<Unit, TonNodeBlockIdExt>()
    private val resolverAddress by lazy {
        resolverAddress?.let { CompletableDeferred(it) } ?: async {
            val rootResolver = getRootResolver()
            logger.info { "Root resolver: $rootResolver" }
            rootResolver
        }
    }

    suspend fun resolveAll(host: String, block: TonNodeBlockIdExt? = null): Map<DnsCategory, DnsRecord> =
        DnsCategory.values().map { category ->
            coroutineScope {
                async {
                    resolve(
                        host,
                        category,
                        block
                    )?.let { category to it }
                }
            }
        }.awaitAll().asSequence().filterNotNull().toMap()

    suspend fun resolve(host: String, category: DnsCategory, block: TonNodeBlockIdExt? = null): DnsRecord? {
        val encodedHost = encodeHostname(host)
        val currentBlock = getOrUpdateBlock(block)
        return resolveCache(encodedHost, category, currentBlock)
    }

    private suspend fun resolveCache(encodedHost: String, category: DnsCategory, block: TonNodeBlockIdExt): DnsRecord? {
        val domains = encodedHost.splitToSequence(DNS_NAME_DELIMITER).map { it + DNS_NAME_DELIMITER }.toMutableList()
        val first = domains.removeFirst()
        val records = dnsCache.get(first) {
            coroutineScope {
                async(coroutineContext) {
                    resolveOrNull(first, block)
                }
            }
        }.await()
        if (records == null) return null
        else {
            val nextResolverAddress = (records[DNS_NEXT_RESOLVER_ID] as? DnsNextResolver)?.let {
                it.resolver as? AddrStd
            }?.let {
                LiteServerAccountId(it)
            }
            return if (nextResolverAddress != null) {
                val dnsResolver = dnsResolversCache.get(nextResolverAddress) {
                    DnsResolver(liteClient, nextResolverAddress)
                }
                dnsResolver.resolveCache(domains.joinToString(""), category, block)
            } else {
                records[BitString(category.value.toByteArray())]
            }
        }
    }

    private suspend fun resolveOrNull(encodedHost: String, block: TonNodeBlockIdExt): Map<BitString, DnsRecord>? {
        val vmStack = liteClient.runSmcMethod(
            resolverAddress.await(),
            block,
            "dnsresolve",
            VmStackValue(CellBuilder.createCell {
                storeBytes(encodedHost.toByteArray())
            }.beginParse()),
            VmStackValue(0)
        ).toMutableVmStack()
        vmStack.popTinyInt()
        val result = vmStack.popCell()

        return try {
            val dnsRecord = result.parse(DnsRecord) as DnsNextResolver
            mapOf(DNS_NEXT_RESOLVER_ID to dnsRecord)
        } catch (e: Exception) {
            result.parse(DNS_RECORDS_CODEC).nodes().toMap()
        }
    }

    private suspend fun getRootResolver(): LiteServerAccountId {
        val blockId = getOrUpdateBlock()
        val configInfo = liteClient.liteApi.getConfigAll(0, blockId)

        // TODO: support TL-B lazy load with pruned cells
        var cell = BagOfCells(configInfo.config_proof).first().beginParse().loadRef().beginParse()
        cell.loadRef() // skip pruned cell out_msg_queue_info:^OutMsgQueueInfo
        cell.loadRef() // skip pruned cell accounts:^ShardAccounts
        cell.loadRef() // skip pruned cell ^[overload_history:uint64 underload_history:uint64 total_balance:CurrencyCollection total_validator_fees:CurrencyCollection libraries:(HashmapE 256 LibDescr) master_ref:(Maybe BlkMasterInfo) ]
        cell = cell.loadRef().beginParse() // load cell custom:(Maybe ^McStateExtra)
        cell.loadRef() // skip shard_hashes:ShardHashes
        val config = cell.loadRef().parse(HashMapEdge.tlbCodec(32, Cell.tlbCodec())) // load config:^(Hashmap 32 ^Cell)
        val (_, rawAddress) = config.nodes().find { (key, _) ->
            CellSlice(key).loadUInt32().toInt() == 4
        } ?: throw IllegalStateException("No dns resolver address found")
        return LiteServerAccountId(-1, rawAddress.beginParse().loadBits(256).toByteArray())
    }

    private suspend fun getOrUpdateBlock(block: TonNodeBlockIdExt? = null) = block ?: blockIdCache.get(Unit) {
        liteClient.getLastBlockId()
    }

    companion object {
        private const val MAX_DEFAULT_MAX_NAME_SIZE = 128
        private const val DNS_NAME_DELIMITER = 0.toChar().toString()
        private val DNS_NEXT_RESOLVER_ID = DnsNextResolver.tlbConstructor().id
        private val MAINNET_ADDRESS =
            LiteServerAccountId(-1, hex("8E25DD08174C9CD3192181A022AEAD7659AC2A9D4A12B84F5160EDD2ECC706EA"))
        private val DNS_RECORDS_CODEC = HashMapEdge.tlbCodec(256, Cell.tlbCodec(DnsRecord))
        private val DISPATCHER = newSingleThreadContext("dns-resolver")

        @JvmStatic
        fun encodeHostname(host: String?): String {
            if (host.isNullOrEmpty() || host == ".") {
                return DNS_NAME_DELIMITER
            }
            val delimiter = 0.toChar().toString()
            return host.lowercase().split('.').asReversed().joinToString(DNS_NAME_DELIMITER, postfix = delimiter)
        }

        @JvmStatic
        fun decodeHostname(host: String): String {
            return host.split(DNS_NAME_DELIMITER).asReversed().joinToString(".")
        }
    }
}
