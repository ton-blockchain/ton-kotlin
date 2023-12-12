package org.ton.block

import kotlinx.serialization.Serializable
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.*
import org.ton.tlb.constructor.tlbCodec
import kotlin.jvm.JvmStatic

@Serializable
public data class MessageRelaxed<X>(
    val info: CommonMsgInfoRelaxed,
    val init: Maybe<Either<StateInit, CellRef<StateInit>>>,
    val body: Either<X, CellRef<X>>
) : TlbObject {
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
        cellBuilder: CellBuilder, value: MessageRelaxed<X>
    ) = cellBuilder {
        storeTlb(CommonMsgInfoRelaxed, value.info)
        storeTlb(maybeEitherCodec, value.init)
        storeTlb(eitherXCodec, value.body)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): MessageRelaxed<X> = cellSlice {
        val info = loadTlb(CommonMsgInfoRelaxed)
        val init = loadTlb(maybeEitherCodec)
        val body = loadTlb(eitherXCodec)
        MessageRelaxed(info, init, body)
    }
}
