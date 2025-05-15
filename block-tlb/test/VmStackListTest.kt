package org.ton.kotlin.block

import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.storeTlb
import kotlin.test.Test
import kotlin.test.assertEquals

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
