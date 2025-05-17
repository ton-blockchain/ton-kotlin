package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.Cell
import org.ton.kotlin.cell.CellType
import org.ton.kotlin.tlb.CellRef


@SerialName("merkle_proof")
public data class MerkleProof<X>(
    val virtualHash: BitString,
    val depth: Int,
    val virtualRoot: CellRef<X>
) {
    public companion object {
        public fun virtualize(cell: Cell, offset: Int = 1): Cell {
            require(cell.type == CellType.MERKLE_PROOF) {
                "Invalid cell type, expected: ${CellType.MERKLE_PROOF}, actual: ${cell.type}"
            }
            return cell.refs.first().virtualize(offset)
        }
    }
}
