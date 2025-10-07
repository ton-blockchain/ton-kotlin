@file:Suppress("OPT_IN_USAGE")

package org.ton.api.dht.config

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.tl.TlCodec
import org.ton.tl.TlCombinator
import org.ton.tl.TlObject

@Polymorphic
@Serializable
@JsonClassDiscriminator("@type")
public sealed interface DhtConfigLocal : TlObject<DhtConfigLocal> {
    override fun tlCodec(): TlCodec<out DhtConfigLocal> = Companion

    public companion object : TlCombinator<DhtConfigLocal>(
        DhtConfigLocal::class,
        DhtConfigRandomLocal::class to DhtConfigRandomLocal.Companion,
        DhtConfigIdLocal::class to DhtConfigIdLocal.Companion,
    )
}
