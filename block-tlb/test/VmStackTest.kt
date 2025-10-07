package org.ton.block

import org.ton.cell.CellBuilder
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

@Ignore
class VmStackTest {
    @Test
    fun `test VmStack serialization with VmStackList-Nil`() {
        val stack = VmStack(VmStackList.Nil)
        val cell = CellBuilder.createCell {
            storeTlb(VmStack.tlbCodec(), stack)
        }
        val stack2 = cell.parse {
            loadTlb(VmStack.tlbCodec())
        }
        assertEquals(stack, stack2)
    }

    @Test
    fun `test VmStack serialization with VmStackList-Single`() {
        val stack = VmStack(
            VmStackList.Cons(
                VmStackList.Cons(
                    VmStackList.Nil, VmStackTinyInt(1313)
                ), VmStackInt(12)
            )
        )
        val cell = CellBuilder.createCell {
            storeTlb(VmStack.tlbCodec(), stack)
        }
        val stack2 = cell.parse {
            loadTlb(VmStack.tlbCodec())
        }
        assertEquals(stack, stack2)
    }
}
