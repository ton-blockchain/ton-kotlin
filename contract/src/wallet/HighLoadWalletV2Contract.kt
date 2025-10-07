//package org.ton.contract.wallet
//
//import org.ton.api.pub.PublicKeyEd25519
//import org.ton.bitstring.BitString
//import org.ton.block.*
//import org.ton.boc.BagOfCells
//import org.ton.cell.Cell
//import org.ton.cell.CellBuilder
//import org.ton.cell.CellSlice
//import org.ton.cell.buildCell
//import org.ton.contract.SmartContract
//import org.ton.crypto.encoding.base64
//import org.ton.hashmap.HashMapE
//import org.ton.lite.api.LiteApi
//import org.ton.tl.asByteString
//import org.ton.tlb.*
//import org.ton.tlb.constructor.AnyTlbConstructor
//import org.ton.tlb.providers.TlbConstructorProvider
//import kotlin.jvm.JvmField
//import kotlin.jvm.JvmStatic
//
//public class HighLoadWalletV2Contract(
//    override val address: MsgAddressInt,
//    public override val state: AccountState
//) : WalletContract<HighLoadWalletV2Contract.Data> {
//
//    public constructor(
//        workchain: Int,
//        init: StateInit
//    ) : this(SmartContract.address(workchain, init), AccountUninit)
//
//    public constructor(
//        workchain: Int,
//        data: Data
//    ) : this(workchain, stateInit(data))
//
//    public constructor(
//        accountInfo: AccountInfo
//    ) : this(accountInfo.addr, accountInfo.storage.state)
//
//    override fun loadData(): Data? = data?.let {
//        Data.loadTlb(it)
//    }
//
//    public fun getSubWalletId(): Int = requireNotNull(loadData()).subWalletId
//
//    public fun getLastCleaned(): Long = requireNotNull(loadData()).lastCleaned
//
//    public fun getPublicKey(): PublicKeyEd25519 = PublicKeyEd25519(requireNotNull(loadData()).publicKey.toByteArray().asByteString())
//
//    public suspend fun <X : Any> sendQuery(
//        liteApi: LiteApi,
//        codec: TlbCodec<X>,
//        query: Query<X>,
//        stateInit: StateInit? = null
//    ) {
//        val externalMessage = Message(
//            info = ExtInMsgInfo(
//                src = AddrNone,
//                dest = address,
//                importFee = Coins()
//            ),
//            init = Maybe.of(stateInit?.let { Either.of(left = null, right = CellRef(it)) }),
//            body = Either.of(left = null, CellRef(query))
//        )
//        sendExternalMessage(liteApi, Query.tlbConstructor(codec), externalMessage)
//    }
//
//    public interface Data {
//        public val subWalletId: Int
//        public val lastCleaned: Long
//        public val publicKey: BitString
//        public val oldQueries: HashMapE<WalletMessage<Cell>>
//
//        public companion object : TlbConstructorProvider<Data> by HighLoadWalletV2DataTlbConstructor {
//            @JvmStatic
//            override fun tlbConstructor(): TlbConstructor<Data> = HighLoadWalletV2DataTlbConstructor
//        }
//    }
//
//    public interface Query<X : Any> {
//        public val signature: BitString
//        public val payload: QueryPayload<X>
//
//        public companion object {
//            @JvmStatic
//            public fun <X : Any> tlbConstructor(x: TlbCodec<X>): TlbConstructor<Query<X>> =
//                HighLoadWalletV2QueryTlbConstructor(x)
//        }
//    }
//
//    public interface QueryPayload<X : Any> {
//        public val subWalletId: Int
//        public val queryId: Long
//        public val msgs: HashMapE<WalletMessage<X>>
//
//        public companion object {
//            @JvmStatic
//            public fun <X : Any> tlbConstructor(x: TlbCodec<X>): TlbConstructor<QueryPayload<X>> =
//                HighLoadWalletV2QueryPayloadTlbConstructor(x)
//        }
//    }
//
//    public companion object {
//        @JvmField
//        public val CODE: Cell = BagOfCells(
//            base64(
//                "te6ccgEBCQEA5QABFP8A9KQT9LzyyAsBAgEgAgMCAUgEBQHq8oMI1xgg0x/TP/gjqh9TILnyY+1E0NMf0z/T//" +
//                        "QE0VNggED0Dm+hMfJgUXO68qIH+QFUEIf5EPKjAvQE0fgAf44WIYAQ9HhvpSCYAtMH1DAB+wCRMuIBs+" +
//                        "ZbgyWhyEA0gED0Q4rmMcgSyx8Tyz/L//QAye1UCAAE0DACASAGBwAXvZznaiaGmvmOuF/8AEG+X5dqJoaY+Y6Z/p/" +
//                        "5j6AmipEEAgegc30JjJLb/JXdHxQANCCAQPSWb6UyURCUMFMDud4gkzM2AZIyMOKz"
//            )
//        ).first()
//
//        @JvmStatic
//        public fun data(
//            subWalletId: Int,
//            lastCleaned: Long,
//            publicKey: BitString,
//            oldQueries: HashMapE<WalletMessage<Cell>>
//        ): Data = HighLoadWalletV2DataImpl(subWalletId, lastCleaned, publicKey, oldQueries)
//
//        @JvmStatic
//        public fun <X : Any> query(
//            signature: BitString,
//            payload: QueryPayload<X>
//        ): Query<X> = HighLoadWalletV2QueryImpl(signature, payload)
//
//        @JvmStatic
//        public fun <X : Any> queryPayload(
//            subWalletId: Int,
//            queryId: Long,
//            msgs: HashMapE<WalletMessage<X>>
//        ): QueryPayload<X> = HighLoadWalletV2QueryPayloadImpl(subWalletId, queryId, msgs)
//
//        @JvmStatic
//        public fun stateInit(
//            data: Data
//        ): StateInit {
//            return StateInit(
//                code = CODE,
//                data = buildCell {
//                    Data.storeTlb(this, data)
//                }
//            )
//        }
//    }
//}
//
//private data class HighLoadWalletV2QueryImpl<X : Any>(
//    override val signature: BitString,
//    override val payload: HighLoadWalletV2Contract.QueryPayload<X>
//) : HighLoadWalletV2Contract.Query<X>
//
//private class HighLoadWalletV2QueryTlbConstructor<X : Any>(
//    val x: TlbCodec<X>
//) : TlbConstructor<HighLoadWalletV2Contract.Query<X>>(
//    schema = "highload_wallet_v2_query#_ signature:(bits 512) payload:(HighLoadWalletV2QueryPayload X) = HighLoadWalletV2Query X"
//) {
//    override fun loadTlb(cellSlice: CellSlice): HighLoadWalletV2Contract.Query<X> {
//        val signature = cellSlice.loadBits(512)
//        val payload = cellSlice.loadTlb(HighLoadWalletV2Contract.QueryPayload.tlbConstructor(x))
//        return HighLoadWalletV2QueryImpl(signature, payload)
//    }
//
//    override fun storeTlb(cellBuilder: CellBuilder, value: HighLoadWalletV2Contract.Query<X>) {
//        cellBuilder.storeBits(value.signature)
//        HighLoadWalletV2Contract.QueryPayload.tlbConstructor(x).storeTlb(cellBuilder, value.payload)
//    }
//}
//
//private data class HighLoadWalletV2QueryPayloadImpl<X : Any>(
//    override val subWalletId: Int,
//    override val queryId: Long,
//    override val msgs: HashMapE<WalletMessage<X>>
//) : HighLoadWalletV2Contract.QueryPayload<X>
//
//private class HighLoadWalletV2QueryPayloadTlbConstructor<X : Any>(
//    x: TlbCodec<X>
//) : TlbConstructor<HighLoadWalletV2Contract.QueryPayload<X>>(
//    schema = "highload_wallet_v2_query_payload#_ subwallet_id:uint32 query_id:uint64 msgs:(HashmapE 16 WalletMessage X) = HighLoadWalletV2QueryPayload X"
//) {
//    val msgs = HashMapE.tlbCodec(16, WalletMessage.tlbCodec(x))
//
//    override fun loadTlb(cellSlice: CellSlice): HighLoadWalletV2Contract.QueryPayload<X> {
//        val subWalletId = cellSlice.loadInt(32).toInt()
//        val queryId = cellSlice.loadInt(64).toLong()
//        val msgs = cellSlice.loadTlb(msgs)
//        return HighLoadWalletV2QueryPayloadImpl(subWalletId, queryId, msgs)
//    }
//
//    override fun storeTlb(cellBuilder: CellBuilder, value: HighLoadWalletV2Contract.QueryPayload<X>) {
//        cellBuilder.storeInt(value.subWalletId, 32)
//        cellBuilder.storeInt(value.queryId, 64)
//        cellBuilder.storeTlb(msgs, value.msgs)
//    }
//}
//
//private data class HighLoadWalletV2DataImpl(
//    override val subWalletId: Int,
//    override val lastCleaned: Long,
//    override val publicKey: BitString,
//    override val oldQueries: HashMapE<WalletMessage<Cell>>
//) : HighLoadWalletV2Contract.Data
//
//private object HighLoadWalletV2DataTlbConstructor : TlbConstructor<HighLoadWalletV2Contract.Data>(
//    schema = "highload_wallet_v2_data#_ sub_wallet_id:uint32 last_cleaned:uint64 public_key:bits256 old_queries:HighLoadWalletV2Queries = HighLoadWalletV2Data"
//) {
//    override fun loadTlb(cellSlice: CellSlice): HighLoadWalletV2Contract.Data {
//        val subWalletId = cellSlice.loadInt(32).toInt()
//        val lastCleaned = cellSlice.loadInt(64).toLong()
//        val publicKey = cellSlice.loadBits(256)
//        val oldQueries = cellSlice.loadTlb(HighLoadWalletV2QueriesTlbConstructor)
//        return HighLoadWalletV2DataImpl(subWalletId, lastCleaned, publicKey, oldQueries)
//    }
//
//    override fun storeTlb(cellBuilder: CellBuilder, value: HighLoadWalletV2Contract.Data) {
//        cellBuilder.storeInt(value.subWalletId, 32)
//        cellBuilder.storeInt(value.lastCleaned, 64)
//        cellBuilder.storeBits(value.publicKey)
//        cellBuilder.storeTlb(HighLoadWalletV2QueriesTlbConstructor, value.oldQueries)
//    }
//}
//
//private object HighLoadWalletV2QueriesTlbConstructor : TlbConstructor<HashMapE<WalletMessage<Cell>>>(
//    schema = "_ (HashmapE 64 WalletMessage Any) = HighLoadWalletV2Queries"
//) {
//    val hashMapEHighLoadWalletV2QueriesEntry = HashMapE.tlbCodec(64, WalletMessage.tlbCodec(AnyTlbConstructor))
//
//    override fun loadTlb(cellSlice: CellSlice): HashMapE<WalletMessage<Cell>> {
//        return cellSlice.loadTlb(hashMapEHighLoadWalletV2QueriesEntry)
//    }
//
//    override fun storeTlb(cellBuilder: CellBuilder, value: HashMapE<WalletMessage<Cell>>) {
//        cellBuilder.storeTlb(hashMapEHighLoadWalletV2QueriesEntry, value)
//    }
//}
