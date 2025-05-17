@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.block

import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.tlb.TlbCodec
import org.ton.kotlin.tlb.TlbCombinator
import org.ton.kotlin.tlb.TlbObject
import kotlin.jvm.JvmStatic

@JsonClassDiscriminator("@type")

public sealed interface CommonMsgInfo : TlbObject {
    public companion object : TlbCodec<CommonMsgInfo> by CommonMsgInfoTlbCombinator {
        @JvmStatic
        public fun tlbCodec(): TlbCombinator<CommonMsgInfo> = CommonMsgInfoTlbCombinator
    }
}

private object CommonMsgInfoTlbCombinator : TlbCombinator<CommonMsgInfo>(
    CommonMsgInfo::class,
    IntMsgInfo::class to IntMsgInfo,
    ExtInMsgInfo::class to ExtInMsgInfo,
    ExtOutMsgInfo::class to ExtOutMsgInfo
)

public val CommonMsgInfo?.value: CurrencyCollection get() = (this as? IntMsgInfo)?.value ?: CurrencyCollection.ZERO
