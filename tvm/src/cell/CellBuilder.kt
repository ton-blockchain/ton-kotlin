package org.ton.cell

import kotlinx.io.bytestring.ByteString
import org.ton.bigint.*
import org.ton.bitstring.BitString
import org.ton.bitstring.ByteBackedMutableBitString
import org.ton.bitstring.MutableBitString
import org.ton.bitstring.toBitString
import org.ton.cell.exception.CellOverflowException
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.crypto.Sha256
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.jvm.JvmStatic
import kotlin.math.max

public interface CellBuilder {
    public var bits: MutableBitString
    public var refs: MutableList<Cell>
    public var levelMask: LevelMask?
    public var isExotic: Boolean

    public val bitsPosition: Int
    public val remainingBits: Int

    /**
     * Converts a builder into an ordinary cell.
     */
    public fun endCell(): Cell = build()
    public fun build(): Cell

    @Deprecated(
        message = "Use storeBoolean()",
        level = DeprecationLevel.WARNING,
        replaceWith = ReplaceWith("storeBoolean(bit)")
    )
    public fun storeBit(bit: Boolean): CellBuilder = storeBoolean(bit)

    public fun storeBoolean(value: Boolean): CellBuilder

    public fun storeBits(vararg bits: Boolean): CellBuilder
    public fun storeBits(bits: Iterable<Boolean>): CellBuilder
    public fun storeBits(bits: Collection<Boolean>): CellBuilder

    @Deprecated("use storeBitString(bits) instead.", ReplaceWith("storeBitString(bits)"))
    public fun storeBits(bits: BitString): CellBuilder = storeBitString(bits)

    public fun storeBitString(value: BitString, startIndex: Int = 0, endIndex: Int = value.size): CellBuilder

    public fun storeByteString(value: ByteString): CellBuilder
    public fun storeBytes(byteArray: ByteArray): CellBuilder
    public fun storeByte(byte: Byte): CellBuilder

    /**
     * Stores a reference to cell into builder.
     */
    public fun storeRef(ref: Cell): CellBuilder
    public fun storeNullableRef(ref: Cell?): CellBuilder

    public fun storeRefs(vararg refs: Cell): CellBuilder
    public fun storeRefs(refs: Iterable<Cell>): CellBuilder
    public fun storeRefs(refs: Collection<Cell>): CellBuilder

    /**
     * Stores an unsigned [length]-bit integer [value] into builder for 0 ≤ [length] ≤ 256.
     */
    public fun storeUInt(value: BigInt, length: Int): CellBuilder
    public fun storeUInt(value: Byte, length: Int): CellBuilder = storeUInt(value.toInt(), length)
    public fun storeUInt(value: Short, length: Int): CellBuilder = storeUInt(value.toInt(), length)
    public fun storeUInt(value: Int, length: Int): CellBuilder = storeUInt(value.toBigInt(), length)
    public fun storeUInt(value: Long, length: Int): CellBuilder = storeUInt(value.toBigInt(), length)

    public fun storeULong(value: ULong, bitCount: Int = ULong.SIZE_BITS): CellBuilder = storeLong(value.toLong(), 64)

    public fun storeUInt8(value: UByte): CellBuilder = storeInt(value.toByte(), 8)
    public fun storeUInt16(value: UShort): CellBuilder = storeInt(value.toShort(), 16)
    public fun storeUInt32(value: UInt): CellBuilder = storeInt(value.toInt(), 32)

    @Deprecated("Use storeULong(value) instead.", ReplaceWith("storeULong(value)"))
    public fun storeUInt64(value: ULong): CellBuilder = storeULong(value)

    public fun storeUIntLeq(value: BigInt, max: BigInt): CellBuilder = storeUInt(value, max.bitLength)
    public fun storeUIntLeq(value: Byte, max: Byte): CellBuilder = storeUIntLeq(value.toInt(), max.toInt())
    public fun storeUIntLeq(value: Short, max: Short): CellBuilder = storeUIntLeq(value.toInt(), max.toInt())
    public fun storeUIntLeq(value: Int, max: Int): CellBuilder = storeUIntLeq(value.toBigInt(), max.toBigInt())
    public fun storeUIntLeq(value: Long, max: Long): CellBuilder = storeUIntLeq(value.toBigInt(), max.toBigInt())

