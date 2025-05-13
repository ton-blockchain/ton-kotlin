package org.ton.contract.wallet

import kotlinx.io.bytestring.ByteString
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.api.pub.PublicKeyEd25519
import org.ton.bigint.BigInt
import org.ton.bigint.toBigInt
import org.ton.bigint.xor
import org.ton.bitstring.BitString
import org.ton.block.*
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.buildCell
import org.ton.contract.exception.AccountNotInitializedException
import org.ton.hashmap.HashMapE
import org.ton.kotlin.account.Account
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.message.MessageLayout
import org.ton.lite.client.LiteClient
import org.ton.tlb.CellRef
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbStorer
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.storeTlb
import kotlin.io.encoding.Base64

public class WalletId (
    public val walletVersion: Int = 0,
    public val subwalletNumber: Int,
    public val networkGlobalId: Int,
    public val workchain: Int,
) {
    public fun serialize(): BigInt {
        val context = CellBuilder.createCell {
            storeUInt(1, 1)
            storeUInt(workchain, 8)
            storeUInt(walletVersion, 8)
            storeUInt(subwalletNumber, 15)
            endCell()
        }.beginParse().loadInt(32)

        return networkGlobalId.toBigInt().xor(context)
    }
}

public class WalletV5R1Contract(
    override val liteClient: LiteClient,
    override val address: AddrStd,
    public val walletId: WalletId
) : WalletContract {
    public suspend fun getWalletData(walletId: WalletId): Data {
        val data =
            ((liteClient.getAccountState(address).account.value as? Account)?.storage?.state as? AccountActive)?.value?.data?.value?.value?.beginParse()
        require(data != null) { throw AccountNotInitializedException(address) }

        val walletData = Data.loadTlb(data)
        walletData.walletId = walletId
        return walletData
    }

    public suspend fun getWalletDataOrNull(walletId: WalletId): Data? = try {
        getWalletData(walletId)
    } catch (e: AccountNotInitializedException) {
        null
    }

    public suspend fun transfer(
        privateKey: PrivateKeyEd25519,
        walletData: Data?,
        transfer: WalletTransfer
    ) {
        val seqno = walletData?.seqno ?: 0

        val walletData = walletData ?: Data(
            seqno,
            privateKey.publicKey(),
            walletId
        )

        val stateInit = if (walletData.seqno == 0) stateInit(walletData).load() else null

        val message = transferMessage(
            address = address,
            stateInit = stateInit,
            privateKey = privateKey,
            validUntil = Int.MAX_VALUE,
            seqno = seqno,
            walletId = walletData.walletId,
            transfer
        )

        liteClient.sendMessage(message)
    }

    public override suspend fun transfer(
        privateKey: PrivateKeyEd25519,
        transfer: WalletTransfer
    ): Unit = transfer(privateKey, getWalletDataOrNull(walletId), transfer)

    public data class Data(
        val seqno: Int,
        val publicKey: PublicKeyEd25519,
        var walletId: WalletId,
        val plugins: HashMapE<Cell>
    ) {
        public constructor(seqno: Int, publicKey: PublicKeyEd25519, walletId: WalletId) : this(
            seqno,
            publicKey,
            walletId,
            HashMapE.empty()
        )

        public companion object : TlbConstructor<Data>(
            "wallet.v5r1.data seqno:uint32 public_key:bits256 plugins:(HashmapE 256 (Maybe ^Cell)) = WalletV5R1Data"
        ) {
            override fun loadTlb(cellSlice: CellSlice): Data {
                val authAllow = cellSlice.loadUInt(1)
                val seqno = cellSlice.loadUInt(32).toInt()
                val serialized = cellSlice.loadInt(32)
                val publicKey = PublicKeyEd25519(ByteString(*cellSlice.loadBits(256).toByteArray()))

                // Create context cell
                val context = CellBuilder.createCell {
                    storeUInt(1, 1)
                    storeUInt(0, 8)  // workchain
                    storeUInt(0, 8)  // walletVersion
                    storeUInt(0, 15) // subwalletNumber
                }.beginParse().loadInt(32)

                val networkGlobalId = serialized.xor(context)

                // Create walletId with default values and the extracted networkGlobalId
                val walletId = WalletId(
                    walletVersion = 0,
                    subwalletNumber = 0,
                    networkGlobalId = networkGlobalId.toInt(),
                    workchain = 0
                )

                return Data(seqno, publicKey, walletId)
            }

            override fun storeTlb(cellBuilder: CellBuilder, value: Data) {
                cellBuilder.storeUInt(1, 1) // is signature auth allowed
                cellBuilder.storeUInt(value.seqno, 32)
                cellBuilder.storeInt(value.walletId.serialize(), 32)
                cellBuilder.storeBytes(value.publicKey.key.toByteArray())
                cellBuilder.storeBoolean(false)
            }
        }
    }

    public companion object {
        public val CODE: Cell by lazy(LazyThreadSafetyMode.PUBLICATION) {
            BagOfCells(
                Base64.decode("te6cckECFAEAAoEAART/APSkE/S88sgLAQIBIAINAgFIAwQC3NAg10nBIJFbj2Mg1wsfIIIQZXh0br0hghBzaW50vbCSXwPgghBleHRuuo60gCDXIQHQdNch+kAw+kT4KPpEMFi9kVvg7UTQgQFB1yH0BYMH9A5voTGRMOGAQNchcH/bPOAxINdJgQKAuZEw4HDiEA8CASAFDAIBIAYJAgFuBwgAGa3OdqJoQCDrkOuF/8AAGa8d9qJoQBDrkOuFj8ACAUgKCwAXsyX7UTQcdch1wsfgABGyYvtRNDXCgCAAGb5fD2omhAgKDrkPoCwBAvIOAR4g1wsfghBzaWduuvLgin8PAeaO8O2i7fshgwjXIgKDCNcjIIAg1yHTH9Mf0x/tRNDSANMfINMf0//XCgAK+QFAzPkQmiiUXwrbMeHywIffArNQB7Dy0IRRJbry4IVQNrry4Ib4I7vy0IgikvgA3gGkf8jKAMsfAc8Wye1UIJL4D95w2zzYEAP27aLt+wL0BCFukmwhjkwCIdc5MHCUIccAs44tAdcoIHYeQ2wg10nACPLgkyDXSsAC8uCTINcdBscSwgBSMLDy0InXTNc5MAGk6GwShAe78uCT10rAAPLgk+1V4tIAAcAAkVvg69csCBQgkXCWAdcsCBwS4lIQseMPINdKERITAJYB+kAB+kT4KPpEMFi68uCR7UTQgQFB1xj0BQSdf8jKAEAEgwf0U/Lgi44UA4MH9Fvy4Iwi1woAIW4Bs7Dy0JDiyFADzxYS9ADJ7VQAcjDXLAgkji0h8uCS0gDtRNDSAFETuvLQj1RQMJExnAGBAUDXIdcKAPLgjuLIygBYzxbJ7VST8sCN4gAQk1vbMeHXTNC01sNe")
            ).first()
        }

        public const val OP_SEND: Int = 0
        public const val MESSAGE_TYPE_EXT: Int = 0x7369676E
        public const val OUT_ACTION_SEND_MSG_TAG: Int = 0x0ec3c86d

        public fun address(privateKey: PrivateKeyEd25519, walletId: WalletId): AddrStd {
            val stateInitRef = stateInit(Data(0, privateKey.publicKey(), walletId)) // Initial sequence number is 0
            val hash = stateInitRef.hash()
            return AddrStd(walletId.workchain, hash)
        }

        public fun stateInit(
            data: Data,
        ): CellRef<StateInit> {
            val dataCell = buildCell {
                storeTlb(Data, data)
            }
            return CellRef(
                StateInit(CODE, dataCell),
                StateInit
            )
        }

        public fun transferMessage(
            address: MsgAddressInt,
            stateInit: StateInit?,
            privateKey: PrivateKeyEd25519,
            validUntil: Int,
            seqno: Int,
            walletId: WalletId,
            vararg transfers: WalletTransfer
        ): Message<Cell> {
            val info = ExtInMsgInfo(
                src = AddrNone,
                dest = address,
                importFee = Coins()
            )

            val transferBody = createTransferMessageBody(
                privateKey,
                validUntil,
                seqno,
                walletId,
                *transfers
            )

            val layout = MessageLayout.compute(
                info = info,
                init = stateInit,
                body = transferBody,
                bodyStorer = object : TlbStorer<Cell> {
                    override fun storeTlb(builder: CellBuilder, value: Cell, context: CellContext) {
                        builder.storeBitString(value.bits)
                        builder.storeRefs(value.refs)
                    }
                }
            )

            return Message(
                info = info,
                init = stateInit,
                body = transferBody,
                bodyCodec = AnyTlbConstructor,
                layout = layout
            )
        }

        private fun createTransferMessageBody(
            privateKey: PrivateKeyEd25519,
            validUntil: Int,
            seqno: Int,
            walletId: WalletId,
            vararg gifts: WalletTransfer
        ): Cell {
            val packed = packV5Actions(*gifts)
            val unsignedBody = CellBuilder.createCell {
                storeUInt(MESSAGE_TYPE_EXT, 32) // MessageType.ext
                storeUInt(walletId.serialize(), 32)
                if(seqno == 0) {
                    storeUInt(0xFFFFFFFF, 32)
                } else {
                    storeUInt(validUntil, 32)
                }
                storeUInt(seqno, 32)
                storeBitString(packed.bits)
                storeRefs(packed.refs)
            }

            val signature = BitString(privateKey.sign(unsignedBody.hash().toByteArray()))

            return CellBuilder.createCell {
                storeBitString(unsignedBody.bits)
                storeRefs(unsignedBody.refs)
                storeBitString(signature)
            }
        }

        private fun packV5Actions(vararg gifts: WalletTransfer): CellBuilder {

            var latestCell = Cell.empty()
            for (gift in gifts) {
                val intMsg = CellRef(gift.toMessageRelaxed(), MessageRelaxed.tlbCodec(AnyTlbConstructor))

                latestCell = CellBuilder.createCell {
                    storeUInt(OUT_ACTION_SEND_MSG_TAG, 32) // OUT_ACTION_SEND_MSG_TAG
                    storeUInt(gift.sendMode, 8)
                    storeRefs(latestCell)
                    storeRefs(intMsg.cell)
                }
            }

            return CellBuilder.beginCell().apply {
                storeBoolean(true)
                storeRef(latestCell)
                storeBoolean(false)
            }
        }
    }
}
