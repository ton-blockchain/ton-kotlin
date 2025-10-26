@file:Suppress("OPT_IN_USAGE")

package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.bigint.toInt
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbCombinatorProvider

@JsonClassDiscriminator("@type")

public sealed interface VmCont {

    @SerialName("vmc_until")

    public data class Until(
        val body: CellRef<VmCont>,
        val after: CellRef<VmCont>
    ) : VmCont

    @SerialName("vmc_again")

    public data class Again(
        val body: CellRef<VmCont>
    ) : VmCont

    @SerialName("vmc_while_cond")

    public data class WhileCond(
        val cond: CellRef<VmCont>,
        val body: CellRef<VmCont>,
        val after: CellRef<VmCont>
    ) : VmCont

    @SerialName("vmc_while_body")

    public data class WhileBody(
        val cond: CellRef<VmCont>,
        val body: CellRef<VmCont>,
        val after: CellRef<VmCont>
    ) : VmCont

    @SerialName("vmc_pushint")

    public data class PushInt(
        val value: Int,
        val next: CellRef<VmCont>
    ) : VmCont

    public companion object : TlbCombinatorProvider<VmCont> by VmContTlbCombinator
}

private object VmContTlbCombinator : TlbCombinator<VmCont>(
    VmCont::class,
    VmContStd::class to VmContStd.tlbCodec(),
    VmContEnvelope::class to VmContEnvelope.tlbCodec(),
    VmContQuit::class to VmContQuit.tlbConstructor(),
    VmContQuitExc::class to VmContQuitExc.tlbConstructor(),
    VmContRepeat::class to VmContRepeat.tlbConstructor(),
    VmCont.Until::class to VmContUntilTlbConstructor,
    VmCont.Again::class to VmContAgainTlbConstructor,
    VmCont.WhileCond::class to VmContWhileCondTlbConstructor,
    VmCont.WhileBody::class to VmContWhileBodyTlbConstructor,
    VmCont.PushInt::class to VmContPushIntTlbConstructor
)

private object VmContUntilTlbConstructor : TlbConstructor<VmCont.Until>(
    schema = "vmc_until\$110000 body:^VmCont after:^VmCont = VmCont;"
) {
    private val vmContCodec = CellRef.tlbCodec(VmCont)

    override fun storeTlb(
        cellBuilder: CellBuilder, value: VmCont.Until
    ) = cellBuilder {
        storeTlb(vmContCodec, value.body)
        storeTlb(vmContCodec, value.after)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmCont.Until = cellSlice {
        val body = loadTlb(vmContCodec)
        val after = loadTlb(vmContCodec)
        VmCont.Until(body, after)
    }
}

private object VmContAgainTlbConstructor : TlbConstructor<VmCont.Again>(
    schema = "vmc_again\$110001 body:^VmCont = VmCont;"
) {
    private val vmContCodec = CellRef.tlbCodec(VmCont)

    override fun storeTlb(
        cellBuilder: CellBuilder, value: VmCont.Again
    ) = cellBuilder {
        storeTlb(vmContCodec, value.body)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmCont.Again = cellSlice {
        val body = loadTlb(vmContCodec)
        VmCont.Again(body)
    }
}

private object VmContWhileCondTlbConstructor : TlbConstructor<VmCont.WhileCond>(
    schema = "vmc_while_cond\$110010 cond:^VmCont body:^VmCont after:^VmCont = VmCont;"
) {
    private val vmContCodec = CellRef.tlbCodec(VmCont)

    override fun storeTlb(
        cellBuilder: CellBuilder, value: VmCont.WhileCond
    ) = cellBuilder {
        storeTlb(vmContCodec, value.cond)
        storeTlb(vmContCodec, value.body)
        storeTlb(vmContCodec, value.after)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmCont.WhileCond = cellSlice {
        val cond = loadTlb(vmContCodec)
        val body = loadTlb(vmContCodec)
        val after = loadTlb(vmContCodec)
        VmCont.WhileCond(cond, body, after)
    }
}

private object VmContWhileBodyTlbConstructor : TlbConstructor<VmCont.WhileBody>(
    schema = "vmc_while_body\$110011 cond:^VmCont body:^VmCont after:^VmCont = VmCont;"
) {
    private val vmContCodec = CellRef.tlbCodec(VmCont)

    override fun storeTlb(
        cellBuilder: CellBuilder, value: VmCont.WhileBody
    ) = cellBuilder {
        storeTlb(vmContCodec, value.cond)
        storeTlb(vmContCodec, value.body)
        storeTlb(vmContCodec, value.after)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmCont.WhileBody = cellSlice {
        val cond = loadTlb(vmContCodec)
        val body = loadTlb(vmContCodec)
        val after = loadTlb(vmContCodec)
        VmCont.WhileBody(cond, body, after)
    }
}

private object VmContPushIntTlbConstructor : TlbConstructor<VmCont.PushInt>(
    schema = "vmc_pushint\$1111 value:int32 next:^VmCont = VmCont;"
) {
    private val vmContCodec = CellRef.tlbCodec(VmCont)

    override fun storeTlb(
        cellBuilder: CellBuilder, value: VmCont.PushInt
    ) = cellBuilder {
        storeInt(value.value, 32)
        storeTlb(vmContCodec, value.next)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): VmCont.PushInt = cellSlice {
        val value = loadInt(32).toInt()
        val next = loadTlb(vmContCodec)
        VmCont.PushInt(value, next)
    }
}
