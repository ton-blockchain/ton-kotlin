package org.ton.api.dht

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.TlCodec
import org.ton.tl.TlObject
import org.ton.tl.constructors.EnumTlCombinator

@Serializable
public enum class DhtUpdateRule : TlObject<DhtUpdateRule> {
    @SerialName("dht.updateRule.signature")
    SIGNATURE,

    @SerialName("dht.updateRule.anybody")
    ANYBODY,

    @SerialName("dht.updateRule.overlayNodes")
    OVERLAY_NODES;

    override fun tlCodec(): TlCodec<DhtUpdateRule> = Companion

    public companion object : EnumTlCombinator<DhtUpdateRule>(
        DhtUpdateRule::class,
        SIGNATURE to "dht.updateRule.signature = dht.UpdateRule",
        ANYBODY to "dht.updateRule.anybody = dht.UpdateRule",
        OVERLAY_NODES to "dht.updateRule.overlayNodes = dht.UpdateRule"
    )
}
