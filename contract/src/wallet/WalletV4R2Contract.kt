package org.ton.contract.wallet

import kotlinx.io.bytestring.ByteString
import org.ton.bigint.toInt
import org.ton.bitstring.BitString
import org.ton.block.*
import org.ton.boc.BagOfCells
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.buildCell
import org.ton.contract.exception.AccountNotInitializedException
import org.ton.contract.wallet.WalletContract.Companion.DEFAULT_WALLET_ID
import org.ton.hashmap.HashMapE
import org.ton.kotlin.account.Account
import org.ton.lite.client.LiteClient
import org.ton.sdk.crypto.PrivateKeyEd25519
import org.ton.sdk.crypto.PublicKeyEd25519
import org.ton.tlb.CellRef
import org.ton.tlb.TlbConstructor
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb
import kotlin.io.encoding.Base64

public class WalletV4R2Contract(
    override val liteClient: LiteClient,
    override val address: AddrStd
) : WalletContract {
    public suspend fun getWalletData(): Data {
        val data =
            ((liteClient.getAccountState(address).account.value as? Account)?.storage?.state as? AccountActive)?.value?.data?.value?.value?.beginParse()
        require(data != null) { throw AccountNotInitializedException(address) }
        return Data.loadTlb(data)
    }

    public suspend fun getWalletDataOrNull(): Data? = try {
        getWalletData()
    } catch (e: AccountNotInitializedException) {
        null
    }

    public suspend fun transfer(
        privateKey: PrivateKeyEd25519,
        walletData: Data?,
        transfer: WalletTransfer
    ) {
        val seqno = walletData?.seqno ?: 0
        val walletId = walletData?.subWalletId ?: DEFAULT_WALLET_ID
        val stateInit = if (walletData == null) stateInit(
            Data(
                seqno,
                walletId,
                privateKey.publicKey(),
            )
        ).load() else null
        val message = transferMessage(
            address = address,
            stateInit = stateInit,
            privateKey = privateKey,
            walletId = walletId,
            validUntil = Int.MAX_VALUE,
            seqno = seqno,
            transfer
        )
        liteClient.sendMessage(message)
    }

    public override suspend fun transfer(
        privateKey: PrivateKeyEd25519,
        transfer: WalletTransfer
    ): Unit = transfer(privateKey, getWalletDataOrNull(), transfer)

    public data class Data(
        val seqno: Int,
        val subWalletId: Int,
        val publicKey: PublicKeyEd25519,
        val plugins: HashMapE<Cell>
    ) {
        public constructor(seqno: Int, subWalletId: Int, publicKey: PublicKeyEd25519) : this(
            seqno,
            subWalletId,
            publicKey,
            HashMapE.empty()
        )

        public constructor(seqno: Int, publicKey: PublicKeyEd25519) : this(seqno, DEFAULT_WALLET_ID, publicKey)

        public companion object : TlbConstructor<Data>(
            "wallet.v4r2.data seqno:uint32 sub_wallet_id:int32 public_key:bits256 = WalletV4R2Data"
        ) {
            override fun loadTlb(cellSlice: CellSlice): Data {
                val seqno = cellSlice.loadUInt(32).toInt()
                val subWalletId = cellSlice.loadUInt(32).toInt()
                val publicKey = PublicKeyEd25519(ByteString(*cellSlice.loadBits(256).toByteArray()))
                val plugins = cellSlice.loadTlb(HashMapE.tlbCodec(8 + 256, AnyTlbConstructor))
                return Data(seqno, subWalletId, publicKey, plugins)
            }

            override fun storeTlb(cellBuilder: CellBuilder, value: Data) {
                cellBuilder.storeUInt(value.seqno, 32)
                cellBuilder.storeUInt(value.subWalletId, 32)
                cellBuilder.storeBytes(value.publicKey.key.toByteArray())
                cellBuilder.storeBit(false)
            }
        }
    }

    public companion object {
        public val CODE: Cell by lazy(LazyThreadSafetyMode.PUBLICATION) {
            BagOfCells(
                Base64.decode("te6cckECFAEAAtQAART/APSkE/S88sgLAQIBIAIDAgFIBAUE+PKDCNcYINMf0x/THwL4I7vyZO1E0NMf0x/T//QE0VFDuvKhUVG68qIF+QFUEGT5EPKj+AAkpMjLH1JAyx9SMMv/UhD0AMntVPgPAdMHIcAAn2xRkyDXSpbTB9QC+wDoMOAhwAHjACHAAuMAAcADkTDjDQOkyMsfEssfy/8QERITAubQAdDTAyFxsJJfBOAi10nBIJJfBOAC0x8hghBwbHVnvSKCEGRzdHK9sJJfBeAD+kAwIPpEAcjKB8v/ydDtRNCBAUDXIfQEMFyBAQj0Cm+hMbOSXwfgBdM/yCWCEHBsdWe6kjgw4w0DghBkc3RyupJfBuMNBgcCASAICQB4AfoA9AQw+CdvIjBQCqEhvvLgUIIQcGx1Z4MesXCAGFAEywUmzxZY+gIZ9ADLaRfLH1Jgyz8gyYBA+wAGAIpQBIEBCPRZMO1E0IEBQNcgyAHPFvQAye1UAXKwjiOCEGRzdHKDHrFwgBhQBcsFUAPPFiP6AhPLassfyz/JgED7AJJfA+ICASAKCwBZvSQrb2omhAgKBrkPoCGEcNQICEekk30pkQzmkD6f+YN4EoAbeBAUiYcVnzGEAgFYDA0AEbjJftRNDXCx+AA9sp37UTQgQFA1yH0BDACyMoHy//J0AGBAQj0Cm+hMYAIBIA4PABmtznaiaEAga5Drhf/AABmvHfaiaEAQa5DrhY/AAG7SB/oA1NQi+QAFyMoHFcv/ydB3dIAYyMsFywIizxZQBfoCFMtrEszMyXP7AMhAFIEBCPRR8qcCAHCBAQjXGPoA0z/IVCBHgQEI9FHyp4IQbm90ZXB0gBjIywXLAlAGzxZQBPoCFMtqEssfyz/Jc/sAAgBsgQEI1xj6ANM/MFIkgQEI9Fnyp4IQZHN0cnB0gBjIywXLAlAFzxZQA/oCE8tqyx8Syz/Jc/sAAAr0AMntVGliJeU=")
            ).first()
        }

        public const val OP_SEND: Int = 0

        public fun address(privateKey: PrivateKeyEd25519, workchainId: Int = 0): AddrStd {
            val stateInitRef = stateInit(Data(0, privateKey.publicKey()))
            val hash = stateInitRef.hash()
            return AddrStd(workchainId, hash)
        }

        public fun stateInit(
            data: Data
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
            walletId: Int,
            validUntil: Int,
            seqno: Int,
            vararg transfers: WalletTransfer
        ): Message<Cell> {
            val info = ExtInMsgInfo(
                src = AddrNone,
                dest = address,
                importFee = Coins()
            )
            val maybeStateInit =
                Maybe.of(stateInit?.let {
                    Either.of<StateInit, CellRef<StateInit>>(
                        null,
                        CellRef(value = it, StateInit)
                    )
                })
            val transferBody = createTransferMessageBody(
                privateKey,
                walletId,
                validUntil,
                seqno,
                *transfers
            )
            val body = Either.of<Cell, CellRef<Cell>>(null, CellRef(value = transferBody, AnyTlbConstructor))
            return Message(
                info = info,
                init = maybeStateInit,
                body = body
            )
        }

        private fun createTransferMessageBody(
            privateKey: PrivateKeyEd25519,
            walletId: Int,
            validUntil: Int,
            seqno: Int,
            vararg gifts: WalletTransfer
        ): Cell {
            val unsignedBody = CellBuilder.createCell {
                storeUInt(walletId, 32)
                storeUInt(validUntil, 32)
                storeUInt(seqno, 32)
                storeUInt(OP_SEND, 8)
                for (gift in gifts) {
                    var sendMode = 3
                    if (gift.sendMode > -1) {
                        sendMode = gift.sendMode
                    }
                    val intMsg = CellRef(gift.toMessageRelaxed(), MessageRelaxed.tlbCodec(AnyTlbConstructor))

                    storeUInt(sendMode, 8)
                    storeRef(intMsg.cell)
                }
            }
            val signature = BitString(privateKey.signToByteArray(unsignedBody.hash().toByteArray()))

            return CellBuilder.createCell {
                storeBits(signature)
                storeBits(unsignedBody.bits)
                storeRefs(unsignedBody.refs)
            }
        }
    }
}
