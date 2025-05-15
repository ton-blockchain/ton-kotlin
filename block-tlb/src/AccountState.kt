@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.block

import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.tlb.TlbCombinator
import org.ton.kotlin.tlb.TlbObject
import org.ton.kotlin.tlb.providers.TlbCombinatorProvider


@JsonClassDiscriminator("@type")
public sealed interface AccountState : TlbObject {
    /**
     * Account status.
     */
    public val status: AccountStatus

    public companion object : TlbCombinatorProvider<AccountState> by AccountStateTlbCombinator
}

private object AccountStateTlbCombinator : TlbCombinator<AccountState>(
    AccountState::class,
    AccountUninit::class to AccountUninit,
    AccountActive::class to AccountActive,
    AccountFrozen::class to AccountFrozen
)
