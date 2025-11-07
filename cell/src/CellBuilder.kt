package org.ton.sdk.cell

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.unsafe.UnsafeByteStringApi
import kotlinx.io.bytestring.unsafe.UnsafeByteStringOperations
import org.ton.sdk.bigint.BigInt
import org.ton.sdk.bitstring.BitString
import org.ton.sdk.bitstring.BitStringBuilder
import org.ton.sdk.cell.internal.DataCell
import org.ton.sdk.cell.internal.LibraryCell
import org.ton.sdk.cell.internal.PrunedCell
import org.ton.sdk.crypto.HashBytes
import org.ton.sdk.crypto.Sha256
import kotlin.jvm.JvmStatic
import kotlin.math.max
import kotlin.math.min

public class CellBuilder() {
    private val bitsBuilder = BitStringBuilder()
    private val references = ArrayList<Cell>(4)

    public val bits: Int get() = bitsBuilder.bitLength
    public val refs: Int get() = references.size

    public val bitsRemaining: Int get() = Cell.MAX_BIT_LENGHT - bitsBuilder.bitLength
    public val refsRemaining: Int get() = 4 - references.size

    public fun store(
        source: BitString,
        startIndex: Int = 0,
        endIndex: Int = source.size
    ): CellBuilder = apply {
        checkBits(endIndex - startIndex)
        bitsBuilder.write(source, startIndex, endIndex)
    }

    public fun store(
        source: ByteArray,
        startIndex: Int = 0,
        endIndex: Int = source.size
    ): CellBuilder = apply {
        checkBits((endIndex - startIndex) * 8)
        bitsBuilder.write(source, startIndex, endIndex)
    }

    public fun store(
        source: ByteString,
        startIndex: Int = 0,
        endIndex: Int = source.size
    ): CellBuilder = apply {
        checkBits((endIndex - startIndex) * 8)
        bitsBuilder.write(source, startIndex, endIndex)
    }

    public fun store(source: HashBytes): CellBuilder = apply {
        checkBits(32)
        bitsBuilder.write(source.value)
    }

    public fun storeInt(value: Int, bitCount: Int = Int.SIZE_BITS): CellBuilder = apply {
        checkBits(bitCount)
        bitsBuilder.writeInt(value, bitCount)
    }

    public fun storeUInt(value: Int, bitCount: Int = Int.SIZE_BITS): CellBuilder = apply {
        checkBits(bitCount)
        bitsBuilder.writeUInt(value, bitCount)
    }

    public fun storeLong(value: Long, bitCount: Int = Long.SIZE_BITS): CellBuilder = apply {
        checkBits(bitCount)
        bitsBuilder.writeLong(value, bitCount)
    }

    public fun storeULong(value: Long, bitCount: Int = Long.SIZE_BITS): CellBuilder = apply {
        checkBits(bitCount)
        bitsBuilder.writeULong(value, bitCount)
    }

    public fun storeBigInt(value: BigInt, bitCount: Int): CellBuilder = apply {
        checkBits(bitCount)
        bitsBuilder.writeBigInt(value, bitCount)
    }

    public fun storeReference(cell: Cell): CellBuilder = apply {
        checkRefs(1)
        references.add(cell)
    }

    public fun append(builder: CellBuilder): CellBuilder = apply {
        checkBits(builder.bits)
        checkRefs(builder.refs)
        bitsBuilder.write(builder.bitsBuilder.toBitString())
        references.addAll(builder.references)
    }

    public fun build(
        context: CellContext = CellContext.EMPTY,
        isExotic: Boolean = false
    ): Cell = context.finalizeCell(this, isExotic)

    public fun build(): Cell = build(isExotic = false)

