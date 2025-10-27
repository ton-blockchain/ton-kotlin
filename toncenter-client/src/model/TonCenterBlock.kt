@file:UseSerializers(
    ByteStringBase64Serializer::class,
    HashBytesAsBase64Serializer::class,
)

package org.ton.sdk.toncenter.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.sdk.blockchain.BlockIdShort
import org.ton.sdk.blockchain.ShardId
import org.ton.sdk.crypto.HashBytes
import org.ton.sdk.tl.serializers.ByteStringBase64Serializer
import org.ton.sdk.toncenter.internal.serializers.HashBytesAsBase64Serializer
import kotlin.jvm.JvmName

@Serializable
public class TonCenterBlock(
    @get:JvmName("workchain")
    public val workchain: Int,
    @get:JvmName("shard")
    public val shard: ULong,
    @get:JvmName("seqno")
    public val seqno: Int,
    @get:JvmName("rootHash")
    public val rootHash: HashBytes,
    @get:JvmName("fileHash")
    public val fileHash: HashBytes,
    @get:JvmName("globalId")
    public val globalId: Int,
    @get:JvmName("version")
    public val version: Int,
    @get:JvmName("afterMerge")
    public val afterMerge: Boolean,
    @get:JvmName("beforeSplit")
    public val beforeSplit: Boolean,
    @get:JvmName("afterSplit")
    public val afterSplit: Boolean,
    @get:JvmName("wantMerge")
    public val wantMerge: Boolean,
    @get:JvmName("wantSplit")
    public val wantSplit: Boolean,
    @get:JvmName("keyBlock")
    public val keyBlock: Boolean,
    @get:JvmName("vertSeqnoIncr")
    public val vertSeqnoIncr: Boolean,
    @get:JvmName("flags")
    public val flags: Int,
    @get:JvmName("genUtime")
    public val genUtime: Long,
    @get:JvmName("startLt")
    public val startLt: Long,
    @get:JvmName("endLt")
    public val endLt: Long,
    @get:JvmName("validatorListHashShort")
    public val validatorListHashShort: Int,
    @get:JvmName("genCatchainSeqno")
    public val genCatchainSeqno: Int,
    @get:JvmName("minRefMcSeqno")
    public val minRefMcSeqno: Int,
    @get:JvmName("prevKeyBlockSeqno")
    public val prevKeyBlockSeqno: Int,
    @get:JvmName("vertSeqno")
    public val vertSeqno: Int,
    @get:JvmName("masterRefSeqno")
    public val masterRefSeqno: Int,
    @get:JvmName("randSeed")
    public val randSeed: HashBytes,
    @get:JvmName("createdBy")
    public val createdBy: HashBytes,
    @get:JvmName("txCount")
    public val txCount: Int,
    @get:JvmName("masterchainBlockRef")
    public val masterchainBlockRef: BlockIdShort,
    @get:JvmName("prevBlocks")
    public val prevBlocks: List<BlockIdShort>,
) {
    @get:JvmName("shardId")
    public val shardId: ShardId get() = ShardId(workchain, shard)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as TonCenterBlock

        if (workchain != other.workchain) return false
        if (shard != other.shard) return false
        if (seqno != other.seqno) return false
        if (globalId != other.globalId) return false
        if (version != other.version) return false
        if (afterMerge != other.afterMerge) return false
        if (beforeSplit != other.beforeSplit) return false
        if (afterSplit != other.afterSplit) return false
        if (wantMerge != other.wantMerge) return false
        if (wantSplit != other.wantSplit) return false
        if (keyBlock != other.keyBlock) return false
        if (vertSeqnoIncr != other.vertSeqnoIncr) return false
        if (flags != other.flags) return false
        if (genUtime != other.genUtime) return false
        if (startLt != other.startLt) return false
        if (endLt != other.endLt) return false
        if (validatorListHashShort != other.validatorListHashShort) return false
        if (genCatchainSeqno != other.genCatchainSeqno) return false
        if (minRefMcSeqno != other.minRefMcSeqno) return false
        if (prevKeyBlockSeqno != other.prevKeyBlockSeqno) return false
        if (vertSeqno != other.vertSeqno) return false
        if (masterRefSeqno != other.masterRefSeqno) return false
        if (txCount != other.txCount) return false
        if (rootHash != other.rootHash) return false
        if (fileHash != other.fileHash) return false
        if (randSeed != other.randSeed) return false
        if (createdBy != other.createdBy) return false
        if (masterchainBlockRef != other.masterchainBlockRef) return false
        if (prevBlocks != other.prevBlocks) return false

        return true
    }

    override fun hashCode(): Int {
        var result = workchain
        result = 31 * result + shard.hashCode()
        result = 31 * result + seqno
        result = 31 * result + globalId
        result = 31 * result + version
        result = 31 * result + afterMerge.hashCode()
        result = 31 * result + beforeSplit.hashCode()
        result = 31 * result + afterSplit.hashCode()
        result = 31 * result + wantMerge.hashCode()
        result = 31 * result + wantSplit.hashCode()
        result = 31 * result + keyBlock.hashCode()
        result = 31 * result + vertSeqnoIncr.hashCode()
        result = 31 * result + flags
        result = 31 * result + genUtime.hashCode()
        result = 31 * result + startLt.hashCode()
        result = 31 * result + endLt.hashCode()
        result = 31 * result + validatorListHashShort
        result = 31 * result + genCatchainSeqno
        result = 31 * result + minRefMcSeqno
        result = 31 * result + prevKeyBlockSeqno
        result = 31 * result + vertSeqno
        result = 31 * result + masterRefSeqno
        result = 31 * result + txCount
        result = 31 * result + rootHash.hashCode()
        result = 31 * result + fileHash.hashCode()
        result = 31 * result + randSeed.hashCode()
        result = 31 * result + createdBy.hashCode()
        result = 31 * result + masterchainBlockRef.hashCode()
        result = 31 * result + prevBlocks.hashCode()
        return result
    }

    override fun toString(): String = buildString {
        append("TonCenterBlock(workchain=")
        append(workchain)
        append(", shard=")
        append(shard)
        append(", seqno=")
        append(seqno)
        append(", rootHash=")
        append(rootHash)
        append(", fileHash=")
        append(fileHash)
        append(", globalId=")
        append(globalId)
        append(", version=")
        append(version)
        append(", afterMerge=")
        append(afterMerge)
        append(", beforeSplit=")
        append(beforeSplit)
        append(", afterSplit=")
        append(afterSplit)
        append(", wantMerge=")
        append(wantMerge)
        append(", wantSplit=")
        append(wantSplit)
        append(", keyBlock=")
        append(keyBlock)
        append(", vertSeqnoIncr=")
        append(vertSeqnoIncr)
        append(", flags=")
        append(flags)
        append(", genUtime=")
        append(genUtime)
        append(", startLt=")
        append(startLt)
        append(", endLt=")
        append(endLt)
        append(", validatorListHashShort=")
        append(validatorListHashShort)
        append(", genCatchainSeqno=")
        append(genCatchainSeqno)
        append(", minRefMcSeqno=")
        append(minRefMcSeqno)
        append(", prevKeyBlockSeqno=")
        append(prevKeyBlockSeqno)
        append(", vertSeqno=")
        append(vertSeqno)
        append(", masterRefSeqno=")
        append(masterRefSeqno)
        append(", randSeed=")
        append(randSeed)
        append(", createdBy=")
        append(createdBy)
        append(", txCount=")
        append(txCount)
        append(", masterchainBlockRef=")
        append(masterchainBlockRef)
        append(", prevBlocks=")
        append(prevBlocks)
        append(")")
    }
}
