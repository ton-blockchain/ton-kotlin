package org.ton.sdk.blockchain

import kotlinx.io.bytestring.ByteString
import kotlin.jvm.JvmName

/**
 * Represents a unique identifier for a block in the TON blockchain.
 *
 * A `BlockId` consists of a shard identifier, sequence number, root hash, and file hash, which
 * collectively provide a unique reference to a specific block within the blockchain.
 *
 * @property shardId The shard identifier containing the workchain and shard prefix to which this block belongs.
 * @property seqno The sequence number of the block within its shard, incrementing with each new block.
 * @property rootHash The hash of the root cell that represents the block's data.
 * @property fileHash The hash of the BoC (Bag Of Cells) encoded root cell that represents the block's data.
 */
public class BlockId(
    @get:JvmName("shardId") public val shardId: ShardId,
    @get:JvmName("seqno") public val seqno: Int,
    @get:JvmName("rootHash") public val rootHash: ByteString,
    @get:JvmName("fileHash") public val fileHash: ByteString
) {
    /**
     * The numerical identifier of the workchain for this [BlockId].
     *
     * The workchain is retrieved from the underlying [ShardId] associated with this block. A workchain
     * represents a distinct virtual blockchain within the TON blockchain. The value `-1` represents
     * the masterchain, while other values identify specific workchains.
     */
    @get:JvmName("workchain")
    public val workchain: Int get() = shardId.workchain

    /**
     * Provides the identifier prefix of the shard within its workchain.
     *
     * This property represents the `prefix` of the [ShardId] associated with the [BlockId].
     * It is an unsigned 64-bit integer uniquely identifying a shard within its respective workchain.
     */
    @get:JvmName("shard")
    public val shard: ULong get() = shardId.prefix

    /**
     * Converts the current [BlockId] instance into a lightweight [BlockIdShort] representation.
     *
     * @return A [BlockIdShort] instance containing the shard identifier and sequence number of the current block.
     */
    public fun toShort(): BlockIdShort = BlockIdShort(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlockId) return false
        if (seqno != other.seqno) return false
        if (shardId != other.shardId) return false
        if (rootHash != other.rootHash) return false
        if (fileHash != other.fileHash) return false
        return true
    }

    override fun hashCode(): Int {
        var result = seqno
        result = 31 * result + shardId.hashCode()
        result = 31 * result + rootHash.hashCode()
        result = 31 * result + fileHash.hashCode()
        return result
    }

    override fun toString(): String = "$shardId:$seqno:$rootHash:$fileHash"
}
