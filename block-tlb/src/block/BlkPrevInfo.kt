@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.block

import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.tlb.TlbCodec
import org.ton.kotlin.tlb.TlbObject
import kotlin.jvm.JvmStatic

@JsonClassDiscriminator("@type")

public sealed interface BlkPrevInfo : TlbObject {
    public companion object {
        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        public fun tlbCodec(multiple: Boolean): TlbCodec<BlkPrevInfo> =
            (if (multiple) PrevBlksInfo else PrevBlkInfo) as TlbCodec<BlkPrevInfo>

        @JvmStatic
        public fun tlbCodec(multiple: Int): TlbCodec<BlkPrevInfo> =
            tlbCodec(multiple != 0)
    }
}
