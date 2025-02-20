@file:Suppress("OPT_IN_USAGE")

package org.ton.cell

import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.bitstring.BitString
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmStatic

@JsonClassDiscriminator("@type")
public interface Cell {
    public val bits: BitString
    public val refs: List<Cell>
    public val descriptor: CellDescriptor
    public val type: CellType get() = descriptor.cellType
    public val levelMask: LevelMask get() = descriptor.levelMask

    public fun isEmpty(): Boolean = bits.isEmpty() && refs.isEmpty()

    public fun hash(level: Int = MAX_LEVEL): BitString
    public fun depth(level: Int = MAX_LEVEL): Int

    public fun treeWalk(): Sequence<Cell> = sequence {
        yieldAll(refs)
        refs.forEach { reference ->
            yieldAll(reference.treeWalk())
        }
    }

    public fun beginParse(): CellSlice = CellSlice.beginParse(this)

    /**
     * Creates a virtualized cell
     */
    public fun virtualize(offset: Int = 1): Cell

    override fun toString(): String

    public companion object {
        public const val HASH_BYTES: Int = 32
        public const val HASH_BITS: Int = HASH_BYTES * Byte.SIZE_BITS
        public const val DEPTH_BYTES: Int = 2
        public const val DEPTH_BITS: Int = DEPTH_BYTES * Byte.SIZE_BITS
        public const val MAX_LEVEL: Int = 3
        public const val MAX_DEPTH: Int = 1024
        public const val MAX_BITS_SIZE: Int = 1023

        @JvmStatic
        public fun empty(): Cell = EmptyCell

        @JvmStatic
        public fun of(hex: String, vararg refs: Cell): Cell = buildCell {
            storeBitString(BitString(hex))
            storeRefs(*refs)
        }

        @JvmStatic
        public fun of(bits: BitString, vararg refs: Cell): Cell = buildCell {
            storeBitString(bits)
            storeRefs(*refs)
        }

        @JvmStatic
        public fun toString(cell: Cell): String = buildString {
            toString(cell, this)
        }

        @JvmStatic
        public fun toString(
            cell: Cell,
            appendable: Appendable,
            indent: String = ""
        ) {
            appendable.append(indent)
            if (cell.type.isExotic) {
                appendable.append(cell.type.toString())
                appendable.append(' ')
            }
            appendable.append(cell.bits.toString())
//            appendable.append(", hash:")
//            appendable.append(cell.hash().toHexString())
            cell.refs.forEach { reference ->
                appendable.append('\n')
                toString(reference, appendable, "$indent  ")
            }
        }

        @JvmStatic
        public fun getRefsDescriptor(refs: Int, isExotic: Boolean, levelMask: LevelMask): Byte {
            return (refs + ((if (isExotic) 1 else 0) * 8) + (levelMask.mask * 32)).toByte()
        }

        @JvmStatic
        public fun getBitsDescriptor(bits: BitString): Byte {
            val result = (bits.size / 8) * 2
            if ((bits.size and 7) != 0) {
                return (result + 1).toByte()
            }
            return result.toByte()
        }
    }
}

public fun <T> Cell.parse(block: CellSlice.() -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return beginParse().parse(block)
}

public fun Cell(): Cell = Cell.empty()

public fun Cell(hex: String, vararg refs: Cell): Cell = Cell.of(hex, *refs)

public fun Cell(bits: BitString, vararg refs: Cell): Cell = Cell.of(bits, *refs)
