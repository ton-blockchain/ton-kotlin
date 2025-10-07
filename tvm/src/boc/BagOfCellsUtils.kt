package org.ton.boc

import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.io.*
import org.ton.bitstring.BitString
import org.ton.cell.Cell
import org.ton.cell.CellDescriptor
import org.ton.cell.buildCell
import org.ton.kotlin.crypto.crc32c

internal fun Input.readBagOfCell(): BagOfCells {
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

        else -> throw IllegalArgumentException("Unknown magic prefix: ${prefix.toUInt().toString(16)}")
    }

    // Counters
    val offsetSize = readByte().toInt()
    val cellCount = readInt(refSize)
    val rootsCount = readInt(refSize)
    readInt(refSize)
    readInt(offsetSize)

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
    val cellDescriptors = Array(cellCount) { CellDescriptor(0, 0) }

//    measureTime {
    val cellHashes = Array<List<Pair<ByteArray, Int>>?>(cellCount) { null }

    repeat(cellCount) { cellIndex ->
        val d1 = readByte()
        val d2 = readByte()
        val descriptor = CellDescriptor(d1, d2)

        if (descriptor.hasHashes) {
            val hashes = ArrayList<ByteArray>(descriptor.hashCount)
            val depths = ArrayList<Int>(descriptor.hashCount)
            repeat(descriptor.hashCount) {
                hashes.add(readByteArray(Cell.HASH_BYTES))
            }
            repeat(descriptor.hashCount) {
                depths.add(readInt(2))
            }
            cellHashes[cellIndex] = hashes.zip(depths)
        }

        val cellData = readByteArray(descriptor.dataLength)
        val cellSize = if (descriptor.isAligned) descriptor.dataLength * Byte.SIZE_BITS else findAugmentTag(cellData)
        cellBits[cellIndex] = BitString(cellData, cellSize)
        cellRefs[cellIndex] = IntArray(descriptor.referenceCount) { k ->
            val refIndex = readInt(refSize)
            check(refIndex > cellIndex) { "bag-of-cells error: reference #$k of cell #$cellIndex is to cell #$refIndex with smaller index" }
            check(refIndex < cellCount) { "bag-of-cells error: reference #$k of cell #$cellIndex is to non-existent cell #$refIndex, only $cellCount cells are defined" }
            refIndex
        }
        cellDescriptors[cellIndex] = descriptor
    }
//    }.let {
//        println("read cell data time: $it")
//    }

    // Resolving references & constructing cells from leaves to roots
    val asyncCells = Array<CompletableDeferred<Cell>>(cellCount) { CompletableDeferred() }
    GlobalScope.launch {
        repeat(cellCount) { cellIndex ->
            launch {
                createCell(cellIndex, asyncCells, cellBits, cellRefs, cellDescriptors, cellHashes)
            }
        }
    }

    // TODO: Crc32c check (calculate size of resulting bytearray)
    if (hashCrc32) {
        readIntLe()
    }

    val cells = runBlocking {
        asyncCells.toList().awaitAll()
    }

    val roots = rootIndexes.map { rootIndex ->
        cells[rootIndex]
    }

    return object : BagOfCells {
        override val roots: List<Cell> = roots

        override fun toString(): String = buildString {
            roots.forEachIndexed { _, cell ->
                Cell.toString(cell, this)
            }
        }

        override fun iterator(): Iterator<Cell> = cells.iterator()
    }
}

private suspend fun createCell(
    index: Int,
    cells: Array<CompletableDeferred<Cell>>,
    bits: Array<BitString>,
    refs: Array<IntArray>,
    descriptors: Array<CellDescriptor>,
    cellHashes: Array<List<Pair<ByteArray, Int>>?>
) = coroutineScope {
    val cellBits = bits[index]
    val cellRefIndexes = refs[index]
    val cellRefs = cellRefIndexes.map { refIndex ->
        cells[refIndex].await()
    }
    val descriptor = descriptors[index]
    cellHashes[index]
//    val cell = if (!descriptors[index].isExotic && hashes != null) {
//        val new = buildCell {
//            isExotic = descriptor.isExotic
//            levelMask = descriptor.levelMask
//            storeBits(cellBits)
//            storeRefs(cellRefs)
//        }
//        fun List<Pair<ByteArray, Int>>.print() = map {
//            it.first.toHexString()+"="+it.second
//        }
//        DataCell(descriptor, cellBits, cellRefs, hashes).also {
//            if (new is DataCell && new.hashes != it.hashes) {
////                println("\nnew:${new.hashes.print()}\nit:${it.hashes.print()}")
//            } else {
//                println("\nWOW: ${it.hashes.print()}")
//            }
//        }
//        new
//    } else {
//        buildCell {
//            isExotic = descriptor.isExotic
//            levelMask = descriptor.levelMask
//            storeBits(cellBits)
//            storeRefs(cellRefs)
//        }
//    }
    val cell = buildCell {
        isExotic = descriptor.isExotic
        levelMask = descriptor.levelMask
        storeBits(cellBits)
        storeRefs(cellRefs)
    }
    cells[index].complete(cell)
}

//private fun createCell(
//    index: Int,
//    cells: Array<Cell?>,
//    bits: Array<BitString>,
//    refs: Array<IntArray>,
//    exotics: BooleanArray
//): Cell {
////    println("$index creating")
//    val cell = cells[index]
//    if (cell != null) {
////        println("$index already created")
//        return cell
//    }
//    val cellBits = bits[index]
//    val cellRefIndexes = refs[index]
//    val cellRefs = cellRefIndexes.map { refIndex ->
//        createCell(refIndex, cells, bits, refs, exotics)
//    }
////    println("$index async")
//    return Cell(cellBits, cellRefs, exotics[index])
//}

internal fun Output.writeBagOfCells(
    bagOfCells: BagOfCells,
    hasIndex: Boolean = false,
    hasCrc32c: Boolean = false,
    hasCacheBits: Boolean = false,
    flags: Int = 0
) {
    val serializedBagOfCells = serializeBagOfCells(bagOfCells, hasIndex, hasCrc32c, hasCacheBits, flags)
    if (hasCrc32c) {
        val crc32c = crc32c(serializedBagOfCells)
        writeFully(serializedBagOfCells)
        writeIntLe(crc32c)
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
        val serializedCell = buildPacket {
            val descriptor = cell.descriptor
            writeByte(descriptor.d1)
            writeByte(descriptor.d2)
            val cellData = cell.bits.toByteArray(true)
            writeFully(cellData)
            cell.refs.forEach { reference ->
                val refIndex = cells.indexOf(reference)
                writeInt(refIndex, sizeBytes)
            }
        }.readByteArray()
        serializedCell
    }

    var fullSize = 0
    val sizeIndex = ArrayList<Int>()
    serializedCells.forEach { serializedCell ->
        sizeIndex.add(fullSize)
        fullSize += serializedCell.size
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
        writeFully(serializedCell)
    }
}.readByteArray()

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

private fun findAugmentTag(byteArray: ByteArray): Int {
    if (byteArray.isEmpty()) return 0
    var length = byteArray.size * 8
    var index = byteArray.lastIndex
    while (true) {
        val byte = byteArray[index--].toInt()
        if (byte == 0) {
            length -= 8
        } else {
            var skip = 1
            var mask = 1
            while (byte and mask == 0) {
                skip++
                mask = mask shl 1
            }
            length -= skip
            break
        }
    }
    return length
}
