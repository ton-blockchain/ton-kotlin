package org.ton.sdk.blockchain.block

import org.ton.kotlin.cell.CellRef
import org.ton.sdk.blockchain.GlobalVersion
import org.ton.sdk.blockchain.ShardId

public class BlockInfo(
    public val version: Int,
    public val notMaster: Boolean,
    public val afterMerge: Boolean,
    public val beforeSplit: Boolean,
    public val afterSplit: Boolean,
    public val wantSplit: Boolean,
    public val wantMerge: Boolean,
    public val keyBlock: Boolean,
    public val vertSeqnoIncr: Boolean,
    public val flags: Int,
    public val seqno: Int,
    public val vertSeqno: Int,
    public val shardId: ShardId,
    public val genUTime: Long,
    public val startLt: Long,
    public val endLt: Long,
    public val genValidatorListHashShort: Int,
    public val genCatchainSeqno: Int,
    public val minRefMcSeqno: Int,
    public val prevKeyBlockSeqno: Int,
    public val genSoftware: CellRef<GlobalVersion>?,
    public val masterRef: BlockRef?,
    public val prevRefs: List<CellRef<BlockRef>>,
    public val prevVertRef: CellRef<BlockRef>?
)
