package org.ton.contract.wallet

import org.ton.api.pub.PublicKey
import org.ton.block.*
import org.ton.cell.Cell
import org.ton.kotlin.account.StateInit
import org.ton.kotlin.message.MessageLayout
import org.ton.kotlin.message.address.AddrExtern
import org.ton.kotlin.message.address.MsgAddress
import org.ton.tlb.CellRef
import org.ton.tlb.constructor.AnyTlbConstructor
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmStatic

public data class WalletTransfer internal constructor(
    val destination: MsgAddress,
    val bounceable: Boolean,
    val coins: CurrencyCollection,
    val sendMode: Int,
    val messageData: MessageData
) {
    val msgInfo: CommonMsgInfoRelaxed = when (destination) {
        is org.ton.kotlin.message.address.MsgAddressInt -> {
            CommonMsgInfoRelaxed.IntMsgInfoRelaxed(
                ihrDisabled = true,
                bounce = bounceable,
                bounced = false,
                src = null,
                dest = destination,
                value = coins,
                ihrFee = Coins(),
                fwdFee = Coins(),
                createdLt = 0u,
                createdAt = 0u
            )
        }

        is AddrExtern -> {
            CommonMsgInfoRelaxed.ExtOutMsgInfoRelaxed(
                src = null,
                dest = destination,
                createdLt = 0u,
                createdAt = 0u
            )
        }
    }

    public fun toMessageRelaxed(layout: MessageLayout? = null): MessageRelaxed<Cell> {
        val init = messageData.stateInit?.value
        val body = messageData.body
        return MessageRelaxed(
            info = msgInfo,
            init = init,
            body = body,
            bodyCodec = AnyTlbConstructor,
            layout = layout ?: MessageLayout.compute(msgInfo, init, body, AnyTlbConstructor)
        )
    }
}

public class WalletTransferBuilder {
    public lateinit var destination: MsgAddressInt
    public var bounceable: Boolean = true
    public var currencyCollection: CurrencyCollection = CurrencyCollection.ZERO
    public var coins: Coins
        get() = currencyCollection.coins
        set(value) {
            currencyCollection = currencyCollection.copy(coins = value)
        }
    public var sendMode: Int = 3
    public var messageData: MessageData = MessageData.Raw(Cell.empty(), null, MessageLayout.PLAIN)

    public fun build(): WalletTransfer =
        WalletTransfer(destination, bounceable, currencyCollection, sendMode, messageData)
}

public inline fun WalletTransfer(builderAction: WalletTransferBuilder.() -> Unit): WalletTransfer {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return WalletTransferBuilder().apply(builderAction).build()
}

public sealed interface MessageData {
    public val body: Cell
    public val stateInit: CellRef<StateInit>?
    public val layout: MessageLayout?

    public data class Raw(
        public override val body: Cell,
        public override val stateInit: CellRef<StateInit>?,
        override val layout: MessageLayout?,
    ) : MessageData

    public data class Text(
        public val text: CellRef<MessageText>,
        override val layout: MessageLayout? = null
    ) : MessageData {
        public constructor(text: MessageText) : this(CellRef(text, MessageText))

        override val body: Cell get() = text.cell
        override val stateInit: CellRef<StateInit>? get() = null
    }

    public companion object {
        @JvmStatic
        public fun raw(body: Cell, stateInit: CellRef<StateInit>? = null): Raw =
            Raw(body, stateInit, null)

        @JvmStatic
        public fun text(text: String): Text = Text(
            MessageText.Raw(text)
        )

        @JvmStatic
        public fun encryptedText(publicKey: PublicKey, text: String): Text = Text(
            MessageText.Raw(text).encrypt(publicKey)
        )
    }
}
