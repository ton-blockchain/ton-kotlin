package org.ton.sdk.toncenter.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmName

@Serializable
public class TonCenterBlocksResponse(
    @get:JvmName("blocks")
    public val blocks: List<TonCenterBlock>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as TonCenterBlocksResponse
        return blocks == other.blocks
    }

    override fun hashCode(): Int = blocks.hashCode()

    override fun toString(): String = buildString {
        append("TonCenterBlocksResponse(")
        append("blocks=$blocks")
        append(")")
    }
}
