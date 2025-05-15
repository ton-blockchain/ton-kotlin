package org.ton.kotlin.block

import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.Cell
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.message.MessageLayout
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.constructor.AnyTlbConstructor
import kotlin.jvm.JvmStatic


public data class Message<X>(
    val info: CommonMsgInfo,
    val init: Maybe<Either<StateInit, CellRef<StateInit>>>,
    val body: Either<X, CellRef<X>>
) : TlbObject {
    public constructor(
        info: CommonMsgInfo,
        init: StateInit?,
        body: X,
        bodyCodec: TlbCodec<X>,
        layout: MessageLayout
    ) : this(
        info = info,
        init = layout.eitherInit(init).toMaybe(),
        body = layout.eitherBody(body, bodyCodec),
    )

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return printer.type("message") {
            field("info", info)
            field("init", init)
            field("body", body)
        }
    }

    override fun toString(): String = print().toString()

    public companion object {
        public val Any: TlbConstructor<Message<Cell>> = tlbCodec(AnyTlbConstructor)

        @JvmStatic
        public fun <X : Any> tlbCodec(
            x: TlbCodec<X>
        ): TlbConstructor<Message<X>> = MessageTlbConstructor(x)
    }
}

public operator fun <X : Any> Message.Companion.invoke(x: TlbCodec<X>): TlbConstructor<Message<X>> = tlbCodec(x)

private class MessageTlbConstructor<X : Any>(
    x: TlbCodec<X>
) : TlbConstructor<Message<X>>(
    schema = "message\$_ {X:Type} info:CommonMsgInfo " +
            "init:(Maybe (Either StateInit ^StateInit)) " +
            "body:(Either X ^X) = Message X;",
    id = BitString.empty()
) {
    private val eitherXX = Either(x, CellRef.tlbCodec(x))

    override fun storeTlb(
        cellBuilder: CellBuilder, value: Message<X>
    ) = cellBuilder {
        storeTlb(CommonMsgInfo, value.info)
        storeTlb(maybeEitherStateInitStateInit, value.init)
        storeTlb(eitherXX, value.body)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): Message<X> = cellSlice {
        val info = loadTlb(CommonMsgInfo)
        val init = loadTlb(maybeEitherStateInitStateInit)
        val body = loadTlb(eitherXX)
        Message(info, init, body)
    }

    companion object {
        private val maybeEitherStateInitStateInit =
            Maybe.tlbCodec(Either.tlbCodec(StateInit, CellRef.tlbCodec(StateInit)))
    }
}
