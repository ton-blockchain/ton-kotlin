@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.block

import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.tlb.TlbCombinator
import org.ton.kotlin.tlb.TlbObject
import org.ton.kotlin.tlb.providers.TlbCombinatorProvider

@JsonClassDiscriminator("@type")

public sealed interface InMsg : TlbObject {
    public companion object : TlbCombinatorProvider<InMsg> by InMsgTlbCombinator
}

private object InMsgTlbCombinator : TlbCombinator<InMsg>(
    InMsg::class,
    MsgImportExt::class to MsgImportExt,
    MsgImportIhr::class to MsgImportIhr,
    MsgImportImm::class to MsgImportImm,
    MsgImportFin::class to MsgImportFin,
    MsgImportTr::class to MsgImportTr,
    MsgDiscardFin::class to MsgDiscardFin,
    MsgDiscardTr::class to MsgDiscardTr,
)
