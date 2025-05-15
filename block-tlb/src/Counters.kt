package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.TlbObject
import org.ton.kotlin.tlb.TlbPrettyPrinter
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


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
        cellBuilder: CellBuilder,
        value: Counters
    ) = cellBuilder {
        storeUInt32(value.lastUpdated)
        storeUInt64(value.total)
        storeUInt64(value.cnt2048)
        storeUInt64(value.cnt65536)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): Counters = cellSlice {
        val lastUpdated = loadUInt32()
        val total = loadUInt64()
        val cnt2048 = loadUInt64()
        val cnt65535 = loadUInt64()
        Counters(lastUpdated, total, cnt2048, cnt65535)
    }
}
