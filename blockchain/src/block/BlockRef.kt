package org.ton.sdk.blockchain.block

import kotlinx.io.bytestring.ByteString
import org.ton.tlb.CellRef

public class BlockRef(
    public val endLt: Long,
    public val seqno: Int,
    public val rootHash: ByteString,
    public val fileHash: ByteString
)

public class BlockRefs(
    public val prev1: CellRef<BlockRef>,
    public val prev2: CellRef<BlockRef>
)
