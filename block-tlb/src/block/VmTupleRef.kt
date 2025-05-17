@file:Suppress("OPT_IN_USAGE", "NOTHING_TO_INLINE")

package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.cell.*
import org.ton.kotlin.tlb.TlbCodec
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.storeTlb
import kotlin.jvm.JvmStatic

public inline fun VmTupleRef(): VmTupleRef = VmTupleRef.of()
public inline fun VmTupleRef(entry: VmStackValue): VmTupleRef = VmTupleRef.of(entry)
public inline fun VmTupleRef(ref: VmTuple): VmTupleRef = VmTupleRef.of(ref)

@JsonClassDiscriminator("@type")

public sealed interface VmTupleRef {
    public fun depth(): Int

    public companion object {
        @JvmStatic
        public fun of(): VmTupleRef = VmTupleRefNil

        @JvmStatic
        public fun of(entry: VmStackValue): VmTupleRef = VmTupleRefSingle(entry)

        @JvmStatic
        public fun of(ref: VmTuple): VmTupleRef = VmTupleRefAny(ref)

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        public fun tlbCodec(n: Int): TlbCodec<VmTupleRef> = when (n) {
            0 -> VmTupleRefNilTlbConstructor
            1 -> VmTupleRefSingleTlbConstructor
            else -> VmTupleRefAnyTlbConstructor(n)
        } as TlbCodec<VmTupleRef>
    }
}

@SerialName("vm_tupref_nil")

public object VmTupleRefNil : VmTupleRef {
    override fun depth(): Int = 0

    override fun toString(): String = "vm_tupref_nil"
}

@SerialName("vm_tupref_single")

public data class VmTupleRefSingle(
    val entry: VmStackValue
) : VmTupleRef {
    override fun depth(): Int = 1

    override fun toString(): String = "(vm_tupref_single entry:$entry)"
}

@SerialName("vm_tupref_any")

public data class VmTupleRefAny(
    val ref: VmTuple
) : VmTupleRef {
    override fun depth(): Int = ref.depth()

    override fun toString(): String = "(vm_tupref_any ref:$ref)"
}

private object VmTupleRefNilTlbConstructor : TlbConstructor<VmTupleRefNil>(
    schema = "vm_tupref_nil\$_ = VmTupleRef 0;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder, value: VmTupleRefNil
    ) {
    }

    override fun loadTlb(cellSlice: CellSlice): VmTupleRefNil {
        return VmTupleRefNil
    }
}

private object VmTupleRefSingleTlbConstructor : TlbConstructor<VmTupleRefSingle>(
    schema = "vm_tupref_single\$_ entry:^VmStackValue = VmTupleRef 1;"
) {

    override fun storeTlb(
        cellBuilder: CellBuilder, value: VmTupleRefSingle
    ) = cellBuilder {
        storeRef {
            storeTlb(VmStackValue, value.entry)
        }
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmTupleRefSingle = cellSlice {
        val entry = loadRef {
            loadTlb(VmStackValue)
        }
        VmTupleRefSingle(entry)
    }
}

private class VmTupleRefAnyTlbConstructor(
    n: Int
) : TlbConstructor<VmTupleRefAny>(
    schema = "vm_tupref_any\$_ {n:#} ref:^(VmTuple (n + 2)) = VmTupleRef (n + 2);"
) {
    private val vmTupleCodec = VmTuple.tlbCodec(n - 2)

    override fun storeTlb(
        cellBuilder: CellBuilder, value: VmTupleRefAny
    ) = cellBuilder {
        storeRef {
            storeTlb(vmTupleCodec, value.ref)
        }
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmTupleRefAny = cellSlice {
        val ref = loadRef {
            loadTlb(vmTupleCodec)
        }
        VmTupleRefAny(ref)
    }
}
