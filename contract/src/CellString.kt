package org.ton.contract

import org.ton.bitstring.BitString
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.storeRef
import org.ton.tl.ByteString
import org.ton.tl.asByteString
import org.ton.tlb.TlbConstructor
import kotlin.math.min

public object CellStringTlbConstructor : TlbConstructor<ByteString>(
    schema = "", id = null
) {
    private const val MAX_BYTES = 1024
    private const val MAX_CHAIN_LENGTH = 16

    override fun loadTlb(cellSlice: CellSlice): ByteString {
        var result = BitString.empty()
        forEach(cellSlice, Cell.MAX_BITS_SIZE) {
            result += it
        }
        return result.toByteArray().asByteString()
    }

    override fun storeTlb(cellBuilder: CellBuilder, value: ByteString) {
        require(value.size <= MAX_BYTES) {
            "String is too long"
        }
        val head = min(
            value.size * 8,
            min(Cell.MAX_BITS_SIZE - cellBuilder.bitsPosition, Cell.MAX_BITS_SIZE)
        ) / 8 * 8
        val maxBits = Cell.MAX_BITS_SIZE / 8 * 8
        val depth = 1 + (value.size * 8 - head + maxBits - 1) / maxBits
        require(depth <= MAX_CHAIN_LENGTH) {
            "String is too long"
        }
        if (head / 8 == value.size) {
            cellBuilder.storeBytes(value.toByteArray())
        } else {
            cellBuilder.storeBytes(value.copyOf(head / 8).toByteArray())
            cellBuilder.storeRef {
                storeTlb(this, value.copyOfRange(head / 8, value.size))
            }
        }
    }

    private fun forEach(cellSlice: CellSlice, topBits: Int, f: (BitString) -> Unit) {
        val head = min(cellSlice.remainingBits, topBits)
        f(cellSlice.loadBits(head))
        var ref = try {
            cellSlice.loadRef()
        } catch (e: Exception) {
            return
        }
        while (true) {
            val cs = ref.beginParse()
            f(cs.loadBits(cs.remainingBits))
            ref = try {
                cs.loadRef()
            } catch (e: Exception) {
                return
            }
        }
    }
}