    @OptIn(UnsafeByteStringApi::class)
    public fun build(isExotic: Boolean): Cell {
        val data = bitsBuilder.buffer
        val cellType = if (isExotic) {
            require(bits >= 8) { "Exotic cells must have at least 8 bits" }
            when (val byte = data[0].toInt()) {
                1 -> CellType.PRUNED_BRANCH
                2 -> CellType.LIBRARY_REFERENCE
                3 -> CellType.MERKLE_PROOF
                4 -> CellType.MERKLE_UPDATE
                else -> throw IllegalArgumentException("Invalid cell type byte: $byte, isExotic=true")
            }
        } else {
            CellType.ORDINARY
        }

        var levelMask = LevelMask.EMPTY
        val depths = IntArray(LevelMask.MAX_LEVEL + 1)
        when (cellType) {
            CellType.ORDINARY -> {
                for (i in 0 until references.size) {
                    val ref = references[i]
                    levelMask = levelMask or ref.levelMask
                    for (j in 0..LevelMask.MAX_LEVEL) {
                        depths[j] = max(depths[j], ref.depth(j))
                    }
                }

                if (references.isNotEmpty()) {
                    for (j in 0..LevelMask.MAX_LEVEL) {
                        depths[j]++
                    }
                }
            }

            CellType.PRUNED_BRANCH -> {
                require(references.isEmpty()) { "Pruned branch cannot have references" }
                require(bits >= 16) { "Pruned branch must have at least 16 bits" }
                val mask = data[1].toInt()
                levelMask = LevelMask(mask)
                require(levelMask.level != 0 && levelMask.level <= LevelMask.MAX_LEVEL) {
                    "Invalid level mask for pruned branch: ${(mask and 0xFF)}"
                }

                val hashesCount = levelMask.hashIndex
                val expectedByteSize = 2 + hashesCount * (32 + 2)
                val expectedBitSize = expectedByteSize * 8

                require(bits == expectedBitSize) {
                    "Pruned branch expected data bits size: ${expectedBitSize}, actual: $bits"
                }

                for (i in (LevelMask.MAX_LEVEL - 1) downTo 0) {
                    if (levelMask.contains(i + 1)) {
                        val hashesBefore = levelMask.apply(i).hashIndex
                        val offset = 2 + hashesCount * 32 + hashesBefore * 2
                        depths[i] = data[offset].toInt() and 0xFF shl 8 or
                                (data[offset + 1].toInt() and 0xFF)
                    } else {
                        depths[i] = depths[i + 1]
                    }
                }
            }

            CellType.LIBRARY_REFERENCE -> {
                check(references.isEmpty()) {
                    "Library reference cannot have references"
                }
                check(bits == 8 * (1 + 32)) {
                    "Library reference must have exactly 8 + 256 bits"
                }
            }

            CellType.MERKLE_PROOF -> {
                check(references.size == 1) {
                    "Merkle proof must have exactly one reference"
                }
                check(bits == 8 * (1 + 32 + 2)) {
                    "Merkle proof must have exactly 8 + 256 + 16 bits"
                }
                levelMask = references[0].levelMask shr 1
            }

            CellType.MERKLE_UPDATE -> {
                check(references.size == 2) {
                    "Merkle update must have exactly two references"
                }
                check(bits == 8 * (1 + (32 + 2) * 2)) {
                    "Merkle update must have exactly 8 + 256 + 16 + 256 + 16 bits"
                }
                levelMask = (references[0].levelMask or references[1].levelMask) shr 1
            }
        }

        var lastComputedHash = -1
        val hashes = Array(4) {
            HashBytes(UnsafeByteStringOperations.wrapUnsafe(ByteArray(32)))
        }

        for (i in 0..LevelMask.MAX_LEVEL) {
            if (i + 1 !in levelMask && i != LevelMask.MAX_LEVEL) {
                continue
            }

            computeHashes(
                hashes,
                i,
                lastComputedHash,
                cellType,
                levelMask
            )

            for (j in lastComputedHash + 1 until i) {
                hashes[j] = hashes[i]
            }
            lastComputedHash = i
        }

        val d1 = CellDescriptor.computeD1(levelMask, isExotic, refs)
        val d2 = CellDescriptor.computeD2(bits)
        val descriptor = CellDescriptor(d1, d2)

        return when (cellType) {
            CellType.PRUNED_BRANCH -> PrunedCell(descriptor, hashes, depths)
            CellType.LIBRARY_REFERENCE -> LibraryCell(descriptor, hashes[0])
            else -> DataCell(descriptor, bitsBuilder.toBitString(), references, hashes, depths)
        }
    }

