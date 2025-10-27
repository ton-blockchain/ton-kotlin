package org.ton.sdk.toncenter.model

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmName

@Serializable
public class TonCenterMasterchainInfo(
    @get:JvmName("first")
    public val first: TonCenterBlock,
    @get:JvmName("last")
    public val last: TonCenterBlock
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as TonCenterMasterchainInfo
        if (first != other.first) return false
        if (last != other.last) return false
        return true
    }

    override fun hashCode(): Int {
        var result = first.hashCode()
        result = 31 * result + last.hashCode()
        return result
    }

    override fun toString(): String = buildString {
        append("TonCenterMasterchainInfo(")
        append("first=$first, ")
        append("last=$last")
        append(")")
    }
}
