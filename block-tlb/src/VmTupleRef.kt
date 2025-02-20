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
        builder: CellBuilder, value: VmTupleRefNil
    ) {
    }

    override fun loadTlb(slice: CellSlice): VmTupleRefNil {
        return VmTupleRefNil
    }
}

private object VmTupleRefSingleTlbConstructor : TlbConstructor<VmTupleRefSingle>(
    schema = "vm_tupref_single\$_ entry:^VmStackValue = VmTupleRef 1;"
) {

    override fun storeTlb(
        builder: CellBuilder, value: VmTupleRefSingle
    ) = builder {
        storeRef {
            storeTlb(VmStackValue, value.entry)
        }
    }

    override fun loadTlb(
        slice: CellSlice
    ): VmTupleRefSingle = slice {
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
        builder: CellBuilder, value: VmTupleRefAny
    ) = builder {
        storeRef {
            storeTlb(vmTupleCodec, value.ref)
        }
    }

    override fun loadTlb(
        slice: CellSlice
    ): VmTupleRefAny = slice {
        val ref = loadRef {
            loadTlb(vmTupleCodec)
        }
        VmTupleRefAny(ref)
    }
}
