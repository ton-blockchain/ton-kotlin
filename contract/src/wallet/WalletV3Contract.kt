package org.ton.contract.wallet

import org.ton.api.pk.PrivateKeyEd25519
import org.ton.api.pub.PublicKeyEd25519
import org.ton.bitstring.BitString
import org.ton.block.*
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.buildCell
import org.ton.contract.exception.AccountNotInitializedException
import org.ton.contract.wallet.WalletContract.Companion.DEFAULT_WALLET_ID
import org.ton.lite.client.LiteClient
import org.ton.tl.asByteString
import org.ton.tlb.CellRef
import org.ton.tlb.TlbConstructor
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.storeRef
import org.ton.tlb.storeTlb

public class WalletV3R2Contract(
    override val liteClient: LiteClient,
    override val address: AddrStd
) : WalletContract {
    public suspend fun getWalletData(): WalletV3R2Data {
        val data =
            ((liteClient.getAccountState(address).account.value as? AccountInfo)?.storage?.state as? AccountActive)?.value?.data?.value?.value?.beginParse()
        require(data != null) { throw AccountNotInitializedException(address) }
        return WalletV3R2Data.loadTlb(data)
    }

    public suspend fun getWalletDataOrNull(): WalletV3R2Data? = try {
        getWalletData()
    } catch (e: AccountNotInitializedException) {
        null
    }

    public override suspend fun transfer(
        privateKey: PrivateKeyEd25519,
        transfer: WalletTransfer
    ) {
        val walletData = getWalletDataOrNull()
        val seqno = walletData?.seqno ?: 0
        val walletId = walletData?.subWalletId ?: DEFAULT_WALLET_ID
        val stateInit = if (walletData == null) stateInit(
            WalletV3R2Data(
                seqno,
                walletId,
                privateKey.publicKey()
            )
        ).value else null
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

    public data class WalletV3R2Data(
        val seqno: Int,
        val subWalletId: Int,
        val publicKey: PublicKeyEd25519
    ) {
        public companion object : TlbConstructor<WalletV3R2Data>(
            "wallet.v3r2.data seqno:uint32 sub_wallet_id:int32 public_key:bits256 = WalletV3R2Data"
        ) {
            override fun loadTlb(cellSlice: CellSlice): WalletV3R2Data {
                val seqno = cellSlice.loadUInt(32).toInt()
                val subWalletId = cellSlice.loadUInt(32).toInt()
                val publicKey = PublicKeyEd25519(cellSlice.loadBits(256).toByteArray().asByteString())
                return WalletV3R2Data(seqno, subWalletId, publicKey)
            }

            override fun storeTlb(cellBuilder: CellBuilder, value: WalletV3R2Data) {
                cellBuilder.storeUInt(value.seqno, 32)
                cellBuilder.storeUInt(value.subWalletId, 32)
                cellBuilder.storeBytes(value.publicKey.key.toByteArray())
            }
        }
    }

    public companion object {
        public val CODE: Cell by lazy(LazyThreadSafetyMode.PUBLICATION) {
            Cell("FF0020DD2082014C97BA218201339CBAB19F71B0ED44D0D31FD31F31D70BFFE304E0A4F2608308D71820D31FD31FD31FF82313BBF263ED44D0D31FD31FD3FFD15132BAF2A15144BAF2A204F901541055F910F2A3F8009320D74A96D307D402FB00E8D101A4C8CB1FCB1FCBFFC9ED54")
        }

        public fun address(privateKey: PrivateKeyEd25519, workchainId: Int = 0): AddrStd {
            val stateInitRef = stateInit(WalletV3R2Data(0, DEFAULT_WALLET_ID, privateKey.publicKey()))
            val hash = stateInitRef.hash()
            return AddrStd(workchainId, hash)
        }

        public fun stateInit(
            data: WalletV3R2Data
        ): CellRef<StateInit> {
            val dataCell = buildCell {
                storeTlb(WalletV3R2Data, data)
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
                Maybe.of(stateInit?.let { Either.of<StateInit, CellRef<StateInit>>(null, CellRef(it)) })
            val transferBody = createTransferMessageBody(
                privateKey,
                walletId,
                validUntil,
                seqno,
                *transfers
            )
            val body = Either.of<Cell, CellRef<Cell>>(null, CellRef(transferBody))
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
                for (gift in gifts) {
                    var sendMode = 3
                    if (gift.sendMode > -1) {
                        sendMode = gift.sendMode
                    }
                    val intMsg = CellRef(createIntMsg(gift))

                    storeUInt(sendMode, 8)
                    storeRef(MessageRelaxed.tlbCodec(AnyTlbConstructor), intMsg)
                }
            }
            val signature = BitString(privateKey.sign(unsignedBody.hash().toByteArray()))

            return CellBuilder.createCell {
                storeBits(signature)
                storeBits(unsignedBody.bits)
                storeRefs(unsignedBody.refs)
            }
        }

        private fun createIntMsg(gift: WalletTransfer): MessageRelaxed<Cell> {
            val info = when (val dest = gift.destination) {
                is MsgAddressInt -> {
                    CommonMsgInfoRelaxed.IntMsgInfoRelaxed(
                        ihrDisabled = true,
                        bounce = gift.bounceable,
                        bounced = false,
                        src = AddrNone,
                        dest = dest,
                        value = gift.coins,
                        ihrFee = Coins(),
                        fwdFee = Coins(),
                        createdLt = 0u,
                        createdAt = 0u
                    )
                }
                is MsgAddressExt -> {
                    CommonMsgInfoRelaxed.ExtOutMsgInfoRelaxed(
                        src = AddrNone,
                        dest = dest,
                        createdLt = 0u,
                        createdAt = 0u
                    )
                }
            }

            val init = Maybe.of(gift.messageData.stateInit?.let {
                Either.of<StateInit, CellRef<StateInit>>(null, it)
            })
            val bodyCell = gift.messageData.body
            val body = if (bodyCell.isEmpty()) {
                Either.of<Cell, CellRef<Cell>>(Cell.empty(), null)
            } else {
                Either.of<Cell, CellRef<Cell>>(null, CellRef(bodyCell))
            }
            return MessageRelaxed(
                info = info,
                init = init,
                body = body,
            )
        }
    }
}
