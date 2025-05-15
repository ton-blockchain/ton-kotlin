package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider

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
        cellBuilder: CellBuilder, value: StorageUsed
    ) = cellBuilder {
        storeTlb(varUInteger7, value.cells)
        storeTlb(varUInteger7, value.bits)
        storeTlb(varUInteger7, value.publicCells)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): StorageUsed = cellSlice {
        val cells = loadTlb(varUInteger7)
        val bits = loadTlb(varUInteger7)
        val publicCells = loadTlb(varUInteger7)
        StorageUsed(cells, bits, publicCells)
    }
}
