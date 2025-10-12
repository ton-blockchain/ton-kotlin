@file:Suppress("PackageDirectoryMismatch")

package org.ton.cell

import kotlinx.io.bytestring.ByteString
import org.ton.bitstring.BitString
import org.ton.kotlin.cell.LoadedCell

public class DataCell(
    override val descriptor: CellDescriptor,
    public val hashes: List<ByteString>,
    public val depths: List<Short>,
    bits: BitString,
    public override val references: List<Cell>
) : LoadedCell {
    @Deprecated("Use new constructor")
    public constructor(
        descriptor: CellDescriptor,
        bits: BitString,
        references: List<Cell>,
        hashes: List<Pair<ByteArray, Int>>
    ) : this(
        descriptor,
        hashes.map { (b, _) -> ByteString(b) },
        hashes.map { (_, d) -> d.toShort() },
        bits,
        references
    )

    @Suppress("OVERRIDE_DEPRECATION")
    override val refs: List<Cell> get() = references

    @Suppress("OVERRIDE_DEPRECATION")
    override val bits: BitString = bits

    private val hashCode: Int by lazy(LazyThreadSafetyMode.PUBLICATION) {
        var result = descriptor.hashCode()
        result = 31 * result + hashes.hashCode()
        result
    }

    override fun hash(level: Int): BitString {
        val hashIndex = levelMask.apply(level).hashIndex
        return BitString(hashes[hashIndex].toByteArray())
    }

    override fun depth(level: Int): Int {
        val hashIndex = levelMask.apply(level).hashIndex
        return depths[hashIndex].toInt()
    }

    override fun virtualize(offset: Int): Cell {
        return if (levelMask.isEmpty()) {
            this
        } else {
            VirtualCell(this, offset)
        }
    }

    override fun toString(): String = Cell.toString(this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataCell) return false

        if (descriptor != other.descriptor) return false
        if (bits != other.bits) return false
        return references == other.references
    }

    override fun hashCode(): Int = hashCode
}
