package org.ton.sdk.cell.boc

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.io.*
import kotlinx.io.bytestring.ByteString
import org.ton.sdk.bitstring.BitString
import org.ton.sdk.bitstring.unsafe.UnsafeBitStringOperations
import org.ton.sdk.cell.*
import org.ton.sdk.cell.boc.internal.ByteArrayRandomAccessStorage
import org.ton.sdk.cell.boc.internal.ByteStringRandomAccessSource
import org.ton.sdk.cell.boc.internal.RandomAccessSource
import org.ton.sdk.cell.boc.internal.readLong
import org.ton.sdk.cell.internal.DataCell
import org.ton.sdk.crypto.HashBytes

private const val HASH_BYTES = 32
private const val DEPTH_BYTES = 2

public class StaticBagOfCells internal constructor(
    private val source: RandomAccessSource,
    private val checkHashes: Boolean = true
) : BagOfCells(), CellContext {
    public constructor(byteArray: ByteArray) : this(ByteArrayRandomAccessStorage(byteArray))
    public constructor(byteString: ByteString) : this(ByteStringRandomAccessSource(byteString))

    public val header: BagOfCellsHeader by lazy {
        readHeader()
    }
    private val cachedCells = CellCache()
    private val cachedLocations = ArrayList<CellLocation>()

    public override fun getRootCell(index: Int): LoadedCell {
        if (index >= header.rootCount) {
            throw IllegalArgumentException("Invalid root index: $index, rootCount=${header.rootCount}")
        }
        val cellIndex = loadRootIndex(index)
        val dataCell = toLoadedCell(loadSerializedCell(cellIndex))
        return RootCell(this, index, dataCell)
    }

    private fun loadSerializedCell(index: Int): Cell {
        val cellLocation = cachedLocations.getOrElse(index) {
            getCellLocation(index)
        }
        val buffer = Buffer()

        var descriptor = cellLocation.descriptor
        if (descriptor == null) {
            source.position = cellLocation.start
            source.readAtMostTo(buffer, 2)
            val d1 = buffer.readByte()
            val d2 = buffer.readByte()
            descriptor = CellDescriptor(d1, d2)
        }
        source.position = cellLocation.start + 2
        require(descriptor.referenceCount in 0..4) {
            "Invalid BOC cell #$index has invalid reference count: ${descriptor.referenceCount}"
        }

        fun loadBits(): BitString {
            val dataLength = descriptor.byteLength
            source.readAtMostTo(buffer, dataLength.toLong())
            val data = buffer.readByteArray(dataLength)
            val bitLength = if (descriptor.isAligned) {
                data.size * 8
            } else {
                data.size * 8 - data.last().countTrailingZeroBits() - 1
            }
            @Suppress("OPT_IN_USAGE")
            return UnsafeBitStringOperations.wrapUnsafe(data, bitLength)
        }

        fun loadRefs(): List<Cell> {
            return List(descriptor.referenceCount) {
                source.readAtMostTo(buffer, header.refByteSize.toLong())
                val refIndex = buffer.readLong(header.refByteSize).toInt()
                check(refIndex < header.cellCount) {
                    "Invalid BOC cell #$index refers ($it) to cell #$refIndex which too big, cellCount=${header.cellCount}"
                }
                check(index < refIndex) {
                    "Invalid BOC cell #$index refers to cell #$refIndex which is a backward reference"
                }
                loadAnyCell(refIndex)
            }
        }

        val hashHashes = descriptor.hasHashes
        val hashes: Array<HashBytes>
        val depths: IntArray
        val cell = if (hashHashes) {
            val hashCount = descriptor.levelMask.hashCount
            source.readAtMostTo(buffer, (HASH_BYTES + 2L) * hashCount)
            hashes = Array(hashCount) {
                HashBytes(buffer.readByteString(HASH_BYTES))
            }
            depths = IntArray(hashCount) {
                buffer.readUShort().toInt()
            }

            val bits = loadBits()
            val refs = loadRefs()

            val cell = DataCell(descriptor, bits, refs, hashes, depths)
            if (checkHashes) {
                val expectedCell = with(CellBuilder()) {
                    store(bits)
                    refs.forEach {
                        storeReference(it)
                    }
                    build(descriptor.isExotic)
                }
                check(expectedCell.descriptor == cell.descriptor) {
                    "Invalid BOC cell #$index descriptor: expected=${expectedCell.descriptor}, found=${cell.descriptor}"
                }
                for (level in 0 until descriptor.levelMask.level) {
                    val expectedDepth = expectedCell.depth(level)
                    val actualDepth = cell.depth(level)
                    check(expectedDepth == actualDepth) {
                        "Invalid BOC cell #$index depth at level $level: expected=$expectedDepth, found=$actualDepth"
                    }

                    val expectedHash = expectedCell.hash(level)
                    val actualHash = cell.hash(level)
                    check(expectedHash == actualHash) {
                        "Invalid BOC cell #$index hash at level $level: expected=$expectedHash, found=$actualHash"
                    }
                }
                expectedCell
            } else {
                cell
            }
        } else {
            val bits = loadBits()
            val refs = loadRefs()
            with(CellBuilder()) {
                store(bits)
                refs.forEach {
                    storeReference(it)
                }
                build(descriptor.isExotic)
            }
        }

        if (cellLocation.shouldCache) {
            cachedCells[index] = cell
        }
        return cell
    }

    private fun loadAnyCell(index: Int): Cell {
        return loadSerializedCell(index)
    }

    private fun getCellLocation(index: Int): CellLocation {
        require(index in 0 until header.cellCount) {
            "Invalid cell index: $index, cellCount=${header.cellCount}"
        }
        if (!header.hasIndex) {
            println("Warning: BOC does not have index, using linear search for cell #$index")
            if (index < cachedLocations.size) {
                return cachedLocations[index]
            }
            var lastLocation = cachedLocations.lastOrNull()
            val buffer = source.buffered()
            for (i in cachedLocations.size..index) {
                source.position = lastLocation?.end ?: header.dataOffset
                buffer.request(2)
                val d1 = buffer.readByte()
                val d2 = buffer.readByte()
                val descriptor = CellDescriptor(d1, d2)
                val endOffset = descriptor.bytesCount(header.refByteSize)
                val location = CellLocation(source.position - 2, source.position + endOffset, false, descriptor)
                cachedLocations.add(location)
                lastLocation = location
            }
            return lastLocation ?: error("Failed to found cell location for #$index")
        }

        fun Source.loadIndexOffset(index: Int): Long {
            source.position = header.indexOffset + (index.toLong() * header.offsetByteSize)
            return readLong(header.offsetByteSize)
        }

        val buffer = source.buffered()

        var start = buffer.loadIndexOffset(index - 1)
        var end = buffer.loadIndexOffset(index)
        var shouldCache = true
        if (header.hasCacheBits) {
            shouldCache = (end and 1) != 0L
            start = start ushr 1
            end = end ushr 1
        }
        val dataOffset = header.dataOffset
        start += dataOffset
        end += dataOffset
        return CellLocation(start, end, shouldCache, null)
    }

    private fun loadRootIndex(rootIndex: Int): Int {
        require(rootIndex in 0..header.rootCount) {
            "Invalid root index: $rootIndex, rootCount=${header.rootCount}"
        }
        if (!header.hasRoots) {
            return 0
        }
        source.position = header.rootsOffset + (rootIndex.toLong() * header.refByteSize)
        val buffer = source.buffered()
        return buffer.readLong(header.refByteSize).toInt()
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun readHeader(): BagOfCellsHeader {
        source.position = 0
        return BagOfCellsHeader.parse(source.buffered())
    }

    override fun toString(): String = "StaticBagOfCells(header=$header)"

    override suspend fun loadCell(cell: Cell): LoadedCell = toLoadedCell(cell)

    private fun toLoadedCell(cell: Cell): LoadedCell {
        return when (cell) {
            is LoadedCell -> cell
            is RootCell -> toLoadedCell(cell.cell)
            is BocCell -> toLoadedCell(cell.cell)
            else -> throw IllegalArgumentException("Can't load ${cell::class} $cell")
        }
    }

    override fun finalizeCell(builder: CellBuilder): Cell {
        TODO("Not yet implemented")
    }

    public class RootCell(
        public val boc: StaticBagOfCells,
        public val index: Int,
        public val cell: LoadedCell
    ) : LoadedCell by cell {
        override fun toString(): String = "RootCell(index=$index, cell=$cell)"
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
        val shouldCache: Boolean,
        val descriptor: CellDescriptor?,
    )

    private class CellCache {
        private val lock = reentrantLock()
        private val cells = HashMap<Int, Cell>()

        operator fun set(index: Int, cell: Cell) = lock.withLock {
            cells[index] = cell
        }

        operator fun get(index: Int): Cell? {
            if (lock.tryLock()) {
                try {
                    return cells[index]
                } finally {
                    lock.unlock()
                }
            }
            return null
        }
    }

    private fun CellDescriptor.bytesCount(refByteSize: Int): Int {
        val n = hashCount
        val hasHashes = hasHashes
        val depthOffset = if (hasHashes) n * HASH_BYTES else 0
        val dataOffset = depthOffset + if (hasHashes) n * DEPTH_BYTES else 0
        val refsOffset = dataOffset + byteLength
        return refsOffset + referenceCount * refByteSize
    }
}
