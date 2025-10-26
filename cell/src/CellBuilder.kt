package org.ton.sdk.cell

import kotlinx.io.bytestring.ByteString
import org.ton.sdk.bigint.BigInt
import org.ton.sdk.bitstring.BitString
import org.ton.sdk.bitstring.BitStringBuilder
import org.ton.sdk.bitstring.unsafe.UnsafeBitStringApi
import org.ton.sdk.bitstring.unsafe.UnsafeBitStringOperations
import org.ton.sdk.cell.internal.DataCell
import org.ton.sdk.cell.internal.EmptyCell
import org.ton.sdk.cell.internal.LibraryCell
import org.ton.sdk.crypto.HashBytes
import org.ton.sdk.crypto.Sha256
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.max

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

    public fun storeUBigInt(value: BigInt, bitCount: Int): CellBuilder = apply {
        checkBits(bitCount)
        bitsBuilder.writeUBigInt(value, bitCount)
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

    @OptIn(UnsafeBitStringApi::class)
    public fun build(isExotic: Boolean): Cell {
        var childMask = LevelMask.EMPTY
        for (child in references) {
            childMask = childMask.or(child.levelMask)
        }

        val bitString = bitsBuilder.toBitString()
        var levelMask = childMask
        var cellType = CellType.ORDINARY
        if (isExotic && bits >= 8) {
            UnsafeBitStringOperations.withByteArrayUnsafe(bitString) { data ->
                val byte = data[0].toInt()
                cellType = when (byte) {
                    1 -> CellType.PRUNED_BRANCH
                    2 -> CellType.LIBRARY_REFERENCE
                    3 -> CellType.MERKLE_PROOF
                    4 -> CellType.MERKLE_UPDATE
                    else -> throw IllegalArgumentException("Invalid cell type byte: $byte, isExotic=true")
                }
                when (cellType) {
                    CellType.PRUNED_BRANCH -> {
                        levelMask = LevelMask(data[1].toInt())
                    }

                    CellType.MERKLE_PROOF,
                    CellType.MERKLE_UPDATE -> {
                        levelMask = childMask.virtualize(1)
                    }

                    CellType.LIBRARY_REFERENCE -> {
                        levelMask = LevelMask.EMPTY
                    }

                    else -> {}
                }
            }
        }

        val hashes = Array(levelMask.hashCount) { EmptyCell.EMPTY_CELL_HASH }
        val depths = IntArray(levelMask.hashCount)
        val descriptor = computeHashes(bitString.toByteArray(), cellType, levelMask, hashes, depths)
        return when (descriptor.cellType) {
            CellType.ORDINARY -> if (EmptyCell.descriptor == descriptor) {
                EmptyCell
            } else {
                DataCell(
                    descriptor,
                    bitString,
                    ArrayList(references),
                    hashes,
                    depths
                )
            }

            CellType.LIBRARY_REFERENCE -> LibraryCell(descriptor, hashes[0])
            CellType.PRUNED_BRANCH -> TODO()
            CellType.MERKLE_PROOF,
            CellType.MERKLE_UPDATE -> DataCell(
                descriptor,
                bitString,
                ArrayList(references),
                hashes,
                depths
            )
        }
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

    private fun computeHashes(
        data: ByteArray,
        cellType: CellType,
        levelMask: LevelMask,
        hashes: Array<HashBytes>,
        depths: IntArray
    ): CellDescriptor {
        var d1 = CellDescriptor.computeD1(levelMask, cellType.isExotic, refs)
        val d2 = CellDescriptor.computeD2(bits)
        val descriptor = CellDescriptor(d1, d2)

        // fast path, don't calc hashes for empty cell
        if (descriptor == EmptyCell.descriptor) {
            return EmptyCell.descriptor
        }

        val levelOffset = if (cellType.isMerkle) 1 else 0
        val hasher = Sha256()
        repeat(levelMask.hashCount) { level ->
            val levelMask = if (cellType == CellType.PRUNED_BRANCH) {
                levelMask
            } else {
                LevelMask.level(level)
            }
            d1 = d1 and (CellDescriptor.LEVEL_MASK or CellDescriptor.HAS_HASHES_MASK).inv().toByte()
            d1 = d1 or (levelMask.mask shl 5).toByte()
            hasher.update(byteArrayOf(d1, d2))

            if (level == 0) {
                hasher.update(data)
            } else {
                val prevHash = hashes[level - 1]
                hasher.update(prevHash.value)
            }

            var depth = 0
            references.forEach { child ->
                val childDepth = child.depth(level + levelOffset)
                depth = max(depth, childDepth + 1)

                hasher.update(
                    byteArrayOf(
                        (childDepth ushr Byte.SIZE_BITS).toByte(),
                        childDepth.toByte()
                    )
                )
            }

            references.forEach { child ->
                val childHash = child.hash(level + levelOffset)
                hasher.update(childHash.value, 0, 32)
            }

            hashes[level] = hasher.digestToHashBytes()
            depths[level] = depth
            hasher.reset()
        }
        return descriptor
    }
}

public operator fun CellBuilder.plusAssign(builder: CellBuilder) {
    this.append(builder)
}

public operator fun CellBuilder.plus(builder: CellBuilder): CellBuilder {
    return CellBuilder().append(this).append(builder)
}
