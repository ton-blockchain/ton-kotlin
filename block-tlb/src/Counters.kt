package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbObject
import org.ton.tlb.TlbPrettyPrinter
import org.ton.tlb.providers.TlbConstructorProvider


@SerialName("counters")
public data class Counters(
    @SerialName("last_updated") val lastUpdated: UInt, // last_updated : uint32
    val total: ULong, // total : uint64
    val cnt2048: ULong, // cnt2048 : uint64
    val cnt65536: ULong // cnt65536 : uint64
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return printer.type("counters") {
            field("last_updated", lastUpdated)
            field("total", total)
            field("cnt2048", cnt2048)
            field("cnt65536", cnt65536)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<Counters> by CounterTlbConstructor
}

private object CounterTlbConstructor : TlbConstructor<Counters>(
    schema = "counters#_ last_updated:uint32 total:uint64 cnt2048:uint64 cnt65536:uint64 = Counters;\n"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: Counters
    ) = builder {
        storeUInt32(value.lastUpdated)
        storeULong(value.total)
        storeULong(value.cnt2048)
        storeULong(value.cnt65536)
    }

    override fun loadTlb(
        slice: CellSlice
    ): Counters = slice {
        val lastUpdated = loadUInt32()
        val total = loadULong()
        val cnt2048 = loadULong()
        val cnt65535 = loadULong()
        Counters(lastUpdated, total, cnt2048, cnt65535)
    }
}
