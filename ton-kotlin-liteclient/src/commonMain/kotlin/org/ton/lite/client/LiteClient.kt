@file:OptIn(ExperimentalTime::class)

package org.ton.lite.client

import io.ktor.utils.io.core.*
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.ton.adnl.connection.AdnlClientImpl
import org.ton.adnl.network.IPAddress.*
import org.ton.api.exception.TonNotReadyException
import org.ton.api.exception.TvmException
import org.ton.api.liteclient.config.LiteClientConfigGlobal
import org.ton.api.liteserver.LiteServerDesc
import org.ton.api.tonnode.*
import org.ton.bitstring.toBitString
import org.ton.block.*
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellType
import org.ton.crypto.crc16
import org.ton.crypto.sha256
import org.ton.lite.api.LiteApiClient
import org.ton.lite.api.liteserver.*
import org.ton.lite.api.liteserver.functions.*
import org.ton.logger.Logger
import org.ton.logger.PrintLnLogger
import org.ton.tl.Bits256
import org.ton.tlb.CellRef
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb
import kotlin.coroutines.CoroutineContext
import kotlin.math.max
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

private const val BLOCK_ID_CACHE_SIZE = 100

public class LiteClient(
    coroutineContext: CoroutineContext,
    private val liteClientConfigGlobal: LiteClientConfigGlobal
) : Closeable, CoroutineScope {
    public constructor(
        coroutineContext: CoroutineContext,
        liteServers: List<LiteServerDesc>
    ) : this(coroutineContext, LiteClientConfigGlobal(liteServers = liteServers))

    public constructor(
        coroutineContext: CoroutineContext,
        vararg liteServer: LiteServerDesc
    ) : this(coroutineContext, liteServer.toList())

    init {
        require(liteClientConfigGlobal.liteServers.isNotEmpty()) { "No lite servers provided" }
    }

    private val logger: Logger = PrintLnLogger("LiteClient")
    override val coroutineContext: CoroutineContext = coroutineContext + CoroutineName("LiteClient")
    private val knownBlockIds: ArrayDeque<TonNodeBlockIdExt> = ArrayDeque(100)
    private var lastMasterchainBlockId: TonNodeBlockIdExt by atomic(
        TonNodeBlockIdExt(
            0, 0, 0
        )
    )
    private var lastMasterchainBlockIdTime: Instant by atomic(Instant.DISTANT_PAST)
    private var zeroStateId: TonNodeZeroStateIdExt by atomic(
        TonNodeZeroStateIdExt(
            liteClientConfigGlobal.validator.zeroState
        )
    )
    private var serverVersion: Int by atomic(0)
    private var serverCapabilities: Long by atomic(0L)
    private var serverTime: Instant by atomic(Clock.System.now())
    private var serverTimeGotAt: Instant by atomic(Clock.System.now())
    private var serverList = liteClientConfigGlobal.liteServers.shuffled()
    private var currentServer: Int = 0

    public val liteApi: LiteApiClient = object : LiteApiClient {
        override suspend fun sendRawQuery(query: ByteReadPacket): ByteReadPacket {
            var attempts = 0
            var exception: Exception? = null
            var liteServer: LiteServerDesc? = null
            val bytes = query.readBytes()
            while (attempts < maxOf(5, serverList.size)) {
                try {
                    liteServer = serverList[currentServer++ % serverList.size]
                    val client = AdnlClientImpl(liteServer)
                    return client.sendQuery(ByteReadPacket(bytes), 10.seconds)
                } catch (e: Exception) {
                    exception = e
                    attempts++
                    delay(100L * attempts)
                }
            }
            throw RuntimeException("Failed to send query to lite server: $liteServer", exception)
        }
    }

    public fun latency(): Duration = serverTimeGotAt - serverTime

    public fun setServerVersion(version: Int, capabilities: Long) {
        if (serverVersion != version || serverCapabilities != capabilities) {
            serverVersion = version
            serverCapabilities = capabilities
            logger.info { "server version is ${version shr 8}.${serverVersion and 0xFF}, capabilities $serverCapabilities" }
        }
    }

    public fun setServerTime(time: Int): Duration {
        serverTime = Instant.fromEpochSeconds(time.toLong())
        serverTimeGotAt = Clock.System.now()
        val latency = latency()
        logger.debug { "server time is $serverTime (latency $latency)" }
        return latency
    }

    public suspend fun getServerTime(): Instant {
        val time = try {
            liteApi.invoke(LiteServerGetTime)
        } catch (e: Exception) {
            throw RuntimeException("Can't get server time", e)
        }
        return Instant.fromEpochSeconds(time.now.toLong())
    }

    public suspend fun getServerVersion(): LiteServerVersion {
        val version = try {
            liteApi.invoke(LiteServerGetVersion)
        } catch (e: Exception) {
            throw RuntimeException("Can't get server version and time", e)
        }
        setServerVersion(version.version, version.capabilities)
        return version
    }

    public suspend fun getLastBlockId(mode: Int = if (serverCapabilities and 2 != 0L) 0 else -1): TonNodeBlockIdExt {
        val last: TonNodeBlockIdExt
        val init: TonNodeZeroStateIdExt
        val ext: LiteServerMasterchainInfoExt?

        if (mode < 0) {
            val masterchainInfo = liteApi.sendQuery(
                LiteServerGetMasterchainInfo,
                LiteServerMasterchainInfo,
                LiteServerGetMasterchainInfo
            )
            last = masterchainInfo.last
            init = masterchainInfo.init
            ext = null
        } else {
            ext = liteApi.sendQuery(
                LiteServerGetMasterchainInfoExt,
                LiteServerMasterchainInfoExt,
                LiteServerGetMasterchainInfoExt(mode)
            )
            last = ext.last
            init = ext.init
        }

        logger.debug { "last masterchain block is $last" }

        var createdAt: Instant? = null
        if (ext != null) {
            setServerVersion(ext.version, ext.capabilities)
            setServerTime(ext.now)
            val serverNow = Instant.fromEpochSeconds(ext.now.toLong())
            val lastUtime = Instant.fromEpochSeconds(ext.lastUtime.toLong())
            createdAt = lastUtime
            if (lastUtime > serverNow) {
                logger.warn {
                    "server claims to have a masterchain block $last created at $lastUtime (${lastUtime - serverNow} in future)"
                }
            } else if (lastUtime < serverNow - 60.seconds) {
                logger.warn {
                    "server appears to be out of sync: its newest masterchain block is $last created at $lastUtime (${serverNow - lastUtime} ago according to the server's clock)"
                }
            } else if (lastUtime < serverTimeGotAt - 60.seconds) {
                logger.warn {
                    "either the server is out of sync, or the local clock is set incorrectly: the newest masterchain block known to server is $last created at $lastUtime (${serverNow - serverTimeGotAt} ago according to the local clock)"
                }
            }
        }

        val currentZeroStateId = zeroStateId
        if (!currentZeroStateId.isValid()) {
            zeroStateId = init
            logger.info { "zero state id set to ${init}" }
        } else if (init != currentZeroStateId) {
            use {
                throw IllegalStateException("masterchain zero state id suddenly changed: expected $zeroStateId, actual $init")
            }
        }
        registerBlockId(last)
        registerBlockId(
            TonNodeBlockIdExt(
                Workchain.MASTERCHAIN_ID, Shard.ID_ALL, 0, zeroStateId.rootHash, zeroStateId.fileHash
            )
        )
        if (!lastMasterchainBlockId.isValid()) {
            lastMasterchainBlockId = last
            lastMasterchainBlockIdTime = Clock.System.now()
        } else if (lastMasterchainBlockId.seqno < last.seqno) {
            lastMasterchainBlockId = last
            lastMasterchainBlockIdTime = Clock.System.now()
        }
        logger.debug {
            "latest masterchain block known to server is:\n$last${
                if (createdAt != null) {
                    "\n  created at $createdAt (${Clock.System.now() - createdAt} ago)"
                } else ""
            }"
        }
        return last
    }

    public suspend fun lookupBlock(blockId: TonNodeBlockId, timeout: Duration): TonNodeBlockIdExt? =
        withTimeoutOrNull(timeout) {
            var result: TonNodeBlockIdExt? = null
            while (isActive && result == null) {
                result = lookupBlock(blockId)
                if (result == null) {
                    delay(1000)
                }
            }
            result
        }

    public suspend fun lookupBlock(
        blockId: TonNodeBlockId,
        lt: Long? = null,
        time: Instant? = null
    ): TonNodeBlockIdExt? {
        if (blockId is TonNodeBlockIdExt) {
            return blockId
        }
        val knownBlockId = knownBlockIds.find { it == blockId }
        if (knownBlockId != null) {
            return knownBlockId
        }
        val blockHeader = try {
            liteApi(LiteServerLookupBlock(blockId, lt, time?.epochSeconds?.toInt()))
        } catch (e: TonNotReadyException) {
            return null
        } catch (e: Exception) {
            throw RuntimeException("Can't lookup block header for $blockId from server", e)
        }
        val actualBlockId = blockHeader.id
        check((!blockId.isValid()) || blockId == actualBlockId) {
            "block id mismatch, expected: $blockId actual: $actualBlockId"
        }
        val blockProofCell = try {
            BagOfCells(blockHeader.headerProof).first()
        } catch (e: Exception) {
            throw IllegalStateException("Can't parse block proof", e)
        }
        val actualRootHash = blockProofCell.refs.firstOrNull()?.hash(level = 0)?.toBitString()
        check(
            blockProofCell.type == CellType.MERKLE_PROOF &&
                    blockHeader.id.rootHash.toBitString() == actualRootHash
        ) {
            "Root hash mismatch:" +
                    "\n expected: ${blockHeader.id.rootHash}" +
                    "\n   actual: $actualRootHash"
        }
        registerBlockId(blockHeader.id)
        return blockHeader.id
    }

    public suspend fun getBlock(blockId: TonNodeBlockIdExt, timeout: Duration): Block? = withTimeoutOrNull(timeout) {
        var result: Block? = null
        while (isActive && result == null) {
            result = getBlock(blockId)
            if (result == null) {
                delay(1000)
            }
        }
        result
    }

    public suspend fun getBlock(blockId: TonNodeBlockIdExt): Block? {
        val blockData = try {
            liteApi(LiteServerGetBlock(blockId))
        } catch (e: TonNotReadyException) {
            return null
        } catch (e: Exception) {
            throw RuntimeException("Can't get block $blockId from server", e)
        }
        val actualFileHash = Bits256(sha256(blockData.data))
        check(blockId.fileHash == actualFileHash) {
            "file hash mismatch for block $blockId, expected: ${blockId.fileHash} , actual: $actualFileHash"
        }
        registerBlockId(blockId)
        val root = try {
            BagOfCells(blockData.data).first()
        } catch (e: Exception) {
            throw RuntimeException("Can't deserialize block data", e)
        }
        val actualRootHash = root.hash().toBitString()
        check(blockId.rootHash.toBitString() == actualRootHash) {
            "block root hash mismatch, expected: ${blockId.rootHash} , actual: $actualRootHash"
        }
        val block = try {
            Block.loadTlb(root.beginParse())
        } catch (e: Exception) {
            throw RuntimeException("Can't parse block: $blockId", e)
        }
        val shard = block.info.shard
        val prevs: List<TonNodeBlockIdExt>
        val prevSeqno: UInt
        if (!block.info.after_merge) {
            val prev1 = block.info.prev_ref.prevs().first()
            prevSeqno = prev1.seq_no
            val prevBlockIdExt = TonNodeBlockIdExt(
                shard.workchain_id,
                (if (block.info.after_split) Shard.shardParent(shard.shard_prefix.toLong()) else shard.shard_prefix.toLong()),
                prev1.seq_no.toInt(),
                Bits256(prev1.root_hash),
                Bits256(prev1.file_hash)
            )
            check(!block.info.after_split || prev1.seq_no != 0u) {
                "shardchains cannot be split immediately after initial state"
            }
            prevs = listOf(prevBlockIdExt)
        } else {
            check(!block.info.after_split) {
                "shardchains cannot be simultaneously split and merged at the same block"
            }
            val (prev1, prev2) = block.info.prev_ref.prevs()
            prevSeqno = max(prev1.seq_no, prev2.seq_no)
            prevs = listOf(
                TonNodeBlockIdExt(
                    shard.workchain_id,
                    Shard.shardChild(shard.shard_prefix.toLong(), true),
                    prev1.seq_no.toInt(),
                    Bits256(prev1.root_hash),
                    Bits256(prev1.file_hash)
                ),
                TonNodeBlockIdExt(
                    shard.workchain_id,
                    Shard.shardChild(shard.shard_prefix.toLong(), false),
                    prev2.seq_no.toInt(),
                    Bits256(prev2.root_hash),
                    Bits256(prev2.file_hash)
                ),
            )
            check(prev1.seq_no != 0u && prev2.seq_no != 0u) {
                "shardchains cannot be merged immediately after initial state"
            }
        }
        check(block.info.seq_no == prevSeqno + 1u) {
            "block $blockId has invalid seqno, expected: ${prevSeqno + 1u} , actual: ${block.info.seq_no}"
        }
        prevs.forEachIndexed { index, id ->
            logger.debug { "previous block #$index : $id" }
        }
        val mcBlockId = if (shard.workchain_id == Workchain.MASTERCHAIN_ID) {
            prevs.first()
        } else {
            val mcRef = checkNotNull(block.info.master_ref?.master) { "missing master block reference" }
            TonNodeBlockIdExt(
                Workchain.MASTERCHAIN_ID,
                Shard.ID_ALL,
                mcRef.seq_no.toInt(),
                Bits256(mcRef.root_hash),
                Bits256(mcRef.file_hash)
            )
        }
        logger.debug { "reference masterchain block: $mcBlockId" }
        registerBlockId(mcBlockId)
        return block
    }

    public suspend fun getAccount(
        address: String, mode: Int = 0
    ): Account? {
        return getAccount(parseAccountId(address) ?: return null, mode)
    }

    public suspend fun getAccount(
        address: LiteServerAccountId, mode: Int = 0
    ): Account? {
        return getAccount(address, getCachedLastMasterchainBlockId(), mode)
    }

    public suspend fun getAccount(
        address: String, blockId: TonNodeBlockIdExt, mode: Int = 0
    ): Account? {
        return getAccount(parseAccountId(address) ?: return null, blockId, mode)
    }

    public suspend fun getAccount(
        address: LiteServerAccountId, blockId: TonNodeBlockIdExt, mode: Int = 0
    ): AccountInfo? {
        logger.debug {
            "requesting account state for ${address.workchain}:${
                address.id
            } with respect to $blockId with mode $mode"
        }
        val accountState = liteApi(LiteServerGetAccountState(blockId, address))
        val shardBlock = accountState.shardBlock
        logger.debug {
            "got account state for ${address.workchain}:${
                address.id
            } with respect to blocks ${blockId}${if (shardBlock == blockId) "" else " and $shardBlock"}"
        }
        val stateBoc = BagOfCells(accountState.state)
        val account = stateBoc.first().parse { loadTlb(Account) }
        return account as? AccountInfo
    }

    public suspend fun runSmcMethod(
        address: LiteServerAccountId, methodName: String, vararg params: VmStackValue
    ): VmStack = coroutineScope {
        runSmcMethod(
            address, getCachedLastMasterchainBlockId(), smcMethodId(methodName), params.asIterable()
        )
    }

    public suspend fun runSmcMethod(
        address: LiteServerAccountId, method: Long, vararg params: VmStackValue
    ): VmStack = coroutineScope {
        runSmcMethod(address, getCachedLastMasterchainBlockId(), method, params.asIterable())
    }

    public suspend fun runSmcMethod(
        address: LiteServerAccountId, methodName: String, params: Iterable<VmStackValue>
    ): VmStack = coroutineScope {
        runSmcMethod(address, getCachedLastMasterchainBlockId(), smcMethodId(methodName), params)
    }

    public suspend fun runSmcMethod(
        address: LiteServerAccountId, method: Long, params: Iterable<VmStackValue>
    ): VmStack = coroutineScope {
        runSmcMethod(address, getCachedLastMasterchainBlockId(), method, params)
    }

    public suspend fun runSmcMethod(
        address: LiteServerAccountId, blockId: TonNodeBlockIdExt, methodName: String, vararg params: VmStackValue
    ): VmStack = runSmcMethod(address, blockId, smcMethodId(methodName), *params)

    public suspend fun runSmcMethod(
        address: LiteServerAccountId, blockId: TonNodeBlockIdExt, methodName: String, params: Iterable<VmStackValue>
    ): VmStack = runSmcMethod(address, blockId, smcMethodId(methodName), params)

    public suspend fun runSmcMethod(
        address: LiteServerAccountId, blockId: TonNodeBlockIdExt, method: Long, vararg params: VmStackValue
    ): VmStack = runSmcMethod(address, blockId, method, params.asIterable())

    public suspend fun runSmcMethod(
        address: LiteServerAccountId, blockId: TonNodeBlockIdExt, method: Long, params: Iterable<VmStackValue>
    ): VmStack {
        logger.debug { "run: $address - ${params.toList()}" }
        val result = liteApi(
            LiteServerRunSmcMethod(
                0b100, blockId, address, method, smcCreateParams(params).toByteArray()
            )
        )
        check((!blockId.isValid()) || blockId == result.id) {
            "block id mismatch, expected: $blockId actual: $result.id"
        }
        val boc = BagOfCells(
            checkNotNull(result.result) { "result is null, but 0b100 mode provided" }
        )
        // TODO: check proofs
        val exitCode = result.exitCode
        if (exitCode != 0) throw TvmException(exitCode)
        return try {
            VmStack.tlbCodec().loadTlb(boc.first().beginParse())
        } catch (e: Exception) {
            throw RuntimeException("Can't parse result for $method@$address($params)", e)
        }
    }


    public suspend fun sendMessage(body: Message<Cell>): LiteServerSendMsgStatus = sendMessage(CellRef(body))
    public suspend fun sendMessage(body: CellRef<Message<Cell>>): LiteServerSendMsgStatus =
        sendMessage(body.toCell(Message.tlbCodec(AnyTlbConstructor)))

    public suspend fun sendMessage(cell: Cell): LiteServerSendMsgStatus = sendMessage(BagOfCells(cell))
    public suspend fun sendMessage(boc: BagOfCells): LiteServerSendMsgStatus {
        return liteApi(LiteServerSendMessage(boc.toByteArray()))
    }

    private fun smcMethodId(methodName: String): Long = crc16(methodName).toLong() or 0x10000

    private fun smcCreateParams(
        vmStack: VmStack
    ): BagOfCells = BagOfCells(
        CellBuilder.createCell {
            storeTlb(VmStack, vmStack)
        }
    )

    private fun smcCreateParams(
        params: Iterable<VmStackValue>
    ): BagOfCells = smcCreateParams(VmStack(VmStackList(params.asIterable())))

    private fun smcCreateParams(
        vararg params: VmStackValue
    ): BagOfCells = smcCreateParams(params.asIterable())

    override fun close(): Unit = runBlocking {
        knownBlockIds.clear()
    }

    private suspend fun getCachedLastMasterchainBlockId(): TonNodeBlockIdExt {
        val cachedLastMasterchainBlockId = lastMasterchainBlockId
        if (!cachedLastMasterchainBlockId.isValid()) return getLastBlockId()
        return if (lastMasterchainBlockIdTime < (Clock.System.now() - 1.seconds)) {
            getLastBlockId()
        } else {
            cachedLastMasterchainBlockId
        }
    }

    private fun parseBlockIdExt(string: String): TonNodeBlockIdExt {
        return if (string.endsWith(')')) {
            TonNodeBlockIdExt(TonNodeBlockId.parse(string))
        } else {
            TonNodeBlockIdExt.parse(string)
        }
    }

    private fun parseAccountId(string: String) = when (string) {
        "none", "root" -> null
        "config" -> TODO()
        "elector" -> TODO()
        "dnsroot" -> TODO()
        else -> {
            val addrStd = AddrStd(string)
            LiteServerAccountId(addrStd.workchain_id, addrStd.address.toByteArray())
        }
    }

    private fun registerBlockId(blockIdExt: TonNodeBlockIdExt) {
        if (knownBlockIds.contains(blockIdExt)) return
        if (BLOCK_ID_CACHE_SIZE > 0 && knownBlockIds.size == BLOCK_ID_CACHE_SIZE) {
            knownBlockIds.removeFirst()
        }
        knownBlockIds.addLast(blockIdExt)
    }

}
