package org.ton.block

import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.kotlin.account.StateInit
import org.ton.kotlin.message.MessageLayout
import org.ton.tlb.*
import org.ton.tlb.constructor.tlbCodec
import kotlin.jvm.JvmStatic

public data class MessageRelaxed<X>(
    val info: CommonMsgInfoRelaxed,
    val init: Maybe<Either<StateInit, CellRef<StateInit>>>,
    val body: Either<X, CellRef<X>>
) : TlbObject {
    public constructor(
        info: CommonMsgInfoRelaxed,
        init: StateInit?,
        body: X,
        bodyCodec: TlbCodec<X>,
        layout: MessageLayout
    ) : this(
        info = info,
        init = layout.eitherInit(init).toMaybe(),
        body = layout.eitherBody(body, bodyCodec),
    )

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("message") {
            field("info", info)
            field("init", init)
            field("body", body)
        }
    }

    override fun toString(): String = print().toString()

    public companion object {
        @JvmStatic
        public fun <X : Any> tlbCodec(
            x: TlbCodec<X>
        ): TlbConstructor<MessageRelaxed<X>> = MessageRelaxedTlbConstructor(x)
    }
}

private class MessageRelaxedTlbConstructor<X : Any>(
    x: TlbCodec<X>
) : TlbConstructor<MessageRelaxed<X>>(
    schema = "message\$_ {X:Type} info:CommonMsgInfoRelaxed " +
            "init:(Maybe (Either StateInit ^StateInit)) " +
            "body:(Either X ^X) = MessageRelaxed X;"
) {
    companion object {
        private val referencedStateInitCodec = Cell.tlbCodec(StateInit)
        private val eitherStateInitCodec = Either.tlbCodec(StateInit, CellRef(referencedStateInitCodec))
        private val maybeEitherCodec = Maybe.tlbCodec(eitherStateInitCodec)
    }

    private val eitherXCodec = Either.tlbCodec(x, CellRef(x))

    override fun storeTlb(
        builder: CellBuilder, value: MessageRelaxed<X>
    ) = builder {
        storeTlb(CommonMsgInfoRelaxed, value.info)
        storeTlb(maybeEitherCodec, value.init)
        storeTlb(eitherXCodec, value.body)
    }

    override fun loadTlb(
        slice: CellSlice
    ): MessageRelaxed<X> = slice {
        val info = loadTlb(CommonMsgInfoRelaxed)
        val init = loadTlb(maybeEitherCodec)
        val body = loadTlb(eitherXCodec)
        MessageRelaxed(info, init, body)
    }
}
