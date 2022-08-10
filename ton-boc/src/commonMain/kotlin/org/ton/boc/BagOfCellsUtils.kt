package org.ton.boc

import io.ktor.utils.io.core.*
import kotlinx.coroutines.DelicateCoroutinesApi
import org.ton.bitstring.BitString
import org.ton.cell.Cell
import org.ton.cell.LevelMask
import org.ton.crypto.crc32c

@OptIn(DelicateCoroutinesApi::class)
fun Input.readBagOfCell(): BagOfCells {
    val prefix = readInt()
    val hasIdx: Boolean
    val hashCrc32: Boolean
    val hasCacheBits: Boolean
    val flags: Int
    val refSize: Int
    when (prefix) {
        BagOfCells.BOC_GENERIC_MAGIC -> {
            val firstByte = readByte().toInt() and 0xFF
            hasIdx = (firstByte and 0b1000_0000) != 0
            hashCrc32 = (firstByte and 0b0100_0000) != 0
            hasCacheBits = (firstByte and 0b0010_0000) != 0
            flags = (firstByte and 16) * 2 + (firstByte and 8)
            refSize = firstByte and 0b0000_0111
        }

        BagOfCells.BOC_INDEXED_MAGIC -> {
            TODO()
//            hasIdx = true
//            hashCrc32 = false
//            hasCacheBits = false
//            flags = 0
//            sizeBytes = readByte().toInt()
        }

        BagOfCells.BOC_INDEXED_CRC32C_MAGIC -> {
            TODO()
//            hasIdx = true
//            hashCrc32 = true
//            hasCacheBits = false
//            flags = 0
//            sizeBytes = readByte().toInt()
        }

        else -> throw IllegalArgumentException("Unknown magic prefix: ${prefix.toString(16)}")
    }

    // Counters
    val offsetSize = readByte().toInt()
    val cellCount = readInt(refSize)
    val rootsCount = readInt(refSize)
    val absentCount = readInt(refSize)
    val totalCellsSize = readInt(offsetSize)

    // Roots
    val rootIndexes = IntArray(rootsCount) {
        readInt(refSize)
    }

    val indexes: IntArray?
    if (hasIdx) {
        var prevOffset = 0
        indexes = IntArray(cellCount)
        repeat(cellCount) { index ->
            var offset = readInt(offsetSize)
            if (hasCacheBits) {
                offset = offset shr 1
            }
            check(prevOffset <= offset) { "bag-of-cells error: offset of cell #$index must be higher, than $prevOffset" }
            indexes[index] = offset
            prevOffset = offset
        }
    } else {
        indexes = null
    }

    val cellBits = Array(cellCount) { BitString() }
    val cellRefs = Array(cellCount) { intArrayOf() }
    val cellExotics = BooleanArray(cellCount) { false }

    repeat(cellCount) { cellIndex ->
        val d1 = readByte().toInt() and 0xFF
        val level = d1 shr 5
        val hasHashes = (d1 and 0b0001_0000) != 0
        val isExotic = (d1 and 0b0000_1000) != 0
        val refsCount = d1 and 0b0000_0111
        val isAbsent = refsCount == 0b0000_0111 && hasHashes

        // For absent cells (i.e., external references), only d1 is present, always equal to 23 + 32l.
        if (isAbsent) {
            TODO()
        } else {
            require(refsCount in 0..4) {
                "refsCount expected: 0..4 actual: $refsCount"
            }

            val d2 = readByte().toInt() and 0xFF
            val fullFilledBytes = d2 and 1 == 0
            val dataSize = (d2 shr 1) + if (fullFilledBytes) 0 else 1

            if (hasHashes) {
                val hashCount = LevelMask(level).level + 1
                val hashes = Array(hashCount) {
                    readBytes(32)
                }
                val depth = IntArray(hashCount) {
                    readShort().toInt()
                }
            }

            var cellData = readBytes(dataSize)
            if (fullFilledBytes) {
                cellData = BitString.appendAugmentTag(cellData, dataSize * 8)
            }
            val cellSize = BitString.findAugmentTag(cellData)
            cellBits[cellIndex] = BitString(cellData, cellSize)
            cellRefs[cellIndex] = IntArray(refsCount) { k ->
                val refIndex = readInt(refSize)
                check(refIndex > cellIndex) { "bag-of-cells error: reference #$k of cell #$cellIndex is to cell #$refIndex with smaller index" }
                check(refIndex < cellCount) { "bag-of-cells error: reference #$k of cell #$cellIndex is to non-existent cell #$refIndex, only $cellCount cells are defined" }
                refIndex
            }
            cellExotics[cellIndex] = isExotic
        }
    }

    // Resolving references & constructing cells from leaves to roots
    val cells = Array<Cell?>(cellCount) { null }
    List(cellCount) { cellIndex ->
        createCell(cellIndex, cells, cellBits, cellRefs, cellExotics)
    }

    // TODO: Crc32c check (calculate size of resulting bytearray)
    if (hashCrc32) {
        readIntLittleEndian()
    }

    val roots = rootIndexes.map { rootIndex ->
        requireNotNull(cells[rootIndex])
    }

    return BagOfCells(roots)
}

