package org.ton.sdk.blockchain

import kotlin.jvm.JvmField
import kotlin.jvm.JvmName

/**
 * Represents a shard identifier in the TON blockchain.
 *
 * A [ShardId] is defined by its workchain identifier and its prefix, which collectively specify a
 * distinct shard in the blockchain hierarchy. It also provides information about whether the shard
 * belongs to the masterchain.
 *
 * @property workchain The numerical identifier of the workchain. The value `-1` represents the masterchain.
 * @property prefix The identifier prefix of the shard within its workchain, represented as an unsigned 64-bit integer.
 */
public class ShardId(
    @get:JvmName("workchain") public val workchain: Int,
    @get:JvmName("prefix") public val prefix: ULong
) {
    /**
     * Indicates if the shard belongs to the masterchain (workchain equals `-1`).
     */
    public val isMasterchain: Boolean get() = workchain == MASTERCHAIN.workchain

    override fun toString(): String = "$workchain:${prefix.toHexString()}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as ShardId
        if (workchain != other.workchain) return false
        if (prefix != other.prefix) return false
        return true
    }

    override fun hashCode(): Int {
        var result = workchain
        result = 31 * result + prefix.hashCode()
        return result
    }

    public companion object {
        public const val PREFIX_ROOT: ULong = 0x8000000000000000uL

        @JvmField
        public val MASTERCHAIN: ShardId = ShardId(-1, PREFIX_ROOT)

        @JvmField
        public val BASECHAIN: ShardId = ShardId(0, PREFIX_ROOT)
    }
}
