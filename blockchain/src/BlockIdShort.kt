package org.ton.sdk.blockchain

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlin.jvm.JvmName

/**
 * Represents a lightweight identifier for a block in the TON blockchain.
 *
 * A `BlockIdShort` is a simplified version of [BlockId], containing only the shard identifier and
 * sequence number to uniquely reference a block within a specific shard. This class provides
 * quick access to the block's workchain and shard details without the overhead of hash values
 * like `rootHash` and `fileHash`.
 *
 * @property shardId The shard identifier containing the workchain and shard prefix to which this block belongs.
 * @property seqno The sequence number of the block within its shard, incrementing with each new block created.
 */
@Serializable(with = BlockIdShort.Serializer::class)
public class BlockIdShort(
    @get:JvmName("shardId") public val shardId: ShardId,
    @get:JvmName("seqno") public val seqno: Int,
) {
    public constructor(blockId: BlockId) : this(blockId.shardId, blockId.seqno)

    /**
     * The numerical identifier of the workchain for this `BlockId`.
     *
     * The workchain is retrieved from the underlying `ShardId` associated with this block. A workchain
     * represents a distinct virtual blockchain within the TON blockchain. The value `-1` represents
     * the masterchain, while other values identify specific workchains.
     */
    @get:JvmName("workchain")
    public val workchain: Int get() = shardId.workchain

    /**
     * Provides the identifier prefix of the shard within its workchain.
     *
     * This property represents the `prefix` of the `ShardId` associated with the `BlockId`.
     * It is an unsigned 64-bit integer uniquely identifying a shard within its respective workchain.
     */
    @get:JvmName("shard")
    public val shard: ULong get() = shardId.prefix

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlockIdShort) return false
        if (seqno != other.seqno) return false
        if (shardId != other.shardId) return false
        return true
    }

    override fun hashCode(): Int {
        var result = seqno
        result = 31 * result + shardId.hashCode()
        return result
    }

    override fun toString(): String = "$shardId:$seqno"

    private object Serializer : KSerializer<BlockIdShort> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor(BlockIdShort::class.toString()) {
            element<Int>("workchain")
            element<ULong>("shard")
            element<Int>("seqno")
        }

        override fun serialize(
            encoder: Encoder,
            value: BlockIdShort
        ) = encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.workchain)
            encodeLongElement(descriptor, 1, value.shard.toLong())
            encodeIntElement(descriptor, 2, value.seqno)
        }

        override fun deserialize(decoder: Decoder): BlockIdShort = decoder.decodeStructure(descriptor) {
            var workchain = 0
            var shard: ULong = 0u
            var seqno = 0
            while (true) {
                when (decodeElementIndex(descriptor)) {
                    0 -> workchain = decodeIntElement(descriptor, 0)
                    1 -> shard = decodeLongElement(descriptor, 1).toULong()
                    2 -> seqno = decodeIntElement(descriptor, 2)
                    else -> break
                }
            }
            BlockIdShort(ShardId(workchain, shard), seqno)
        }
    }
}
