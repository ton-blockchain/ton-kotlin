@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.block

import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.tlb.TlbCombinator
import org.ton.kotlin.tlb.TlbObject
import org.ton.kotlin.tlb.providers.TlbCombinatorProvider


@JsonClassDiscriminator("@type")
public sealed interface BlockCreateStats : TlbObject {
    public companion object : TlbCombinatorProvider<BlockCreateStats> by BlockCreateStatsTlbCombinator
}

private object BlockCreateStatsTlbCombinator : TlbCombinator<BlockCreateStats>(
    BlockCreateStats::class,
    BlockCreateStatsRegular::class to BlockCreateStatsRegular.tlbConstructor(),
    BlockCreateStatsExt::class to BlockCreateStatsExt.tlbConstructor()
)
