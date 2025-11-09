package utils

import org.ton.sdk.bitstring.BitString
import org.ton.sdk.cell.Cell
import org.ton.sdk.cell.CellBuilder
import org.ton.sdk.cell.LevelMask
import kotlin.math.max
import kotlin.random.Random

class RandomBagOfCells(
    size: Int,
    random: Random,
    withPrunedBranches: Boolean,
    vararg cells: Cell
) {
    private val nodes = ArrayList<Node>()

    init {
        repeat(size) {
            addRandomCell(random, withPrunedBranches)
        }
    }

    val root: Cell get() {
        while (nodes.last().cell.level != 0) {
            val cell = CellBuilder.createMerkleProof(nodes.last().cell)
            val node = Node(cell, nodes.last().merkleDepth + 1)
            nodes.add(node)
        }
        return nodes.last().cell
    }

    fun getRandomRoots(size: Int, random: Random) = Array(size) {
        nodes[random.nextInt(nodes.size)].cell
    }

    fun addRandomCell(random: Random, withPrunedBranches: Boolean = true) {
        while (true) {
            val cb = CellBuilder()
            val nextCnt = random.nextInt(Cell.MAX_REFS + 1)
            var merkleDepth = 0
            var j = 0
            while (j < nextCnt && nodes.isNotEmpty()) {
                val to = random.nextInt(if (j == 0 && nodes.size > 3) nodes.size - 3 else 0, nodes.size)
                merkleDepth = max(merkleDepth, nodes[to].merkleDepth)
                cb.storeReference(nodes[to].cell)
                j++
            }
            val size = random.nextInt(5)
            repeat(size) {
                val i = random.nextInt(2)
                cb.store("ab".encodeToByteArray(), i, i + 1)
            }
            if (random.nextInt(0, 5) == 4) {
                val is_ff = random.nextBoolean()
                val bitCount = random.nextInt(1, 8)
                cb.store(
                    BitString(
                        byteArrayOf((if (is_ff) 0xFF else 0x55).toByte()),
                        bitCount
                    )
                )
            }
            var cell = cb.build()
            val cellLevel = cell.level
            if (withPrunedBranches) {
                if (random.nextInt(6) == 0 && cellLevel + 1 < Cell.MAX_LEVEL) {
                    cell = CellBuilder.createPrunedBranch(cell, cellLevel + 1)
                }
                if (merkleDepth + 1 + cell.level < Cell.MAX_LEVEL && random.nextInt(11) == 0) {
                    cell = CellBuilder.createMerkleProof(cell)
                    merkleDepth++
                }
            }
            if (merkleDepth + cell.level >= Cell.MAX_LEVEL) {
                continue
            }
            nodes.add(Node(cell, merkleDepth))
            break
        }
    }

    private data class Node(
        val cell: Cell,
        val merkleDepth: Int
    )
}

fun genRandomCell(size: Int, random: XorShift128Plus, withPrunedBranches: Boolean = true): Cell {
    return RandomBagOfCells(size, random, withPrunedBranches).root
}
