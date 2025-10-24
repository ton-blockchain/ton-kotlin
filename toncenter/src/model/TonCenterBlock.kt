@file:UseSerializers(
    ByteStringBase64Serializer::class,
    HashBytesAsBase64Serializer::class,
)

package org.ton.sdk.toncenter.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.kotlin.crypto.HashBytes
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer
import org.ton.sdk.toncenter.internal.serializers.HashBytesAsBase64Serializer

@Serializable
public class TonCenterBlock(
    public val workchain: Int,
    public val shard: Long,
    public val seqno: Int,
    public val rootHash: HashBytes,
    public val fileHash: HashBytes,
    public val globalId: Int,
    public val version: Int,
    public val afterMerge: Boolean,
    public val beforeSplit: Boolean,
    public val afterSplit: Boolean,
    public val wantMerge: Boolean,
    public val wantSplit: Boolean,
    public val keyBlock: Boolean,
    public val vertSeqnoIncr: Boolean,
    public val flags: Int,
    public val genUtime: Long,
    public val startLt: Long,
    public val endLt: Long,
    public val validatorListHashShort: Int,
    public val genCatchainSeqno: Int,
    public val minRefMcSeqno: Int,
    public val prevKeyBlockSeqno: Int,
    public val vertSeqno: Int,
    public val masterRefSeqno: Int,
    public val randSeed: HashBytes,
    public val createdBy: HashBytes,
    public val txCount: Int,
    public val masterchainBlockRef: TonCenterBlockId,
    public val prevBlocks: List<TonCenterBlockId>,
)
