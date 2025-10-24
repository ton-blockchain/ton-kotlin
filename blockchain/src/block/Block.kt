package org.ton.sdk.blockchain.block

import org.ton.kotlin.cell.CellRef
import org.ton.kotlin.cell.MerkleUpdate

public class Block(
    public val globalId: Int,
    public val info: CellRef<BlockInfo>,
    public val valueFlow: CellRef<ValueFlow>,
    public val stateUpdate: CellRef<MerkleUpdate>,
    public val extra: CellRef<BlockExtra>
)
