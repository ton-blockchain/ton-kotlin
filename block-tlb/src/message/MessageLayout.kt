@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.message

import org.ton.block.*
import org.ton.block.Either.Left
import org.ton.block.Either.Right
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.kotlin.cell.CellContext
import org.ton.tlb.CellRef
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbStorer
import org.ton.tlb.storeTlb

/**
 * Message payload layout.
 */
public data class MessageLayout(
    /**
     * Whether to store state init in a child cell.
     */
    val initToCell: Boolean,

    /**
     * Whether to store payload as a child cell.
     */
    val bodyToCell: Boolean,
) {
    public fun eitherInit(init: StateInit?): Either<StateInit, CellRef<StateInit>>? {
        if (init == null) return null
        return if (initToCell) {
            Right(CellRef(init, StateInit))
        } else {
            Left(init)
        }
    }

    public fun <T> eitherBody(body: T, bodyCodec: TlbCodec<T>): Either<T, CellRef<T>> {
        return if (bodyToCell) Right(CellRef(body, bodyCodec)) else Left(body)
    }

    public companion object {
        /**
         * Plain message layout (init and body stored in the root cell).
         */
        public val PLAIN: MessageLayout = MessageLayout(initToCell = false, bodyToCell = false)

        private val LAYOUTS = arrayOf(
            PLAIN,
            MessageLayout(initToCell = false, bodyToCell = true),
            MessageLayout(initToCell = true, bodyToCell = false),
        )

        public fun <T : Any> compute(
            info: CommonMsgInfoRelaxed,
            init: StateInit?,
            body: T,
            bodyStorer: TlbStorer<T>
        ): MessageLayout {
            val bodyCodec = object : TlbCodec<T> {
                override fun storeTlb(builder: CellBuilder, value: T, context: CellContext) =
                    bodyStorer.storeTlb(builder, value, context)

                override fun loadTlb(slice: CellSlice, context: CellContext): T = throw UnsupportedOperationException()
            }
            val messageCodec = MessageRelaxed.tlbCodec(bodyCodec)
            val builder = CellBuilder()
            for (layout in LAYOUTS) {
                builder.reset()
                val result = runCatching {
                    builder.storeTlb(messageCodec, MessageRelaxed(info, init, body, bodyCodec, layout))
                    check(builder.remainingBits >= 0 && builder.refs.size <= 4)
                }
                if (result.isSuccess) {
                    return layout
                }
            }
            return MessageLayout(initToCell = true, bodyToCell = true)
        }

        public fun <T : Any> compute(
            info: CommonMsgInfo,
            init: StateInit?,
            body: T,
            bodyStorer: TlbStorer<T>
        ): MessageLayout {
            val bodyCodec = object : TlbCodec<T> {
                override fun storeTlb(builder: CellBuilder, value: T, context: CellContext) =
                    bodyStorer.storeTlb(builder, value, context)

                override fun loadTlb(slice: CellSlice): T = throw UnsupportedOperationException()
            }
            val messageCodec = Message.tlbCodec(bodyCodec)
            val builder = CellBuilder()
            for (layout in LAYOUTS) {
                builder.reset()
                val result = runCatching {
                    builder.storeTlb(messageCodec, Message(info, init, body, bodyCodec, layout))
                    check(builder.remainingBits >= 0 && builder.refs.size <= 4)
                }
                if (result.isSuccess) {
                    return layout
                }
            }
            return MessageLayout(initToCell = true, bodyToCell = true)
        }
    }
}