    public fun storeUIntLes(value: BigInt, max: BigInt): CellBuilder = storeUInt(value, (max - 1.toBigInt()).bitLength)
    public fun storeUIntLes(value: Byte, max: Byte): CellBuilder = storeUIntLes(value.toInt(), max.toInt())
    public fun storeUIntLes(value: Short, max: Short): CellBuilder = storeUIntLes(value.toInt(), max.toInt())
    public fun storeUIntLes(value: Int, max: Int): CellBuilder = storeUIntLes(value.toBigInt(), max.toBigInt())
    public fun storeUIntLes(value: Long, max: Long): CellBuilder = storeUIntLes(value.toBigInt(), max.toBigInt())

    /**
     * Stores a signed [length]-bit integer [value] into builder for 0 ≤ [length] ≤ 257.
     */
    public fun storeInt(value: BigInt, length: Int): CellBuilder
    public fun storeInt(value: Byte, length: Int): CellBuilder = storeInt(value.toInt(), length)
    public fun storeInt(value: Short, length: Int): CellBuilder = storeInt(value.toInt(), length)
    public fun storeInt(value: Int, length: Int): CellBuilder = storeInt(value.toBigInt(), length)

    @Deprecated("Use storeLong(length) instead", ReplaceWith("storeLong(value, length)"))
    public fun storeInt(value: Long, length: Int): CellBuilder = storeLong(value, length)

    public fun storeLong(value: Long, bitCount: Int = Long.SIZE_BITS): CellBuilder {
        if (bitCount <= 0) return this
        if (value == 0L) {
            storeBits(*BooleanArray(bitCount))
            return this
        }
        storeInt(value.toBigInt(), bitCount)
        return this
    }

    /**
     * Stores [slice] into builder.
     */
    public fun storeSlice(slice: CellSlice): CellBuilder

    public fun reset(): CellBuilder

    public companion object {
        @JvmStatic
        public fun of(cell: Cell): CellBuilder =
            CellBuilderImpl(cell.bits.toMutableBitString(), cell.refs.toMutableList())

        @JvmStatic
        public fun beginCell(): CellBuilder = CellBuilderImpl()

        @OptIn(ExperimentalContracts::class)
        @JvmStatic
        public fun createCell(builder: CellBuilder.() -> Unit): Cell {
            contract {
                callsInPlace(builder, InvocationKind.EXACTLY_ONCE)
            }
            val cellBuilder = CellBuilderImpl()
            builder(cellBuilder)
            return cellBuilder.build()
        }

        @JvmStatic
        public fun createPrunedBranch(cell: Cell, merkleDepth: Int): Cell = buildCell {
            val descriptor = cell.descriptor
            val levelMask = LevelMask.level(descriptor.levelMask.mask or (1 shl merkleDepth)).also {
                levelMask = it
            }
            isExotic = true
            storeByte(CellType.PRUNED_BRANCH.value.toByte())
            storeByte(levelMask.mask.toByte())

            val hashCount = descriptor.hashCount
            repeat(hashCount) { level ->
                storeBits(cell.hash(level))
            }
            repeat(hashCount) { level ->
                storeUInt16(cell.depth(level).toUShort())
            }
        }
    }

    public fun storeBytes(byteArray: ByteArray, length: Int): CellBuilder

}

public inline operator fun CellBuilder.invoke(builder: CellBuilder.() -> Unit) {
    builder(this)
}

@OptIn(ExperimentalContracts::class)
public inline fun buildCell(context: CellContext = CellContext.EMPTY, builderAction: CellBuilder.() -> Unit): Cell {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return context.finalizeCell(CellBuilder.beginCell().apply(builderAction))
}

