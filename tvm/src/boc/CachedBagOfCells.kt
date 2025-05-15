package org.ton.boc

import io.ktor.utils.io.core.*
import org.ton.kotlin.cell.Cell
import kotlin.math.min

public class CachedBagOfCells(
    override val roots: List<Cell>
) : BagOfCells, Iterable<Cell> {
    private var cellCount = 0
    private var cellHashmap = HashMap<Cell, Int>()
    private var cellList = ArrayList<CellInfo>()
    private var rootIndexes = ArrayList<Int>(roots.size)
    private var revisitIndex = 0
    private var cellListTmp = ArrayList<CellInfo>()

    init {
        importCells()
    }

    override fun iterator(): Iterator<Cell> = cellList.asSequence().map { it.cell }.iterator()

    override fun toByteArray(): ByteArray = buildPacket {
        writeBagOfCells(this@CachedBagOfCells)
    }.readBytes()

    override fun toString(): String = buildString {
        roots.forEachIndexed { _, cell ->
            Cell.toString(cell, this)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BagOfCells) return false
        if (roots != other.roots) return false
        return true
    }

    override fun hashCode(): Int = roots.hashCode()

    private fun clearCells() {
        cellCount = 0
        cellHashmap.clear()
        cellList.clear()
        rootIndexes.clear()
    }

    private fun importCells() {
        clearCells()
        roots.forEach { rootCell ->
            val index = importCell(rootCell, 0)
            rootIndexes.add(index)
        }
        reorderCells()
        check(cellCount != 0)
    }

    private fun importCell(cell: Cell, depth: Int): Int {
        check(depth <= Cell.MAX_DEPTH) { "error while importing a cell into a bag of cells: cell depth too large" }

        val currentIndex = cellHashmap[cell]
        if (currentIndex != null) {
            cellList[currentIndex].shouldCache = true
            return currentIndex
        }

        var sumChildWeight = 1
        val referenceIndexes = ArrayList<Int>(cell.refs.size)
        cell.refs.forEach { reference ->
            val referenceIndex = importCell(reference, depth + 1)
            sumChildWeight += cellList[referenceIndex].weight
            referenceIndexes.add(referenceIndex)
        }
        check(cellList.size == cellCount)
        cellHashmap[cell] = cellCount
        val weight = min(0xFF, sumChildWeight)
        val cellInfo = CellInfo(
            cell,
            referenceIndexes,
            weight
        )
        cellList.add(cellInfo)
        return cellCount++
    }

    private fun reorderCells() {
        for (i in cellList.lastIndex downTo 0) {
            val cellInfo = cellList[i]
            var sum = MAX_CELL_WEIGHT - 1
            var mask = 0
            var overlimitedRefs = cellInfo.referencesIndexes.size
            cellInfo.referencesIndexes.forEachIndexed { j, referenceIndex ->
                val referenceInfo = cellList[referenceIndex]
                val limit = (MAX_CELL_WEIGHT - 1 + j) / cellInfo.referencesIndexes.size
                if (referenceInfo.weight <= limit) {
                    sum -= referenceInfo.weight
                    overlimitedRefs--
                    mask = mask or (1 shl j)
                }
            }
            if (overlimitedRefs > 0) {
                cellInfo.referencesIndexes.forEachIndexed { j, referenceIndex ->
                    if ((mask and (1 shl j)) == 0) {
                        val referenceInfo = cellList[referenceIndex]
                        val limit = sum++ / overlimitedRefs
                        if (referenceInfo.weight > limit) {
                            referenceInfo.weight = limit
                        }
                    }
                }
            }
        }
        cellList.forEach { cellInfo ->
            var sum = 1
            cellInfo.referencesIndexes.forEach { referenceIndex ->
                sum += cellList[referenceIndex].weight
            }
            check(sum <= MAX_CELL_WEIGHT)
            if (sum <= cellInfo.weight) {
                cellInfo.weight = sum
            } else {
                cellInfo.weight = 0
            }
        }
        if (cellCount > 0) {
            revisitIndex = 0
            cellListTmp.clear()
            cellListTmp.ensureCapacity(cellCount)

            rootIndexes.forEach { rootIndex ->
                revisit(rootIndex, Revisit.PREVISIT)
                revisit(rootIndex, Revisit.VISIT)
            }
            rootIndexes.forEach { rootIndex ->
                revisit(rootIndex, Revisit.ALLOCATE)
            }
            rootIndexes = ArrayList(
                rootIndexes.map { rootIndex ->
                    cellList[rootIndex].newIndex
                }
            )
            check(revisitIndex == cellCount) { "revisitIndex: $revisitIndex, cellCount: $cellCount" }
            check(cellList.size == cellListTmp.size) { "cellList.size: ${cellList.size}, cellListTmp.size: ${cellListTmp.size}" }
            cellList = ArrayList(cellListTmp.asReversed())
            cellListTmp.clear()
        }
    }

    // force=0 : previsit (recursively until special cells are found; then visit them)
    // force=1 : visit (allocate and process all children)
    // force=2 : allocate (assign a new index; can be run only after visiting)
    private fun revisit(cellIndex: Int, force: Revisit): Int {
        check(cellIndex in 0 until cellCount)
        val cellInfo = cellList[cellIndex]

        if (cellInfo.newIndex >= 0) {
            return cellInfo.newIndex
        }
        if (force == Revisit.PREVISIT) {
            // previsit
            if (cellInfo.newIndex == -1) {
                for (j in cellInfo.referencesIndexes.lastIndex downTo 0) {
                    val childIndex = cellInfo.referencesIndexes[j]
                    // either previsit or visit child, depending on it weight
                    val childForce = if (cellInfo.weight == 0) Revisit.VISIT else Revisit.PREVISIT
                    revisit(childIndex, childForce)
                }
                cellInfo.newIndex = -2
            }
            return cellInfo.newIndex
        }
        if (force == Revisit.ALLOCATE) {
            // time to allocate
            val newIndex = revisitIndex++
            cellInfo.newIndex = newIndex
            cellListTmp.add(cellInfo)
            return newIndex
        }
        if (cellInfo.newIndex == -3) {
            // already revisited
            return cellInfo.newIndex
        }
        if (cellInfo.weight == 0) {
            // if current cell weight == 0, previsit it first
            revisit(cellIndex, Revisit.PREVISIT)
        }
        // visit children
        for (j in cellInfo.referencesIndexes.lastIndex downTo 0) {
            revisit(cellInfo.referencesIndexes[j], Revisit.VISIT)
        }
        // allocate children
        for (j in cellInfo.referencesIndexes.lastIndex downTo 0) {
            cellInfo.referencesIndexes[j] = revisit(cellInfo.referencesIndexes[j], Revisit.ALLOCATE)
        }
        cellInfo.newIndex = -3 // mark as visited (and all children processed)
        return cellInfo.newIndex
    }

    private enum class Revisit {
        PREVISIT,
        VISIT,
        ALLOCATE
    }

    private data class CellInfo(
        val cell: Cell,
        var referencesIndexes: ArrayList<Int>,
        var weight: Int = 0,
        var newIndex: Int = -1,
        var shouldCache: Boolean = false
    )

    public companion object {
        public const val MAX_CELL_WEIGHT: Int = 64
    }
}
