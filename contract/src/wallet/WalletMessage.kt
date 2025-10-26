package org.ton.contract.wallet

import org.ton.bigint.toInt
import org.ton.block.MessageRelaxed
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.*
import kotlin.jvm.JvmStatic

public interface WalletMessage<X : Any> {
    public val mode: Int
    public val msg: CellRef<MessageRelaxed<X>>

    public fun loadMsg(): MessageRelaxed<X> = msg.value

    public companion object {
        @JvmStatic
        public fun <X : Any> of(mode: Int, msg: CellRef<MessageRelaxed<X>>): WalletMessage<X> =
            WalletMessageImpl(mode, msg)

        @JvmStatic
        public fun <X : Any> tlbCodec(x: TlbCodec<X>): TlbCodec<WalletMessage<X>> =
            WalletMessageTlbConstructor(x)
    }
}

public inline fun <X : Any> WalletMessage(mode: Int, msg: CellRef<MessageRelaxed<X>>): WalletMessage<X> =
    WalletMessage.of(mode, msg)

private data class WalletMessageImpl<X : Any>(
    override val mode: Int,
    override val msg: CellRef<MessageRelaxed<X>>
) : WalletMessage<X>

private class WalletMessageTlbConstructor<X : Any>(
    x: TlbCodec<X>
) : TlbConstructor<WalletMessage<X>>(
    schema = "wallet_message#_ mode:uint8 msg:(MessageRelaxed X) = WalletMessage X"
) {
    val messageRelaxedX = MessageRelaxed.tlbCodec(x)

    override fun loadTlb(cellSlice: CellSlice): WalletMessage<X> {
        val mode = cellSlice.loadInt(8).toInt()
        val msg = cellSlice.loadRef(messageRelaxedX)
        return WalletMessageImpl(mode, msg)
    }

    override fun storeTlb(cellBuilder: CellBuilder, value: WalletMessage<X>) {
        cellBuilder.storeInt(value.mode, 8)
        cellBuilder.storeRef(messageRelaxedX, value.msg)
    }
}
