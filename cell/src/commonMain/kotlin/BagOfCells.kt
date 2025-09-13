package org.ton.kotlin.cell

import kotlinx.io.Buffer
import kotlinx.io.buffered
import kotlinx.io.bytestring.ByteString
import kotlinx.io.readByteString
import org.ton.kotlin.bitstring.ByteWrappingBitString
import org.ton.kotlin.cell.internal.ByteArrayRandomAccessStorage
import org.ton.kotlin.cell.internal.ByteStringRandomAccessSource
import org.ton.kotlin.cell.internal.RandomAccessSource
import org.ton.kotlin.cell.internal.readLong

public sealed interface BagOfCells : CellContext {
    public val header: BagOfCellsHeader

    public fun getRootCell(index: Int = 0): Cell
}

public fun BagOfCells(byteArray: ByteArray): BagOfCells = StaticBagOfCells(byteArray)
public fun BagOfCells(byteString: ByteString): BagOfCells = StaticBagOfCells(byteString)

public val BagOfCells.rootCount: Int get() = header.rootCount

public operator fun BagOfCells.get(index: Int): Cell = getRootCell(index)

public class StaticBagOfCells internal constructor(
    private val source: RandomAccessSource
) : BagOfCells, CellContext {
    public constructor(byteArray: ByteArray) : this(ByteArrayRandomAccessStorage(byteArray))
    public constructor(byteString: ByteString) : this(ByteStringRandomAccessSource(byteString))

    public override val header: BagOfCellsHeader by lazy {
        readHeader()
    }
    private val cachedCells = mutableMapOf<Int, DataCell>()
    private val cachedDescriptors = ArrayList<Pair<CellDescriptor, CellLocation>>()

    public override fun getRootCell(index: Int): Cell {
        val cellIndex = loadRootIndex(index)
        val dataCell = loadDataCell(cellIndex)
        return dataCell
    }

    private fun loadDataCell(index: Int): DataCell {
        cachedCells[index]?.let {
            return it
        }

        val (descriptor, cellLocation) = cachedDescriptors.getOrElse(index) {
            val cellLocation = getCellLocation(index)
            val buffer = Buffer()
            source.position = cellLocation.start
            source.readAtMostTo(buffer, 2)

            val d1 = buffer.readByte()
            val d2 = buffer.readByte()
            CellDescriptor(d1, d2) to cellLocation
        }

        source.position = cellLocation.start + 2
        val buffer = source.buffered()
        cachedDescriptors.add(Pair(descriptor, cellLocation))
        if (descriptor.isAbsent) {
            throw IllegalStateException("Cell is absent")
        }
        val hashCount = descriptor.levelMask.hashCount
        val hasHashes = descriptor.hasHashes

        val hashes = List(if (hasHashes) hashCount else 0) {
            buffer.readByteString(Cell.HASH_BYTES)
        }
        val depths = List(if (hasHashes) hashCount else 0) {
            buffer.readShort()
        }
        val dataLength = descriptor.dataLength
        val rawData = ByteArray(dataLength + if (descriptor.isAligned) 1 else 0)
        buffer.readAtMostTo(rawData, 0, dataLength)
        if (descriptor.isAligned) {
            rawData[dataLength] = 0b1000_0000.toByte()
        }
        val bits = ByteWrappingBitString(rawData)
        val refsCount = descriptor.referenceCount
        val refsIndexes = List(refsCount) {
            val refIndex = buffer.readLong(header.refByteSize).toInt()
            if (refIndex >= header.cellCount) {
                throw IllegalStateException("Invalid BOC cell #$index refers ($it) to cell #$refIndex which too big, cellCount=${header.cellCount}")
            }
            if (index >= refIndex) {
                throw IllegalStateException("Invalid BOC cell #$index refers ($it) to cell #$refIndex which is backward reference")
            }
            refIndex
        }

        val references = refsIndexes.map {
            when (val ref = loadAnyCell(it)) {
                is DataCell -> BocLoadedCell(it, ref)
                is ExternalCell -> BocCell(it, ref)
                else -> ref
            }
        }

        val cell = DataCell(descriptor, hashes, depths, bits, references)
//        val cell = DataCell.create(descriptor, data, refs)
//        if (hasHashes) {
//            check(cell.hashes == hashes) {
//                "Invalid hashes: provided: $hashes, calculated: ${cell.hashes}"
//            }
//            check(cell.depths == depths) {
//                "Invalid depths: provided: $depths, calculated: ${cell.depths}"
//            }
//        }

        if (cellLocation.shouldCache) {
            cachedCells[index] = cell
        }
        return cell
    }

    public fun loadAnyCell(index: Int): Cell {
        cachedCells[index]?.let {
            return it
        }

        val cellLocation = getCellLocation(index)
        val buffer = Buffer()
        source.position = cellLocation.start
        source.readAtMostTo(buffer, cellLocation.end - cellLocation.start)

        val descriptor = CellDescriptor(buffer.readByte(), buffer.readByte())
        if (!descriptor.hasHashes) {
            return loadDataCell(index)
        }

        val hashCount = descriptor.levelMask.hashCount
        val hashes = List(hashCount) {
            buffer.readByteString(Cell.HASH_BYTES)
        }
        val depths = List(hashCount) {
            buffer.readShort()
        }

        return ExternalCell(descriptor, hashes, depths, index)
    }

    private fun getCellLocation(index: Int): CellLocation {
        require(index in 0 until header.cellCount) {
            "Invalid cell index: $index, cellCount=${header.cellCount}"
        }
        if (!header.hasIndex) {
            println("Warning: BOC does not have index, using linear search for cell #$index")
            if (index < cachedDescriptors.size) {
                return cachedDescriptors[index].second
            }
            var lastLocation = cachedDescriptors.lastOrNull()?.second
            val buffer = Buffer()
            for (i in cachedDescriptors.size..index) {
                source.position = lastLocation?.end ?: header.dataOffset
                source.readAtMostTo(buffer, 2)
                val d1 = buffer.readByte()
                val d2 = buffer.readByte()
                val descriptor = CellDescriptor(d1, d2)
                val endOffset = descriptor.bytesCount(header.refByteSize)
                val location = CellLocation(source.position - 2, source.position + endOffset, false)
                cachedDescriptors.add(descriptor to location)
                lastLocation = location
            }
            return lastLocation ?: error("Failed to found cell location for #$index")
        }


        var start = loadIndexOffset(index - 1)
        var end = loadIndexOffset(index)
        var shouldCache = true
        if (header.hasCacheBits) {
            shouldCache = (end and 1) != 0L
            start = start ushr 1
            end = end ushr 1
        }
        val dataOffset = header.dataOffset
        start += dataOffset
        end += dataOffset
        return CellLocation(start, end, shouldCache)
    }

    private fun loadIndexOffset(index: Int): Long {
        if (index < 0) return 0L
        val buffer = Buffer()
        if (header.hasIndex) {
            source.position = header.indexOffset + (index.toLong() * header.offsetByteSize)
            source.readAtMostTo(buffer, header.offsetByteSize.toLong())
        } else {
            throw IllegalStateException("Searching for index in a BOC without index")
        }
        return buffer.readLong(header.offsetByteSize)
    }

    private fun loadRootIndex(rootIndex: Int): Int {
        require(rootIndex in 0..rootCount) {
            "Invalid root index: $rootIndex, rootCount=$rootCount"
        }
        if (!header.hasRoots) {
            return 0
        }
        val buffer = Buffer()
        source.position = header.rootsOffset + (rootIndex.toLong() * header.refByteSize)
        source.readAtMostTo(buffer, header.refByteSize.toLong())
        return buffer.readLong(header.refByteSize).toInt()
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun readHeader(): BagOfCellsHeader {
        source.position = 0
        return BagOfCellsHeader.parse(source.buffered())
    }

    override fun toString(): String = "StaticBagOfCells(header=$header)"

    override fun loadCell(cell: Cell): DataCell {
        return when (cell) {
            is DataCell -> cell
            is BocCell -> loadCell(cell.cell)
            is BocLoadedCell -> loadCell(cell.cell)
            is ExternalCell -> loadDataCell(cell.index)
            else -> throw IllegalArgumentException("Unsupported cell type: $cell")
        }
    }

    override fun finalizeCell(cell: Cell): Cell {
        TODO("Not yet implemented")
    }

    private class BocCell(
        val index: Int,
        val cell: Cell,
    ) : Cell by cell {
        override fun toString(): String = "$index: $cell"
    }

    private class BocLoadedCell(
        val index: Int,
        val cell: LoadedCell,
    ) : LoadedCell by cell {
        override fun toString(): String = "$index: $cell"
    }

    private data class CellLocation(
        val start: Long,
        val end: Long,
        val shouldCache: Boolean
    )

    private fun CellDescriptor.bytesCount(refByteSize: Int): Int {
        val n = hashCount
        val hasHashes = hasHashes
        val depthOffset = if (hasHashes) n * Cell.HASH_BYTES else 0
        val dataOffset = depthOffset + if (hasHashes) n * Cell.DEPTH_BYTES else 0
        val refsOffset = dataOffset + dataLength
        return refsOffset + referenceCount * refByteSize
    }
}
