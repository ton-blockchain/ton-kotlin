@file:Suppress("NOTHING_TO_INLINE")

package org.ton.block

import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbCombinatorProvider
import kotlin.jvm.JvmStatic

public inline fun SmcCapList(capabilities: Iterable<SmcCapability>): SmcCapList = SmcCapList.of(capabilities)
public inline fun SmcCapList(vararg capabilities: SmcCapability): SmcCapList = SmcCapList.of(capabilities.toList())

public inline fun Iterable<SmcCapability>.toSmcCapList(): SmcCapList = SmcCapList.of(this)
public inline fun Array<SmcCapability>.toSmcCapList(): SmcCapList = SmcCapList.of(*this)

public sealed interface SmcCapList : Iterable<SmcCapability> {
    public object Nil : SmcCapList {
        override fun iterator(): Iterator<SmcCapability> = iterator {}
    }

    public data class Next(
        val head: SmcCapability,
        val tail: SmcCapList
    ) : SmcCapList {
        override fun iterator(): Iterator<SmcCapability> = iterator {
            yield(head)
            yieldAll(tail)
        }
    }

    public companion object : TlbCombinatorProvider<SmcCapList> by SmcCapListTlbCombinator {
        @JvmStatic
        public fun of(capability: Iterable<SmcCapability>): SmcCapList =
            capability.reversed().fold(Nil as SmcCapList) { acc, cap ->
                Next(cap, acc)
            }

        @JvmStatic
        public fun of(vararg capability: SmcCapability): SmcCapList = of(capability.asIterable())
    }
}

private object SmcCapListTlbCombinator : TlbCombinator<SmcCapList>(
    SmcCapList::class,
    SmcCapList.Nil::class to capListNil,
    SmcCapList.Next::class to SmcCapListNextTlbConstructor,
)

private val capListNil = ObjectTlbConstructor(
    SmcCapList.Nil,
    schema = "cap_list_nil\$0 = SmcCapList;",
)

private object SmcCapListNextTlbConstructor : TlbConstructor<SmcCapList.Next>(
    schema = "cap_list_next\$1 head:SmcCapability tail:SmcCapList = SmcCapList;"
) {
    override fun storeTlb(cellBuilder: CellBuilder, value: SmcCapList.Next) {
        cellBuilder.storeTlb(SmcCapability, value.head)
        cellBuilder.storeTlb(SmcCapList, value.tail)
    }

    override fun loadTlb(cellSlice: CellSlice): SmcCapList.Next {
        val head = cellSlice.loadTlb(SmcCapability)
        val tail = cellSlice.loadTlb(SmcCapList)
        return SmcCapList.Next(head, tail)
    }
}
