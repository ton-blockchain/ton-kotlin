@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.block

import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.tlb.TlbCombinator
import org.ton.kotlin.tlb.TlbObject
import org.ton.kotlin.tlb.providers.TlbCombinatorProvider


@JsonClassDiscriminator("@type")
public sealed interface ShardState : TlbObject {
    public companion object : TlbCombinatorProvider<ShardState> by ShardStateTlbCombinator
}

private object ShardStateTlbCombinator : TlbCombinator<ShardState>(
    ShardState::class,
    SplitState::class to SplitState.tlbConstructor(),
    ShardStateUnsplit::class to ShardStateUnsplit.tlbConstructor(),
)
