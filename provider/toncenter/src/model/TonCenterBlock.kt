@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.kotlin.provider.toncenter.model

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
public data class TonCenterBlock(
    public val workchain: Int,
    public val shard: Long,
    public val seqno: Int,
    public val rootHash: ByteString,
    public val fileHash: ByteString,
    public val globalId: Long,
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
    public val randSeed: ByteString,
    public val createdBy: ByteString,
    public val txCount: Int,
    public val masterchainBlockRef: TonCenterBlockId,
    public val prevBlocks: List<TonCenterBlockId>,
)
