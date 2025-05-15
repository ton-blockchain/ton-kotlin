package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.TlbObject
import org.ton.kotlin.tlb.TlbPrettyPrinter
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


public data class KeyMaxLt(
    val key: Boolean,
    @SerialName("max_end_lt") val maxEndLt: ULong,
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("key_max_lt") {
        field("key", key)
        field("max_end_lt", maxEndLt)
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<KeyMaxLt> by KeyMaxLtTlbConstructor
}

private object KeyMaxLtTlbConstructor : TlbConstructor<KeyMaxLt>(
    schema = "_ key:Bool max_end_lt:uint64 = KeyMaxLt;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: KeyMaxLt
    ) = cellBuilder {
        storeBit(value.key)
        storeUInt64(value.maxEndLt)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): KeyMaxLt = cellSlice {
        val key = loadBit()
        val maxEndLt = loadUInt64()
        KeyMaxLt(key, maxEndLt)
    }
}
