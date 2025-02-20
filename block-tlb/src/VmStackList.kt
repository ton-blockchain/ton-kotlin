@file:Suppress("OPT_IN_USAGE", "NOTHING_TO_INLINE")

package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.cell.*
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.storeTlb
import kotlin.jvm.JvmStatic

public inline fun VmStackList(vararg stackValues: VmStackValue): VmStackList = VmStackList.of(*stackValues)
public inline fun VmStackList(stackValues: Iterable<VmStackValue>): VmStackList = VmStackList.of(stackValues)

@JsonClassDiscriminator("@type")

public sealed interface VmStackList : Iterable<VmStackValue> {
    @SerialName("vm_stk_cons")

    public data class Cons(
        val rest: VmStackList,
        val tos: VmStackValue
    ) : VmStackList {
        override fun iterator(): Iterator<VmStackValue> = ListIterator(this)
        override fun toString(): String = "(vm_stk_cons rest:$rest tos:$tos)"
    }

    @SerialName("vm_stk_nil")

    public object Nil : VmStackList {
        private val iterator = ListIterator(this)
        override fun iterator(): Iterator<VmStackValue> = iterator
        override fun toString(): String = "vm_stk_nil"
    }

    private class ListIterator(
        var vmStackList: VmStackList
    ) : Iterator<VmStackValue> {
        override fun hasNext(): Boolean = vmStackList != Nil

        override fun next(): VmStackValue {
            val list = vmStackList as Cons
            val value = list.tos
            vmStackList = list.rest
            return value
        }
    }

    public companion object {
        @JvmStatic
        public fun of(vararg stackValues: VmStackValue): VmStackList = of(stackValues.toList())

        @JvmStatic
        public fun of(stackValues: Iterable<VmStackValue>): VmStackList {
            var stackList: VmStackList = Nil
            stackValues.forEach { value ->
                stackList = Cons(stackList, value)
            }
            return stackList
        }

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        public fun tlbCodec(n: Int): TlbCodec<VmStackList> =
            when (n) {
                0 -> VmStackListNilConstructor
                else -> VmStackListConsConstructor(n)
            } as TlbCodec<VmStackList>
    }
}

private class VmStackListConsConstructor(
    n: Int
) : TlbConstructor<VmStackList.Cons>(
    schema = "vm_stk_cons#_ {n:#} rest:^(VmStackList n) tos:VmStackValue = VmStackList (n + 1);"
) {
    private val vmStackListCodec by lazy {
        VmStackList.tlbCodec(n - 1)
    }

    override fun storeTlb(
        builder: CellBuilder,
        value: VmStackList.Cons
    ) = builder {
        storeRef {
            storeTlb(vmStackListCodec, value.rest)
        }
        storeTlb(VmStackValue, value.tos)
    }

    override fun loadTlb(
        slice: CellSlice
    ): VmStackList.Cons = slice {
        val rest = loadRef {
            loadTlb(vmStackListCodec)
        }
        val tos = loadTlb(VmStackValue)
        VmStackList.Cons(rest, tos)
    }
}

private object VmStackListNilConstructor : TlbConstructor<VmStackList.Nil>(
    schema = "vm_stk_nil#_ = VmStackList 0;"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: VmStackList.Nil
    ) {
    }

    override fun loadTlb(
        slice: CellSlice
    ): VmStackList.Nil {
        return VmStackList.Nil
    }
}
