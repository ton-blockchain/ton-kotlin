package org.ton.sdk.cell.boc

import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.writeIntLe
import org.ton.sdk.bitstring.unsafe.UnsafeBitStringApi
import org.ton.sdk.bitstring.unsafe.UnsafeBitStringOperations
import org.ton.sdk.cell.Cell
import org.ton.sdk.cell.CellContext
import org.ton.sdk.cell.CellType
import org.ton.sdk.cell.LoadedCell
import org.ton.sdk.cell.boc.internal.DigestSink
import org.ton.sdk.cell.boc.internal.writeLong
import org.ton.sdk.cell.internal.LibraryCell
import org.ton.sdk.cell.internal.PrunedCell
import org.ton.sdk.crypto.CRC32C
import org.ton.sdk.crypto.HashBytes
import kotlin.experimental.or
import kotlin.math.min

internal class BagOfCellSerializer(
    private val cellContext: CellContext = CellContext.EMPTY
) {
    private val cellsMap = HashMap<HashBytes, CellInfo>()
    private val cells = ArrayList<CellInfo>()
    private var rvIndex = 0
    private var internalHashes = 0
    private var intRefs = 0
    private var topHashes = 0
    private var dataBytes = 0L
    private val roots = ArrayList<RootInfo>()

    fun addRoot(cell: Cell) {
        roots += RootInfo(
            cell = cell,
            hash = cell.hash()
        )
    }

    fun importCells() {
        resetCells()
        for (root in roots) {
            importCell(root.cell ?: continue, 0)
        }
        reorderCells()
    }

    private fun importCell(cell: Cell, depth: Int): CellInfo {
        val hash = cell.hash()
        cellsMap[hash]?.let {
            it.shouldCache = true
            return it
        }
        val refIndexes = IntArray(cell.referenceCount) { -1 }
        var sumChildWeight = 1
        if (refIndexes.isNotEmpty()) {
            val loadedCell = cellContext.loadCell(cell)
            for (i in 0 until refIndexes.size) {
                val ref = requireNotNull(loadedCell.reference(i)) {
                    "Failed to load reference $i of cell $hash"
                }
                val refInfo = importCell(ref, depth + 1)
                refIndexes[i] = refInfo.index
                sumChildWeight += refInfo.weight
                intRefs++
            }
        }
        val hashCount = cell.levelMask.hashCount
        val index = cells.size
        val serializedSize = cell.descriptor.byteLength + 2
        dataBytes += serializedSize
        val cellInfo = CellInfo(
            refIndexes = refIndexes,
            weight = min(0xFF, sumChildWeight),
            hashCount = hashCount,
            serializedSize = serializedSize,
            index = index,
            hash = hash,
            cell = cell
        )
        cells.add(cellInfo)
        cellsMap[hash] = cellInfo
//        println("${cellInfo.index} import_cell ${cell.hash()} depth=$depth levelmask=${cell.levelMask.mask} hc=${cellInfo.hashCount} wt=${cellInfo.weight} ")
        return cellInfo
    }

    private fun reorderCells() {
        // --------------------------------------------------------------------
        // Pass 1: rebalance child weights bottom-up.
        // --------------------------------------------------------------------
        for (cellIndex in cells.size - 1 downTo 0) {
            val parent = cells[cellIndex]
//            println("reorder_cells $cellIndex levelmask=${parent.cell?.levelMask?.mask} hashes=${parent.hashCount} wt=${parent.weight}")

            var remainingChildrenToAdjust = parent.refs
            var remainingWeightBudget = CellInfo.MAX_CELL_WHS - 1 // parent itself costs "1"
            var pinnedChildrenMask = 0 // bitmask of children that keep their wt

            // First pass: pin children whose current wt already fits a fair share.
            for (childPos in 0 until parent.refs) {
                val childIndex = parent.refIndexes[childPos]
                val child = cells[childIndex]
                val fairLimit = (CellInfo.MAX_CELL_WHS - 1 + childPos) / parent.refs

                if (child.weight <= fairLimit) {
                    remainingWeightBudget -= child.weight
                    remainingChildrenToAdjust--
                    pinnedChildrenMask = pinnedChildrenMask or (1 shl childPos)
                }
            }

            // Second pass: for remaining children, cap their wt so total fits into budget.
            if (remainingChildrenToAdjust > 0) {
                for (childPos in 0 until parent.refs) {
                    if (pinnedChildrenMask and (1 shl childPos) != 0) {
                        continue // this child already accepted as-is
                    }
                    val childIndex = parent.refIndexes[childPos]
                    val child = cells[childIndex]

                    // Distribute the remaining budget evenly across remaining children.
                    val failLimit = remainingWeightBudget++ / remainingChildrenToAdjust

                    if (child.weight > failLimit) {
                        child.weight = failLimit
                    }
                }
            }
        }

        // --------------------------------------------------------------------
        // Pass 2: finalize weights or mark special cells.
        // --------------------------------------------------------------------
        internalHashes = 0
        for (cellIndex in 0 until cells.size) {
            val cell = cells[cellIndex]

            var subtreeWeight = 1
            for (childPos in 0 until cell.refs) {
                val childIndex = cell.refIndexes[childPos]
                val child = cells[childIndex]
                subtreeWeight += child.weight
            }

            if (subtreeWeight <= cell.weight) {
                // Can inline the whole subtree under this cell.
                cell.weight = subtreeWeight
            } else {
                // Subtree is too heavy -> cell becomes "special":
                // BoC writer will use its hashes instead of fully inlining everything.
                cell.weight = 0
                internalHashes += cell.hashCount
            }
        }

        // --------------------------------------------------------------------
        // Pass 3: compute top-level hashes.
        // --------------------------------------------------------------------
        var topHashes = 0
        for (root in roots) {
            val info = cellsMap[root.hash] ?: continue
            if (!info.isSpecial) {
                topHashes += info.hashCount
                info.isRootCell = true
            }
        }

        // --------------------------------------------------------------------
        // Pass 4: assign final indices via three-phase DFS.
        // --------------------------------------------------------------------

        for (info in cells) {
            info.index = -1
        }

        // First, previsit and visit starting from all roots.
        for (root in roots) {
            val info = cellsMap[root.hash] ?: continue
            revisit(info, phase = 0) // previsit: schedule and mark dependencies
            revisit(info, phase = 1) // visit: finalize children layout
        }

        // Then, allocate final indices in a deterministic order from roots.
        for (root in roots) {
            val root = cellsMap[root.hash] ?: continue
            revisit(root, phase = 2)
        }

        // Refresh root.idx based on final indices.
        for (root in roots) {
            val info = cellsMap[root.hash] ?: continue
            root.idx = info.index
        }

        // --------------------------------------------------------------------
        // Pass 5: in-place permutation of cellList.
        //
        // After revisit:
        //   - each CellInfo.index holds its final target index.
        // This loop permutes cellList so that:
        //   cellList[i] is exactly the cell whose index == i.
        // --------------------------------------------------------------------
        val size = cells.size
        var i = 0
        while (i < size) {
            val target = cells[i].index
            if (target == i) {
                i++
            } else {
                val tmp = cells[i]
                cells[i] = cells[target]
                cells[target] = tmp
            }
        }
    }

    private fun revisit(cell: CellInfo, phase: Int): Int {
        if (cell.index >= 0) {
            return cell.index
        }
        if (phase == 0) {
            // previsit
            if (cell.index != CellInfo.NOT_VISITED) {
                return cell.index // already previsited or visited
            }
            for (i in cell.refs - 1 downTo 0) {
                val childIndex = cell.refIndexes[i]
                val childCell = cells[childIndex]
//                println("revisit child $childIndex is_special ${childCell.isSpecial} wt=${childCell.weight}")
                // either previsit or visit child, depending on whether it is special
                revisit(childCell, if (childCell.isSpecial) 1 else 0)
            }
            cell.index = CellInfo.PRE_VISITED
            return CellInfo.PRE_VISITED
        }
        if (phase > 1) {
            // time to allocate
            cell.index = rvIndex++
            return cell.index
        }
        if (cell.index == CellInfo.VISITED) {
            return cell.index
        }
        if (cell.isSpecial) {
            // if current cell is special, previsit it first
            revisit(cell, 0)
        }
        // visit children
        for (i in cell.refs - 1 downTo 0) {
            val childIndex = cell.refIndexes[i]
            val childCell = cells[childIndex]
            revisit(childCell, 1)
        }
        // allocate children
        for (i in cell.refs - 1 downTo 0) {
            val childIndex = cell.refIndexes[i]
            val childCell = cells[childIndex]
            cell.refIndexes[i] = revisit(childCell, 2)
        }
        cell.index = CellInfo.VISITED
        return cell.index
    }

    fun createHeader(options: BagOfCells.EncodeOptions): BagOfCellsHeader {
        val rootCount = roots.size
        val cellCount = cells.size

        var refByteSize = 0
        while (cells.size >= (1L shl (refByteSize shl 3))) {
            refByteSize++
        }

        val hashes = ((if (options.withTopHashes) topHashes else 0) +
                (if (options.withInternalHashes) internalHashes else 0)) *
                (Cell.HASH_BYTES + Cell.DEPTH_BYTES)
        val dataBytesAdj = dataBytes + intRefs.toLong() * refByteSize + hashes
        val maxOffset = if (options.withCacheBits) dataBytesAdj * 2 else dataBytesAdj
        var offsetByteSize = 0
        while (maxOffset >= (1L shl (offsetByteSize shl 3))) {
            offsetByteSize++
        }

        val rootsOffset = 4L + 1 + 1 + 3 + refByteSize + offsetByteSize
        val indexOffset = rootsOffset + rootCount * refByteSize
        val dataOffset = indexOffset + if (options.withIndex) {
            cells.size * offsetByteSize
        } else {
            0
        }
        val crcSize = if (options.withCrc32c) 4 else 0
        val totalSize = dataOffset + dataBytesAdj + crcSize
        return BagOfCellsHeader(
            BagOfCellsHeader.BOC_GENERIC_MAGIC,
            rootCount,
            cellCount,
            0,
            refByteSize,
            offsetByteSize,
            options.withIndex,
            true,
            options.withCrc32c,
            options.withCacheBits,
            rootsOffset,
            indexOffset,
            dataOffset,
            dataBytesAdj,
            totalSize
        )
    }

    fun serialize(
        sink: Sink,
        options: BagOfCells.EncodeOptions
    ) {
        val header = createHeader(options)
        val cellCount = header.cellCount

        val crC32c: CRC32C?
        val output: Sink
        if (header.hasCrc32c) {
            crC32c = CRC32C()
            output = DigestSink(sink, crC32c).buffered()
        } else {
            crC32c = null
            output = sink
        }

        output.writeInt(BagOfCellsHeader.BOC_GENERIC_MAGIC)
        var byte = 0
        if (header.hasIndex) {
            byte = byte or (1 shl 7)
        }
        if (header.hasCrc32c) {
            byte = byte or (1 shl 6)
        }
        if (header.hasCacheBits) {
            byte = byte or (1 shl 5)
        }
        byte = byte or header.refByteSize
        output.writeByte(byte.toByte())
        output.writeByte(header.offsetByteSize.toByte())

        output.writeLong(cellCount.toLong(), header.refByteSize)
        output.writeLong(header.rootCount.toLong(), header.refByteSize)
        output.writeLong(0, header.refByteSize)
        output.writeLong(header.dataSize, header.offsetByteSize)

        for (root in roots) {
            val index = cellCount - 1 - root.idx
            output.writeLong(index.toLong(), header.refByteSize)
        }

        if (header.hasIndex) {
            var offs = 0
            for (index in cells.size - 1 downTo 0) {
                val cell = cells[index]
                val withHash = (options.withInternalHashes && cell.isSpecial) ||
                        (options.withTopHashes && cell.isRootCell)
                val hashSize = if (withHash) {
                    (Cell.HASH_BYTES + Cell.DEPTH_BYTES) * cell.hashCount
                } else {
                    0
                }
                offs += cell.serializedSize + hashSize + cell.refs * header.refByteSize
                val fixedOffset = if (header.hasCacheBits) {
                    offs * 2 + if (cell.shouldCache) 1 else 0
                } else {
                    offs
                }
                output.writeLong(fixedOffset.toLong(), header.offsetByteSize)
            }
        }

        val batchSize = 1_000_000
        for (batchStart in 0 until cellCount step batchSize) {
            val batchEnd = min(batchStart + batchSize, cellCount)

            val batchWindow = batchEnd - batchStart
            val batchHashes = ArrayList<HashBytes>(batchWindow)
            for (i in batchStart until batchEnd) {
                val cellIndex = cellCount - 1 - i
                val cellInfo = cells[cellIndex]
                if (cellInfo.cell == null) {
                    batchHashes.add(cellInfo.hash)
                }
            }
            val batchCells = ArrayList<LoadedCell>()

            var indexInBatch = 0
            val buf = ByteArray(Cell.MAX_SERIALIZED_BYTES)
            for (i in batchStart until batchEnd) {
                val cellIndex = cellCount - 1 - i
                val cellInfo = cells[cellIndex]
                val withHash = (options.withInternalHashes && cellInfo.isSpecial) ||
                        (options.withTopHashes && cellInfo.isRootCell)
                val cell = cellInfo.cell ?: batchCells[indexInBatch++]
                if (i == 75) {
                    println("catch")
                }
                val bytes = cell.serialize(buf, withHash)
                println("serialize $i wt=${cellInfo.weight} ${buf.copyOf(bytes).toHexString()}")
                output.write(buf, 0, bytes)

                for (i in cellInfo.refIndexes) {
                    val refIndex = cellCount - 1 - i
                    output.writeLong(refIndex.toLong(), header.refByteSize)
                }
            }
        }

        output.flush()
        if (crC32c != null) {
            val crc = crC32c.intDigest()
            output.writeIntLe(crc)
            output.flush()
        }
    }

    @OptIn(UnsafeBitStringApi::class)
    private fun Cell.serialize(buf: ByteArray, withHash: Boolean): Int {
        var offset = 0
        buf[offset++] = descriptor.d1 or (if (withHash) 16 else 0)
        buf[offset++] = descriptor.d2
        val mask = descriptor.levelMask
        val level = mask.level
        if (withHash) {
            for (i in 0..level) {
                if (i !in mask) continue
                val hash = hash(i).value
                hash.copyInto(buf, offset)
                offset += Cell.HASH_BYTES
            }
            for (i in 0..level) {
                if (i !in mask) continue
                val depth = depth(i)
                buf[offset++] = (depth shr 8).toByte()
                buf[offset++] = (depth).toByte()
            }
        }

        when (this) {
            is LoadedCell -> {
                UnsafeBitStringOperations.withByteArrayUnsafe(bits) { data ->
                    val length = descriptor.byteLength
                    data.copyInto(buf, offset, 0, length)
                    offset += length
                }
            }

            is PrunedCell -> {
                buf[offset++] = CellType.PRUNED_BRANCH.value.toByte()
                buf[offset++] = mask.mask.toByte()
                for (i in 0 until level) {
                    val hash = hash(i).value
                    hash.copyInto(buf, offset)
                    offset += Cell.HASH_BYTES
                }
                for (i in 0 until level) {
                    val depth = depth(i)
                    buf[offset++] = (depth shr 8).toByte()
                    buf[offset++] = (depth).toByte()
                }
            }

            is LibraryCell -> {
                hash.value.copyInto(buf, offset, offset + Cell.HASH_BYTES)
                offset += Cell.HASH_BYTES
            }
//            is MerkleProof -> {
//                buf[offset++] = CellType.MERKLE_PROOF.value.toByte()
//                virtualHash.value.copyInto(buf, offset, offset + Cell.HASH_BYTES)
//                offset += Cell.HASH_BYTES
//                buf[offset++] = (virtualDepth shr 8).toByte()
//                buf[offset++] = (virtualDepth).toByte()
//            }
            else -> TODO("$this")
        }
        return offset
    }


    private fun resetCells() {
        cellsMap.clear()
        cells.clear()
        internalHashes = 0
        topHashes = 0
        rvIndex = 0
        // roots remain; their idx will be overwritten
        roots.forEach { it.idx = -1 }
    }


    private data class RootInfo(
        val cell: Cell?,
        val hash: HashBytes,
        var idx: Int = -1,
    )

    @Suppress("ArrayInDataClass")
    private data class CellInfo(
        val refIndexes: IntArray = IntArray(4) { -1 }, // indices of children in [0, cellCount)
        var weight: Int = 0,                       // wt: subtree weight or 0 if "special"
        var hashCount: Int = 0,
        var serializedSize: Int = 0,
        var shouldCache: Boolean = false,
        var index: Int = -1,                        // final index after allocation (phase 2)
        var isRootCell: Boolean = false,
        val hash: HashBytes,
        val cell: Cell? = null
    ) {
        val refs: Int get() = refIndexes.size
        val isSpecial get() = weight == 0

        companion object {
            const val NOT_VISITED = -1
            const val PRE_VISITED = -2
            const val VISITED = -3
            const val MAX_CELL_WHS = 64
        }
    }
}