    @OptIn(UnsafeByteStringApi::class)
    private fun computeHashes(
        hashes: Array<HashBytes>,
        level: Int,
        lastComputedHash: Int,
        cellType: CellType,
        levelMask: LevelMask,
    ) {
        if (level != LevelMask.MAX_LEVEL && cellType == CellType.PRUNED_BRANCH) {
            val hashesBefore = levelMask.apply(level).hashIndex
            val offset = 2 + hashesBefore * 32
            hashes[level] = HashBytes(
                UnsafeByteStringOperations.wrapUnsafe(
                    bitsBuilder.buffer.copyOfRange(offset, offset + 32)
                )
            )
            return
        }

        val digest = Sha256()
        val d1 = CellDescriptor.computeD1(levelMask.apply(level), cellType.isExotic, refs)
        val d2 = CellDescriptor.computeD2(bits)
        digest.update(byteArrayOf(d1, d2))

        if (lastComputedHash != -1 && cellType != CellType.PRUNED_BRANCH) {
            digest.update(hashes[lastComputedHash].value)
        } else {
            digest.update(bitsBuilder.buffer, 0, bits / 8)
            if (bits % 8 != 0) {
                var lastByte = bitsBuilder.buffer[bits / 8].toInt() and 0xFF
                lastByte = lastByte ushr 7 - bits % 8
                lastByte = lastByte or 1
                lastByte = lastByte shl 7 - bits % 8
                digest.update(byteArrayOf(lastByte.toByte()))
            }
        }

        val isMerkle = cellType.isMerkle
        val childLevel = if (isMerkle) min(LevelMask.MAX_LEVEL, level + 1) else level

        for (i in 0 until references.size) {
            val child = references[i]
            val childDepth = child.depth(childLevel)
            digest.update(
                byteArrayOf(
                    (childDepth ushr 8).toByte(),
                    (childDepth and 0xFF).toByte()
                )
            )
        }

        for (i in 0 until references.size) {
            val child = references[i]
            val childHash = child.hash(childLevel)
            digest.update(childHash.value, 0, 32)
        }

        hashes[level] = digest.digestToHashBytes()
    }

    private fun checkBits(bitCount: Int) {
        require(bitCount >= 0) { "bitCount must be >= 0, was: $bitCount" }
        require(bits + bitCount <= Cell.MAX_BIT_LENGHT) {
            "Cell overflow: requested $bitCount bits, available ${Cell.MAX_BIT_LENGHT - bits}"
        }
    }

    private fun checkRefs(refCount: Int) {
        require(refCount >= 0) { "refCount must be >= 0, was: $refCount" }
        require(references.size + refCount <= 4) {
            "Cell overflow: requested $refCount refs, available ${4 - references.size}"
        }
    }

    public companion object {
        @JvmStatic
        public fun createPrunedBranch(
            cell: Cell,
            newLevel: Int,
        ): Cell {
            return createPrunedBranch(cell, newLevel, CellContext.EMPTY)
        }

        @JvmStatic
        public fun createPrunedBranch(
            cell: Cell,
            newLevel: Int,
            cellContext: CellContext = CellContext.EMPTY
        ): Cell {
            if (cell is LoadedCell && cell.descriptor.referenceCount == 0) {
                return cell
            }
            var cellLevelMask = cell.levelMask
            val levelMask = cellLevelMask or LevelMask.level(newLevel)
            val builder = CellBuilder()
            builder.storeUInt(CellType.PRUNED_BRANCH.value, 8)
            builder.storeUInt(levelMask.mask, 8)

            // Only write levels lower than the new level.
            cellLevelMask = LevelMask(cellLevelMask.mask and (newLevel - 1))

            cellLevelMask.forEach { level ->
                val hash = cell.hash(level)
                builder.store(hash)
            }
            cellLevelMask.forEach { level ->
                val depth = cell.depth(level)
                builder.storeUInt(depth, 16)
            }

            return builder.build(isExotic = true)
        }
    }
}

public operator fun CellBuilder.plusAssign(builder: CellBuilder) {
    this.append(builder)
}

public operator fun CellBuilder.plus(builder: CellBuilder): CellBuilder {
    return CellBuilder().append(this).append(builder)
}
