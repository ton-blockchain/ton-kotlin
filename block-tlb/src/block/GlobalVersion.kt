package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor

@SerialName("capabilities")

public data class GlobalVersion(
    val version: UInt, // version : uint32
    val capabilities: ULong // capabilities : uint64
) : TlbObject {

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("capabilities") {
        field("version", version)
        field("capabilities", capabilities)
    }

    override fun toString(): String = print().toString()

    public companion object : TlbCodec<GlobalVersion> by GlobalVersionTlbConstructor.asTlbCombinator()
}

private object GlobalVersionTlbConstructor : TlbConstructor<GlobalVersion>(
    schema = "capabilities#c4 version:uint32 capabilities:uint64 = GlobalVersion;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: GlobalVersion
    ) = cellBuilder {
        storeUInt(value.version.toInt(), 32)
        storeUInt(value.capabilities.toLong(), 64)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): GlobalVersion = cellSlice {
        val version = loadUInt(32).toInt().toUInt()
        val capabilities = loadUInt(64).toLong().toULong()
        GlobalVersion(version, capabilities)
    }
}
