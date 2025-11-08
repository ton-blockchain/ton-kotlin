package org.ton.sdk.cell.boc

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.io.*
import kotlinx.io.unsafe.UnsafeBufferOperations
import kotlinx.io.unsafe.withData
import org.ton.sdk.cell.*
import org.ton.sdk.cell.boc.internal.*
import org.ton.sdk.cell.internal.DataCell
import org.ton.sdk.cell.internal.ExtCell
import org.ton.sdk.crypto.CRC32C
import kotlin.math.min

private const val HASH_BYTES = 32
private const val DEPTH_BYTES = 2

public class StaticBagOfCells internal constructor(
    private val source: SeekableRawSource,
    private val options: DecodeOptions
) : BagOfCells(), CellContext {
    public constructor(byteArray: ByteArray, decodeOptions: DecodeOptions) : this(
        ByteArraySeekableRawSource(byteArray),
        decodeOptions
    )

    public val header: BagOfCellsHeader by lazy {
        loadHeader()
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

    private fun deserializeCell(index: Int, source: Source, descriptor: CellDescriptor, shouldCache: Boolean): Cell {
        require(descriptor.referenceCount in 0..4) {
            "Invalid BOC cell #$index has invalid reference count: ${descriptor.referenceCount}"
        }

        fun loadRefs(): List<Cell> {
            return List(descriptor.referenceCount) {
                val refIndex = source.readLong(header.refByteSize).toInt()
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
        val cell = if (hashHashes) {
            val hashes = source.readHashes(descriptor.levelMask)
            val depths = source.readDepths(descriptor.levelMask)
            val bits = source.readBits(descriptor)
            val refs = loadRefs()
            val cell = DataCell(descriptor, bits, refs, hashes, depths)
            if (options.checkHashes) {
                val expectedCell = with(CellBuilder()) {
                    store(bits)
                    refs.forEach {
                        storeReference(it)
                    }
                    build(descriptor.isExotic)
                }
                for (level in 0 until descriptor.levelMask.level) {
                    if (level !in descriptor.levelMask) continue
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
            }
            cell
        } else {
            val bits = source.readBits(descriptor)
            val refs = loadRefs()
            with(CellBuilder()) {
                store(bits)
                refs.forEach {
                    storeReference(it)
                }
                build(descriptor.isExotic)
            }
        }

        if (shouldCache) {
            cachedCells[index] = cell
        }
        return cell
    }

    private fun deserializeAnyCell(index: Int, source: Source, descriptor: CellDescriptor, shouldCache: Boolean): Cell {
        if (options.lazyLoad && descriptor.hasHashes) {
            return ExtCell(descriptor, loader = { loadSerializedCell(index) })
        }
        return deserializeCell(index, source, descriptor, shouldCache)
    }

    private fun loadSerializedCell(index: Int): Cell {
        cachedCells[index]?.let { return it }
        val cellLocation = getCellLocation(index)
        source.position = cellLocation.start
        val buffer = source.buffer(cellLocation.end - cellLocation.start)

        var descriptor = cellLocation.descriptor
        if (descriptor == null) {
            val d1 = buffer.readByte()
            val d2 = buffer.readByte()
            descriptor = CellDescriptor(d1, d2)
        } else {
            buffer.skip(2)
        }
        return deserializeCell(index, buffer, descriptor, cellLocation.shouldCache)
    }

    private fun loadAnyCell(index: Int): Cell {
        cachedCells[index]?.let { return it }
        val cellLocation = getCellLocation(index)
        source.position = cellLocation.start
        val buffer = source.buffer(cellLocation.end - cellLocation.start)
        var descriptor = cellLocation.descriptor
        if (descriptor == null) {
            val d1 = buffer.readByte()
            val d2 = buffer.readByte()
            descriptor = CellDescriptor(d1, d2)
        } else {
            buffer.skip(2)
        }
        return deserializeAnyCell(index, buffer, descriptor, cellLocation.shouldCache)
    }

    private fun getCellLocation(index: Int): CellLocation {
        require(index in 0 until header.cellCount) {
            "Invalid cell index: $index, cellCount=${header.cellCount}"
        }
        cachedLocations.getOrNull(index)?.let { return it }
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
            if (index < 0) return 0
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

    @OptIn(InternalIoApi::class, UnsafeIoApi::class)
    private fun loadHeader(): BagOfCellsHeader {
        source.position = 0
        val buffer = source.buffered()
        if (!options.checkCrc32c) {
            return BagOfCellsHeader.parse(buffer)
        }
        val header = BagOfCellsHeader.parse(buffer.peek())
        if (header.hasCrc32c) {
            val crc32c = CRC32C()
            val dataSize = header.totalSize - 4
            var remaining = dataSize
            UnsafeBufferOperations.forEachSegment(buffer.buffer) { ctx, segment ->
                ctx.withData(segment) { data, startIndex, endIndex ->
                    val byteCount = endIndex - startIndex
                    if (remaining <= 0) return@withData
                    val toRead = min(remaining, byteCount.toLong()).toInt()
                    crc32c.update(data, startIndex, startIndex + toRead)
                    remaining -= toRead
                }
            }
            buffer.skip(dataSize)
            val expectedCrc32c = buffer.readIntLe()
            val actualCrc32c = crc32c.intDigest()
            check(expectedCrc32c == actualCrc32c) {
                "Invalid BOC CRC32C: expected=${expectedCrc32c.toUInt().toString(16)}, found=${
                    actualCrc32c.toUInt().toString(16)
                }"
            }
        }
        return header
    }

    override fun toString(): String = "StaticBagOfCells(header=$header)"

    override suspend fun loadCell(cell: Cell): LoadedCell = toLoadedCell(cell)

    private fun toLoadedCell(cell: Cell): LoadedCell {
        return when (cell) {
            is LoadedCell -> cell
            is RootCell -> toLoadedCell(cell.cell)
            is BocCell -> toLoadedCell(cell.cell)
            is ExtCell -> toLoadedCell(cell.cell)
            else -> throw IllegalArgumentException("Can't load ${cell::class} $cell")
        }
    }

    override fun finalizeCell(builder: CellBuilder): Cell {
        return CellContext.EMPTY.finalizeCell(builder)
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
