package org.ton.lite.client

import io.ktor.utils.io.core.Closeable
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.contentEquals
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
import org.ton.kotlin.account.Account
import org.ton.kotlin.transaction.Transaction
import org.ton.lite.api.LiteApiClient
import org.ton.lite.api.exception.LiteServerException
import org.ton.lite.api.exception.LiteServerNotReadyException
import org.ton.lite.api.exception.LiteServerUnknownException
import org.ton.lite.api.liteserver.*
import org.ton.lite.api.liteserver.functions.*
import org.ton.lite.client.internal.*
import org.ton.sdk.crypto.crc16
import org.ton.sdk.crypto.sha256
import org.ton.tlb.CellRef
import org.ton.tlb.NullableTlbCodec
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.storeTlb
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.use

private const val BLOCK_ID_CACHE_SIZE = 100

public class LiteClient(
    coroutineContext: CoroutineContext,
    liteClientConfigGlobal: LiteClientConfigGlobal
) : Closeable, CoroutineScope, LiteClientApi {
    public constructor(
        coroutineContext: CoroutineContext,
        liteServers: Collection<LiteServerDesc>
    ) : this(coroutineContext, LiteClientConfigGlobal(liteServers = liteServers))

    public constructor(
        coroutineContext: CoroutineContext,
        vararg liteServer: LiteServerDesc
    ) : this(coroutineContext, liteServer.toList())

    init {
        require(liteClientConfigGlobal.liteServers.isNotEmpty()) { "No lite servers provided" }
    }

    override val coroutineContext: CoroutineContext = coroutineContext + CoroutineName("LiteClient")
    private val knownBlockIds: ArrayDeque<TonNodeBlockIdExt> = ArrayDeque(100)
    private var lastMasterchainBlockId: TonNodeBlockIdExt by atomic(
        TonNodeBlockIdExt(
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
        override suspend fun sendRawQuery(query: ByteArray): ByteArray {
            var attempts = 0
            var exception: Exception? = null
            var liteServer: LiteServerDesc? = null
            while (attempts < maxOf(5, serverList.size)) {
                try {
                    liteServer = serverList[currentServer++ % serverList.size]
                    return LiteTcpConnection(liteServer).call(query)
                } catch (e: LiteServerException) {
                    exception = e
                    break
                } catch (e: Exception) {
                    exception = e
                    attempts++
                    delay(100L)
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
//            logger.info { "server version is ${version shr 8}.${serverVersion and 0xFF}, capabilities $serverCapabilities" }
        }
    }

    public fun setServerTime(time: Int): Duration {
        serverTime = Instant.fromEpochSeconds(time.toLong())
        serverTimeGotAt = Clock.System.now()
        val latency = latency()
//        logger.debug { "server time is $serverTime (latency $latency)" }
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

//        logger.debug { "last masterchain block is $last" }

        var createdAt: Instant? = null
        if (ext != null) {
            setServerVersion(ext.version, ext.capabilities)
            setServerTime(ext.now)
            val serverNow = Instant.fromEpochSeconds(ext.now.toLong())
            val lastUtime = Instant.fromEpochSeconds(ext.lastUTime.toLong())
            createdAt = lastUtime
            if (lastUtime > serverNow) {
//                logger.warn {
//                    "server claims to have a masterchain block $last created at $lastUtime (${lastUtime - serverNow} in future)"
//                }
            } else if (lastUtime < serverNow - 60.seconds) {
//                logger.warn {
//                    "server appears to be out of sync: its newest masterchain block is $last created at $lastUtime (${serverNow - lastUtime} ago according to the server's clock)"
//                }
            } else if (lastUtime < serverTimeGotAt - 60.seconds) {
//                logger.warn {
//                    "either the server is out of sync, or the local clock is set incorrectly: the newest masterchain block known to server is $last created at $lastUtime (${serverNow - serverTimeGotAt} ago according to the local clock)"
//                }
            }
        }

        val currentZeroStateId = zeroStateId
        if (!currentZeroStateId.isValid()) {
            zeroStateId = init
//            logger.info { "zero state id set to ${init}" }
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
//        logger.debug {
//            "latest masterchain block known to server is:\n$last${
//                if (createdAt != null) {
//                    "\n  created at $createdAt (${Clock.System.now() - createdAt} ago)"
//                } else ""
//            }"
//        }
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
        val mode = when {
            time != null -> LiteServerLookupBlock.UTIME_MASK
            lt != null -> LiteServerLookupBlock.LT_MASK
            else -> LiteServerLookupBlock.ID_MASK
        }
        val blockHeader = try {
            liteApi(LiteServerLookupBlock(mode, blockId, lt, time?.epochSeconds?.toInt()))
        } catch (_: LiteServerNotReadyException) {
            return null
        } catch (e: LiteServerUnknownException) {
            if (e.message == "block is not applied") {
                return null
            } else {
                throw e
            }
        } catch (e: Exception) {
            throw RuntimeException("Can't lookup block header for $blockId from server", e)
        }
        val actualBlockId = blockHeader.id
        check(
            blockId.workchain == actualBlockId.workchain &&
                    blockId.shard == actualBlockId.shard &&
                    blockId.seqno == actualBlockId.seqno
        ) {
            "block id mismatch, expected: $blockId actual: $actualBlockId"
        }
        val blockProofCell = try {
            BagOfCells.of(blockHeader.headerProof.toByteArray()).first()
        } catch (e: Exception) {
            throw IllegalStateException("Can't parse block proof", e)
        }
        val actualRootHash = blockProofCell.refs.firstOrNull()?.hash(level = 0)?.toBitString()
        check(
            blockProofCell.type == CellType.MERKLE_PROOF &&
                    blockHeader.id.rootHash.toByteArray().toBitString() == actualRootHash
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

    public suspend fun getBlock(blockId: TonNodeBlockId): Block? {
        val blockIdExt = lookupBlock(blockId) ?: return null
        return getBlock(blockIdExt)
    }

    public suspend fun getBlock(blockId: TonNodeBlockIdExt): Block? {
        val blockData = try {
            liteApi(LiteServerGetBlock(blockId))
        } catch (_: TonNotReadyException) {
            return null
        } catch (e: Exception) {
            throw RuntimeException("Can't get block $blockId from server", e)
        }
        val actualFileHash = sha256(blockData.data.toByteArray())
        check(blockId.fileHash.contentEquals(actualFileHash)) {
            "file hash mismatch for block $blockId, expected: ${blockId.fileHash} , actual: $actualFileHash"
        }
        registerBlockId(blockId)
        val root = try {
            BagOfCells.of(blockData.data.toByteArray()).first()
        } catch (e: Exception) {
            throw RuntimeException("Can't deserialize block data", e)
        }
        root.hash().toBitString()
        // FIXME: https://github.com/andreypfau/ton-kotlin/issues/82
//        check(blockId.rootHash.toBitString() == actualRootHash) {
//            "block root hash mismatch, expected: ${blockId.rootHash} , actual: $actualRootHash"
//        }
        val block = try {
            Block.loadTlb(root.beginParse())
        } catch (e: Exception) {
            throw RuntimeException("Can't parse block: $blockId", e)
        }
        return block
    }

    override suspend fun getAccountState(accountAddress: MsgAddressInt): FullAccountState =
        getAccountState(accountAddress, getLastBlockId())

    public override suspend fun getAccountState(
        accountAddress: MsgAddressInt, blockId: TonNodeBlockIdExt
    ): FullAccountState {
        val rawAccountState = liteApi(LiteServerGetAccountState(blockId, accountAddress.toLiteServer()), blockId.seqno)
        val root = try {
            BagOfCells(rawAccountState.state.toByteArray()).first()
        } catch (e: Exception) {
            throw IllegalStateException("Can't deserialize account state", e)
        }
        if (root.isEmpty()) {
            return FullAccountState(
                rawAccountState.shardBlock,
                accountAddress,
                null,
                CellRef(null, NullableTlbCodec(Account))
            )
        }

        check(rawAccountState.id == blockId || rawAccountState.id.seqno == 0) {
            "Obtained different reference block: ${rawAccountState.id} instead of requested $blockId"
        }
        check(rawAccountState.shardBlock.isValidFull()) {
            "Shard block id: ${rawAccountState.shardBlock} in answer is invalid"
        }
        check(Shard.containsShard(rawAccountState.shardBlock.shard, Shard.extractShard(accountAddress.address))) {
            "Received data from shard block ${rawAccountState.shardBlock.shard} that can't contain requested account: ${accountAddress.address}"
        }

        return CheckProofUtils.checkAccountProof(
            rawAccountState.proof.toByteArray(),
            rawAccountState.shardBlock,
            accountAddress,
            root
        )
    }

    public override suspend fun getTransactions(
        accountAddress: MsgAddressInt,
        fromTransactionId: TransactionId,
        count: Int,
    ): List<TransactionInfo> {
        val rawTransactionList = liteApi(
            LiteServerGetTransactions(
                count,
                accountAddress.toLiteServer(),
                fromTransactionId.lt,
                ByteString(*fromTransactionId.hash.toByteArray())
            )
        )
        val transactionsCells = BagOfCells.of(rawTransactionList.transactions.toByteArray()).roots
        check(rawTransactionList.ids.size == transactionsCells.size)
        return List(transactionsCells.size) { index ->
            val transaction = CellRef(transactionsCells[index], Transaction)
            TransactionInfo(
                blockId = rawTransactionList.ids[index],
                id = TransactionId(transaction.hash(), transaction.load().lt),
                transaction = transaction
            )
        }
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
//        logger.debug { "run: $address - ${params.toList()}" }
        val result = liteApi(
            LiteServerRunSmcMethod(
                0b100, blockId, address, method, ByteString(*smcCreateParams(params).toByteArray())
            )
        )
        check((!blockId.isValid()) || blockId == result.id) {
            "block id mismatch, expected: $blockId actual: $result.id"
        }
        val boc = BagOfCells.of(
            checkNotNull(result.result) { "result is null, but 0b100 mode provided" }.toByteArray()
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


    public suspend fun sendMessage(body: Message<Cell>): LiteServerSendMsgStatus =
        sendMessage(CellRef(body, Message.tlbCodec(AnyTlbConstructor)))

    public suspend fun sendMessage(body: CellRef<Message<Cell>>): LiteServerSendMsgStatus =
        sendMessage(body.cell)

    public suspend fun sendMessage(cell: Cell): LiteServerSendMsgStatus = sendMessage(BagOfCells(cell))
    public suspend fun sendMessage(boc: BagOfCells): LiteServerSendMsgStatus {
        return liteApi(LiteServerSendMessage(ByteString(*boc.toByteArray())))
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

    private fun registerBlockId(blockIdExt: TonNodeBlockIdExt) {
        if (knownBlockIds.contains(blockIdExt)) return
        if (BLOCK_ID_CACHE_SIZE > 0 && knownBlockIds.size == BLOCK_ID_CACHE_SIZE) {
            knownBlockIds.removeFirst()
        }
        knownBlockIds.addLast(blockIdExt)
    }

    private fun MsgAddressInt.toLiteServer() = LiteServerAccountId(workchainId, ByteString(*address.toByteArray()))
}
