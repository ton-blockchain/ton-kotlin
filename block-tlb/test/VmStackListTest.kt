package org.ton.block

import org.ton.cell.CellBuilder
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

@Ignore
class VmStackListTest {
    @Test
    fun `test VmStackList serialization`() {
        val vmStackList = VmStackList(VmStackTinyInt(37218))
        val depth = vmStackList.count()

        val cellBuilder = CellBuilder.beginCell()
        cellBuilder.storeTlb(VmStackList.tlbCodec(depth), vmStackList)
        val cell = cellBuilder.endCell()

        val cellSlice = cell.beginParse()
        val newVmStackList = cellSlice.loadTlb(VmStackList.tlbCodec(depth))

        assertEquals(vmStackList, newVmStackList)
    }
}
