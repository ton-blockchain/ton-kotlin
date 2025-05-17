package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbCodec
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.constructor.IntTlbConstructor
import org.ton.kotlin.tlb.constructor.UIntTlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.storeTlb
import kotlin.jvm.JvmStatic

@SerialName("vm_ctl_data")

public class VmControlData(
    public val nargs: Maybe<UInt>,
    public val stack: Maybe<VmStack>,
    public val save: VmSaveList,
    public val cp: Maybe<Int>
) {
    public constructor(nargs: UInt?, stack: VmStack?, save: VmSaveList, cp: Int?) : this(
        nargs.toMaybe(),
        stack.toMaybe(),
        save,
        cp.toMaybe()
    )

    public companion object : TlbCodec<VmControlData> by VmControlDataTlbConstructor {
        @JvmStatic
        public fun tlbCodec(): TlbConstructor<VmControlData> = VmControlDataTlbConstructor
    }
}

private object VmControlDataTlbConstructor : TlbConstructor<VmControlData>(
    schema = "vm_ctl_data\$_ nargs:(Maybe uint13) stack:(Maybe VmStack) save:VmSaveList cp:(Maybe int16) = VmControlData;"
) {
    private val maybeUint13Constructor = Maybe.tlbCodec(UIntTlbConstructor.int(13))
    private val maybeVmStackConstructor = Maybe.tlbCodec(VmStack.tlbCodec())
    private val maybeInt16Constructor = Maybe.tlbCodec(IntTlbConstructor.int(16))

    @Suppress("UNCHECKED_CAST")
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: VmControlData
    ) = cellBuilder {
        storeTlb(maybeUint13Constructor, value.nargs)
        storeTlb(maybeVmStackConstructor, value.stack)
        storeTlb(VmSaveList, value.save)
        storeTlb(maybeInt16Constructor, value.cp)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmControlData = cellSlice {
        val nargs = loadTlb(maybeUint13Constructor)
        val stack = loadTlb(maybeVmStackConstructor)
        val save = loadTlb(VmSaveList)
        val cp = loadTlb(maybeInt16Constructor)
        VmControlData(nargs, stack, save, cp)
    }
}
