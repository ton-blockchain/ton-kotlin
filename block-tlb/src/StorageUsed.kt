package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider

@SerialName("storage_used")

public data class StorageUsed(
    val cells: VarUInteger,
    val bits: VarUInteger,
    @SerialName("public_cells") val publicCells: VarUInteger,
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return printer.type("storage_used") {
            field("cells", cells)
            field("bits", bits)
            field("public_cells", publicCells)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<StorageUsed> by StorageUsedTlbConstructor
}

private object StorageUsedTlbConstructor : TlbConstructor<StorageUsed>(
    schema = "storage_used\$_ cells:(VarUInteger 7) bits:(VarUInteger 7) public_cells:(VarUInteger 7) = StorageUsed;"
) {
    private val varUInteger7 = VarUInteger.tlbCodec(7)

    override fun storeTlb(
        builder: CellBuilder, value: StorageUsed
    ) = builder {
        storeTlb(varUInteger7, value.cells)
        storeTlb(varUInteger7, value.bits)
        storeTlb(varUInteger7, value.publicCells)
    }

    override fun loadTlb(
        slice: CellSlice
    ): StorageUsed = slice {
        val cells = loadTlb(varUInteger7)
        val bits = loadTlb(varUInteger7)
        val publicCells = loadTlb(varUInteger7)
        StorageUsed(cells, bits, publicCells)
    }
}
