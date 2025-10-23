package org.ton.kotlin.blockchain

public class ShardId(
    public val workchain: Int,
    public val prefix: ULong
) {
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

        public val MASTERCHAIN: ShardId = ShardId(-1, PREFIX_ROOT)
        public val BASECHAIN: ShardId = ShardId(0, PREFIX_ROOT)
    }
}
