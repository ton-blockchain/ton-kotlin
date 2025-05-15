package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.TlbObject
import org.ton.kotlin.tlb.TlbPrettyPrinter
import org.ton.kotlin.tlb.providers.TlbConstructorProvider

@Suppress("NOTHING_TO_INLINE")
public inline fun Pair<Boolean, Boolean>.toTickTock(): TickTock = TickTock(first, second)

@SerialName("tick_tock")

public data class TickTock(
    val tick: Boolean,
    val tock: Boolean
) : TlbObject {
    public fun toPair(): Pair<Boolean, Boolean> = tick to tock

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("tick_tock") {
            field("tick", tick)
            field("tick", tock)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<TickTock> by TickTockTlbConstructor
}

private object TickTockTlbConstructor : TlbConstructor<TickTock>(
    schema = "tick_tock\$_ tick:Bool tock:Bool = TickTock;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder, value: TickTock
    ) = cellBuilder {
        storeBit(value.tick)
        storeBit(value.tock)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): TickTock = cellSlice {
        val tick = loadBit()
        val tock = loadBit()
        TickTock(tick, tock)
    }
}
