@file:Suppress("OPT_IN_USAGE")

package org.ton.cell

import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.bitstring.BitString
import kotlin.math.ceil
import kotlin.math.floor

fun Cell(hex: String, vararg refs: Cell, isExotic: Boolean = false): Cell =
    Cell.of(BitString(hex), refs = refs.toList(), isExotic)

fun Cell(hex: String, refs: Iterable<Cell> = emptyList(), isExotic: Boolean = false): Cell =
    Cell.of(BitString(hex), refs, isExotic)

fun Cell(bits: BitString = BitString.empty(), refs: Iterable<Cell> = emptyList(), isExotic: Boolean = false) =
    Cell.of(bits, refs, isExotic)

fun Cell(bits: BitString, vararg refs: Cell, isExotic: Boolean = false): Cell =
    Cell.of(bits, refs = refs, isExotic)

@JsonClassDiscriminator("@type")
interface Cell {
    val bits: BitString
    val refs: List<Cell>
    val type: CellType

    val isExotic: Boolean get() = type.isExotic
    val isMerkle: Boolean get() = type.isMerkle
    val isPruned: Boolean get() = type.isPruned
    val levelMask: Int

    fun isEmpty(): Boolean = bits.isEmpty() && refs.isEmpty()

    fun hash(level: Int = 0): ByteArray
    fun depth(level: Int = 0): Int

    fun treeWalk(): Sequence<Cell> = sequence {
        yieldAll(refs)
        refs.forEach { reference ->
            yieldAll(reference.treeWalk())
        }
    }

    fun loadCell(): Cell =
        when (type) {
            CellType.ORDINARY -> this
            CellType.PRUNED_BRANCH -> error("Can't load pruned branch cell")
            CellType.LIBRARY_REFERENCE -> TODO()
            CellType.MERKLE_PROOF -> refs[0]
            CellType.MERKLE_UPDATE -> refs[1]
        }

    fun beginParse(): CellSlice = CellSlice.beginParse(this)

    fun <T> parse(block: CellSlice.() -> T): T {
        val slice = beginParse()
        val result = block(slice)
        slice.endParse()
        return result
    }

    fun getBitsDescriptor(): Byte = Companion.getBitsDescriptor(bits)
    fun getRefsDescriptor(): Byte = Companion.getRefsDescriptor(refs.size, isExotic, levelMask)

    override fun toString(): String

    companion object {
        @JvmStatic
        fun of(hex: String, vararg refs: Cell, isExotic: Boolean = false): Cell =
            CellImpl.of(BitString(hex), refs.toList(), isExotic)

        @JvmStatic
        fun of(
            bits: BitString = BitString(),
            refs: Iterable<Cell> = emptyList(),
            isExotic: Boolean = false
        ): Cell = CellImpl.of(bits, refs.toList(), isExotic)

        @JvmStatic
        fun of(
            bits: BitString,
            vararg refs: Cell,
            isExotic: Boolean = false
        ): Cell = CellImpl.of(bits, refs.toList(), isExotic)

        @JvmStatic
        fun toString(cell: Cell) = buildString {
            toString(cell, this)
        }

        @JvmStatic
        fun toString(
            cell: Cell,
            appendable: Appendable,
            indent: String = ""
        ) {
            appendable.append(indent)
            if (cell.isExotic) {
                appendable.append(cell.type.toString())
                appendable.append(' ')
            }
            appendable.append("x{")
            appendable.append(cell.bits.toString())
            appendable.append("}")
            cell.refs.forEachIndexed { index, reference ->
                appendable.append('\n')
                toString(reference, appendable, "$indent    ")
            }
        }

        @JvmStatic
        fun getRefsDescriptor(refs: Int, isExotic: Boolean, levelMask: Int): Byte {
            return (refs + ((if (isExotic) 1 else 0) * 8) + (levelMask * 32)).toByte()
        }

        @JvmStatic
        fun getBitsDescriptor(bits: BitString): Byte =
            (ceil(bits.size / 8.0).toInt() + floor(bits.size / 8.0).toInt()).toByte()
    }
}

