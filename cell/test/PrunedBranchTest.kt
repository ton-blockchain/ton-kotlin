import org.ton.sdk.cell.Cell
import org.ton.sdk.cell.CellBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

class PrunedBranchTest {
    @Test
    fun correctPrunedBranch() {
        val cell = CellBuilder()
            .storeULong(0xdeafbeaf123123, 128)
            .storeReference(Cell.EMPTY)
            .build()

        val prunedCell = CellBuilder.createPrunedBranch(cell, 1)
        assertEquals(cell.hash(), prunedCell.hash(0))
        assertEquals(cell.depth(), prunedCell.depth(0))

        val virtualCell = cell.virtualize()
        assertEquals(cell.hash(), virtualCell.hash())
        assertEquals(cell.depth(3), virtualCell.depth(3))

        val virtualPrunedBranch = CellBuilder.createPrunedBranch(virtualCell, 1)
        assertEquals(prunedCell, virtualPrunedBranch)
    }
}
