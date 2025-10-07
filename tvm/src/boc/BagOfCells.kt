package org.ton.boc

import io.ktor.utils.io.core.*
import kotlinx.io.readByteArray
import org.ton.cell.Cell
import kotlin.io.encoding.Base64
import kotlin.jvm.JvmStatic

public fun BagOfCells(byteArray: ByteArray): BagOfCells = BagOfCells.of(byteArray)
public fun BagOfCells(roots: Collection<Cell>): BagOfCells = BagOfCells.of(roots)
public fun BagOfCells(vararg roots: Cell): BagOfCells = BagOfCells.of(*roots)

public interface BagOfCells : Iterable<Cell> {
    public val roots: List<Cell>

    public fun toByteArray(): ByteArray = buildPacket {
        writeBagOfCells(this@BagOfCells)
    }.readByteArray()

    override fun toString(): String

    public fun write(output: Output) {
        output.writeBagOfCells(this)
    }

    public companion object {
        public const val BOC_GENERIC_MAGIC: Int = 0xB5EE9C72.toInt()
        public const val BOC_INDEXED_MAGIC: Int = 0x68FF65F3
        public const val BOC_INDEXED_CRC32C_MAGIC: Int = 0xACC3A728.toInt()

        @JvmStatic
        public fun of(roots: Iterable<Cell>): BagOfCells {
            val rootsList = roots.toList()
            return CachedBagOfCells(rootsList)
        }

        @JvmStatic
        public fun of(vararg roots: Cell): BagOfCells {
            val rootsList = roots.toList()
            return CachedBagOfCells(rootsList)
        }

        @JvmStatic
        public fun of(byteArray: ByteArray): BagOfCells {
            if (byteArray.isEmpty()) {
                return BagOfCells(Cell())
            }
            try {
                return read(ByteReadPacket(byteArray))
            } catch (e: Exception) {
                throw IllegalArgumentException("Can't load BoC: (${byteArray.size}) ${byteArray.toHexString()}", e)
            }
        }

        @JvmStatic
        public fun read(input: Input): BagOfCells = input.readBagOfCell()
    }
}

public fun String.base64ToCell(): Cell = BagOfCells(Base64.decode(this)).first()
