@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.block

import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.tlb.TlbCombinator
import org.ton.kotlin.tlb.TlbObject
import org.ton.kotlin.tlb.providers.TlbCombinatorProvider


@JsonClassDiscriminator("@type")
public sealed interface OutMsg : TlbObject {
    public companion object : TlbCombinatorProvider<OutMsg> by OutMsgTlbCombinator
}

private object OutMsgTlbCombinator : TlbCombinator<OutMsg>(
    OutMsg::class,
    MsgExportExt::class to MsgExportExt,
    MsgExportImm::class to MsgExportImm,
    MsgExportNew::class to MsgExportNew,
    MsgExportTr::class to MsgExportTr,
    MsgExportDeq::class to MsgExportDeq,
    MsgExportDeqShort::class to MsgExportDeqShort,
    MsgExportTrReq::class to MsgExportTrReq,
    MsgExportDeqImm::class to MsgExportDeqImm,
)