private fun createCell(
    index: Int,
    cells: Array<Cell?>,
    bits: Array<BitString>,
    refs: Array<IntArray>,
    exotics: BooleanArray
): Cell {
    var cell = cells[index]
    if (cell != null) {
        return cell
    }
    val cellBits = bits[index]
    val cellRefIndexes = refs[index]
    val cellRefs = cellRefIndexes.map { refIndex ->
        createCell(refIndex, cells, bits, refs, exotics)
    }
    cell = Cell(cellBits, cellRefs, exotics[index])
    cells[index] = cell
    return cell
}

fun Output.writeBagOfCells(
    bagOfCells: BagOfCells,
    hasIndex: Boolean = false,
    hasCrc32c: Boolean = false,
    hasCacheBits: Boolean = false,
    flags: Int = 0
) {
    val serializedBagOfCells = serializeBagOfCells(bagOfCells, hasIndex, hasCrc32c, hasCacheBits, flags)
    if (hasCrc32c) {
        val crc32c = crc32c(serializedBagOfCells).toInt()
        writeFully(serializedBagOfCells)
        writeIntLittleEndian(crc32c)
    } else {
        writeFully(serializedBagOfCells)
    }
}

private fun serializeBagOfCells(
    bagOfCells: BagOfCells,
    hasIndex: Boolean,
    hasCrc32c: Boolean,
    hasCacheBits: Boolean,
    flags: Int
): ByteArray = buildPacket {
    val cells = bagOfCells.toList()
    val cellsCount = cells.size
    val rootsCount = bagOfCells.roots.size
    var sizeBytes = 0
    while (cellsCount >= (1L shl (sizeBytes shl 3))) {
        sizeBytes++
    }

    val serializedCells = cells.mapIndexed { index: Int, cell: Cell ->
        buildPacket {
            val d1 = cell.getRefsDescriptor()
            writeByte(d1)
            val d2 = cell.getBitsDescriptor()
            writeByte(d2)
            val cellData = cell.bits.toByteArray(augment = true)
            writeFully(cellData)
            cell.refs.forEach { reference ->
                val refIndex = cells.indexOf(reference)
                writeInt(refIndex, sizeBytes)
            }
        }
    }

    var fullSize = 0
    val sizeIndex = ArrayList<Int>()
    serializedCells.forEach { serializedCell ->
        sizeIndex.add(fullSize)
        fullSize += serializedCell.remaining.toInt()
    }
    var offsetBytes = 0
    while (fullSize >= (1L shl (offsetBytes shl 3))) {
        offsetBytes++
    }

    writeInt(BagOfCells.BOC_GENERIC_MAGIC)

    var flagsByte = 0
    if (hasIndex) {
        flagsByte = flagsByte or (1 shl 7) // has_idx:(## 1)
    }
    if (hasCrc32c) {
        flagsByte = flagsByte or (1 shl 6) // has_crc32c:(## 1)
    }
    if (hasCacheBits) {
        flagsByte = flagsByte or (1 shl 5) // has_cache_bits:(## 1)
    }
    flagsByte = flagsByte or flags // flags:(## 2) { flags = 0 }
    flagsByte = flagsByte or sizeBytes // size:(## 3) { size <= 4 }
    writeByte(flagsByte.toByte())

    writeByte(offsetBytes.toByte()) // off_bytes:(## 8) { off_bytes <= 8 }
    writeInt(cellsCount, sizeBytes) // cells:(##(size * 8))
    writeInt(rootsCount, sizeBytes) // roots:(##(size * 8)) { roots >= 1 }
    writeInt(0, sizeBytes) // absent:(##(size * 8)) { roots + absent <= cells }
    writeInt(fullSize, offsetBytes) // tot_cells_size:(##(off_bytes * 8))
    bagOfCells.roots.forEach { root ->
        val rootIndex = cells.indexOf(root)
        writeInt(rootIndex, sizeBytes)
    }
    if (hasIndex) {
        serializedCells.forEachIndexed { index, _ ->
            writeInt(sizeIndex[index], offsetBytes)
        }
    }
    serializedCells.forEach { serializedCell ->
        val bytes = serializedCell.readBytes()
        writeFully(bytes)
    }
}.readBytes()

@OptIn(ExperimentalUnsignedTypes::class)
private fun Input.readInt(bytes: Int): Int {
    return when (bytes) {
        1 -> readUByte().toInt()
        2 -> readUShort().toInt()
        3 -> (readUByte().toInt() shl 16) or (readUByte().toInt() shl 8) or (readUByte().toInt())
        else -> readUInt().toInt()
    }
}

private fun Output.writeInt(value: Int, bytes: Int) {
    when (bytes) {
        1 -> writeByte(value.toByte())
        2 -> writeShort(value.toShort())
        3 -> {
            writeByte((value shr Short.SIZE_BITS).toByte())
            writeShort(value.toShort())
        }

        else -> writeInt(value)
    }
}