@OptIn(ExperimentalContracts::class)
public inline fun CellBuilder.storeRef(
    context: CellContext = CellContext.EMPTY,
    block: CellBuilder.() -> Unit
): CellBuilder {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    val cell = context.finalizeCell(CellBuilder.beginCell().apply(block))
    storeRef(cell)
    return this
}

public inline fun CellBuilder(cell: Cell): CellBuilder = CellBuilder.of(cell)
public inline fun CellBuilder(): CellBuilder = CellBuilder.beginCell()

private class CellBuilderImpl(
    override var bits: MutableBitString = ByteBackedMutableBitString(0),
    override var refs: MutableList<Cell> = ArrayList(),
    override var levelMask: LevelMask? = null,
    override var isExotic: Boolean = false
) : CellBuilder {
    override val bitsPosition: Int get() = bits.size
    override val remainingBits: Int get() = Cell.MAX_BITS_SIZE - bitsPosition

    override fun storeBoolean(bit: Boolean): CellBuilder = apply {
        checkBitsOverflow(1)
        bits.plus(bit)
    }

    override fun storeBits(vararg bits: Boolean): CellBuilder = apply {
        checkBitsOverflow(bits.size)
        this.bits += bits
    }

    override fun storeBits(bits: Collection<Boolean>): CellBuilder = apply {
        checkBitsOverflow(bits.size)
        this.bits.plus(bits)
    }

    override fun storeBitString(value: BitString, startIndex: Int, endIndex: Int): CellBuilder {
        if (startIndex == endIndex) return this
        if (startIndex == 0 && endIndex == value.size) {
            checkBitsOverflow(value.size)
            this.bits.plus(value)
        } else {
            this.bits.plus(value.slice(startIndex, endIndex))
        }
        return this
    }

    override fun storeBits(bits: Iterable<Boolean>): CellBuilder = storeBits(bits.toList())

    override fun storeByteString(value: ByteString): CellBuilder {
        checkBitsOverflow(value.size * Byte.SIZE_BITS)
        this.bits.plus(value.toByteArray())
        return this
    }

    override fun storeBytes(byteArray: ByteArray): CellBuilder = apply {
        checkBitsOverflow(byteArray.size * Byte.SIZE_BITS)
        this.bits.plus(byteArray)
    }

    override fun storeByte(byte: Byte): CellBuilder = apply {
        checkBitsOverflow(Byte.SIZE_BITS)
        this.bits.plus(byteArrayOf(byte))
    }

    override fun storeBytes(byteArray: ByteArray, length: Int): CellBuilder = apply {
        checkBitsOverflow(length)
        this.bits.plus(byteArray, length)
    }

    override fun storeRef(ref: Cell): CellBuilder = apply {
        checkRefsOverflow(1)
        refs.add(ref)
    }

    override fun storeNullableRef(ref: Cell?): CellBuilder {
        if (ref != null) {
            checkRefsOverflow(1)
            storeBoolean(true)
            refs.add(ref)
        } else {
            storeBoolean(false)
        }
        return this
    }

    override fun storeRefs(vararg refs: Cell): CellBuilder = apply {
        checkRefsOverflow(refs.size)
        this.refs.addAll(refs)
    }

    override fun storeRefs(refs: Iterable<Cell>): CellBuilder = storeRefs(refs.toList())

    override fun storeRefs(refs: Collection<Cell>): CellBuilder = apply {
        checkRefsOverflow(refs.size)
        this.refs.addAll(refs)
    }

    override fun storeUInt(value: BigInt, length: Int): CellBuilder = apply {
        check(value.bitLength <= length) { "Integer `$value` does not fit into $length bits" }
        check(value.sign >= 0) { "Integer `$value` must be unsigned" }
        storeNumber(value, length)
    }

    override fun storeInt(value: BigInt, length: Int): CellBuilder = apply {
        val intBits = 1.toBigInt() shl (length - 1)
        require(value >= -intBits && value < intBits) { "Can't store an Int, because its value allocates more space than provided." }
        storeNumber(value, length)
    }

    private fun storeNumber(value: BigInt, length: Int): CellBuilder = apply {
        val bits = BooleanArray(length) { index ->
            ((value shr index) and 1.toBigInt()).toInt() == 1
        }.reversedArray()
        storeBits(*bits)
    }

    override fun storeSlice(slice: CellSlice): CellBuilder = apply {
        checkBitsOverflow(slice.remainingBits)
        checkRefsOverflow(slice.remainingRefs)

        storeBitString(slice.preloadBitString(slice.remainingBits))
        repeat(slice.remainingRefs) { index ->
            storeRef(slice.preloadRef(index))
        }
    }

    override fun reset(): CellBuilder {
        bits = ByteBackedMutableBitString(ByteArray(128), 0)
        refs.clear()
        levelMask = null
        isExotic = false
        return this
    }

    override fun build(): Cell {
        var childrenMask = LevelMask()
        refs.forEach { child ->
            childrenMask = childrenMask or child.levelMask
        }

        val levelMask = levelMask ?: childrenMask
        val d1 = CellDescriptor.computeD1(levelMask, isExotic, refs.size)
        val d2 = CellDescriptor.computeD2(bitsPosition)
        val descriptor = CellDescriptor(d1, d2)

        val hashes = computeHashes(descriptor, childrenMask)

        return when (descriptor.cellType) {
            CellType.PRUNED_BRANCH -> {
                check(hashes.size == 1)
                val (hash, depth) = hashes[0]
                PrunedBranchCell(
                    hash.toBitString(), depth, descriptor, bits
                )
            }

            else -> if (descriptor == EmptyCell.descriptor) {
                EmptyCell
            } else {
                DataCell(descriptor, bits, refs, hashes)
            }
        }
    }

    private fun computeHashes(descriptor: CellDescriptor, childrenMask: LevelMask): List<Pair<ByteArray, Int>> {
        var levels = descriptor.levelMask.level + 1
        val data = bits.toByteArray(augment = true)

        val computedLevelMask = when (descriptor.cellType) {
            CellType.ORDINARY -> childrenMask
            // 8 bits type, 8 bits level mask, level x (hash, depth)
            CellType.PRUNED_BRANCH -> {
                check(data[0].toInt() == CellType.PRUNED_BRANCH.value) {
                    "Cell type mismatch, expected: ${CellType.PRUNED_BRANCH} ${CellType.PRUNED_BRANCH.value}, actual: ${data[0]}"
                }
                val expectedBitLength = 8 + 8 + descriptor.levelMask.level * (HASH_BITS + DEPTH_BITS)
                check(bitsPosition == expectedBitLength) {
                    "Invalid bit length, expected: $expectedBitLength, actual: $bitsPosition"
                }
                check(refs.isEmpty()) {
                    "Pruned branch contains non empty references"
                }
                val storedMask = data[1].toInt()
                check(descriptor.levelMask.mask == storedMask) {
                    "Invalid level mask in pruned branch, expected: ${descriptor.levelMask.mask}, actual: $storedMask"
                }
                levels = 1
                descriptor.levelMask
            }
            // 8 bits type, hash, depth
            CellType.MERKLE_PROOF -> {
                check(data[0].toInt() == CellType.MERKLE_PROOF.value) {
                    "Cell type mismatch, expected: ${CellType.MERKLE_PROOF} ${CellType.MERKLE_PROOF.value}, actual: ${data[0]}"
                }
                val expectedBitLength = 8 + HASH_BITS + DEPTH_BITS
                check(bitsPosition == expectedBitLength) {
                    "Invalid bit length, expected: $expectedBitLength, actual: $bitsPosition"
                }
                check(refs.size == 1) {
                    "Invalid merkle proof reference count, expected: 1, actual: ${refs.size}"
                }
                childrenMask.virtualize(1)
            }
            // 8 bits type, 2 x (hash, depth)
            CellType.MERKLE_UPDATE -> {
                check(data[0].toInt() == CellType.MERKLE_UPDATE.value) {
                    "Cell type mismatch, expected: ${CellType.MERKLE_UPDATE} ${CellType.MERKLE_UPDATE.value}, actual: ${data[0]}"
                }
                val expectedBitLength = 8 + 2 * (HASH_BITS + DEPTH_BITS)
                check(bitsPosition == expectedBitLength) {
                    println(bits)
                    "Invalid bit length, expected: $expectedBitLength, actual: $bitsPosition"
                }
                check(refs.size == 2) {
                    "Invalid merkle update reference count, expected: 2, actual: ${refs.size}"
                }
                childrenMask.virtualize(1)
            }
            // 8 bits type, hash
            CellType.LIBRARY_REFERENCE -> {
                check(data[0].toInt() == CellType.LIBRARY_REFERENCE.value) {
                    "Cell type mismatch, expected: ${CellType.LIBRARY_REFERENCE} ${CellType.LIBRARY_REFERENCE.value}, actual: ${data[0]}"
                }
                val expectedBitLength = 8 + HASH_BITS
                check(bitsPosition == expectedBitLength) {
                    "Invalid bit length, expected: $expectedBitLength, actual: $bitsPosition"
                }
                check(refs.isEmpty()) {
                    "Invalid library reference count, expected: 0, actual: ${refs.size}"
                }
                LevelMask()
            }
        }

        check(descriptor.levelMask == computedLevelMask) {
            "Invalid level mask, expected: $levelMask, actual: $computedLevelMask"
        }

        val levelOffset = if (descriptor.cellType.isMerkle) 1 else 0
        val hashes = ArrayList<Pair<ByteArray, Int>>(levels)

        var (d1, d2) = descriptor
        val hasher = Sha256()
        repeat(levels) { level ->
            hasher.reset()
            val levelMask = if (descriptor.cellType == CellType.PRUNED_BRANCH) {
                descriptor.levelMask
            } else {
                LevelMask.level(level)
            }
            d1 = d1 and (CellDescriptor.LEVEL_MASK or CellDescriptor.HAS_HASHES_MASK).inv().toByte()
            d1 = d1 or (levelMask.mask shl 5).toByte()
            hasher.update(byteArrayOf(d1, d2))

            if (level == 0) {
                hasher.update(data)
            } else {
                val prevHash = hashes[level - 1].first
                hasher.update(prevHash)
            }

            var depth = 0
            refs.forEach { child ->
                val childDepth = child.depth(level + levelOffset)
                depth = max(depth, childDepth + 1)

                hasher.update(
                    byteArrayOf(
                        (childDepth ushr Byte.SIZE_BITS).toByte(),
                        childDepth.toByte()
                    )
                )
            }

            refs.forEach { child ->
                val childHash = child.hash(level + levelOffset).toByteArray()
                hasher.update(childHash)
            }

            val hash = hasher.digest()
            hashes.add(hash to depth)
        }

        return hashes
    }

    override fun toString(): String = endCell().toString()

    private fun checkBitsOverflow(length: Int) = require(length <= remainingBits) {
        throw CellOverflowException("Bits overflow. Can't add $length bits. $remainingBits bits left. - ${bits.size}")
    }

    private fun checkRefsOverflow(count: Int) = require(count <= (4 - refs.size)) {
        throw CellOverflowException("Refs overflow. Can't add $count refs. ${4 - refs.size} refs left.")
    }

    companion object {
        const val HASH_BITS = 256
        const val DEPTH_BITS = 16
    }
}

public fun CellBuilder.storeUInt(value: UInt, bits: Int = UInt.SIZE_BITS): CellBuilder {
    if (bits <= 0) return this
    if (value == 0u) {
        storeBits(*BooleanArray(bits))
        return this
    }
    storeUInt(value.toLong().toBigInt(), bits)
    return this
}
