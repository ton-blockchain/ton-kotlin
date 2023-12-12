package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.bitstring.BitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.*
import kotlin.jvm.JvmStatic

@SerialName("!merkle_update")
@Serializable
public data class MerkleUpdate<X>(
    @SerialName("old_hash") val oldHash: BitString,
    @SerialName("new_hash") val newHash: BitString,
    @SerialName("old_depth") val oldDepth: UShort,
    @SerialName("new_depth") val newDepth: UShort,
    val old: CellRef<X>,
    val new: CellRef<X>
) : TlbObject {
    init {
        require(oldHash.size == 256)
        require(newHash.size == 256)
    }

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return printer.type("!merkle_update") {
            field("old_hash", oldHash)
            field("new_hash", newHash)
            field("old_depth", oldDepth)
            field("new_depth", newDepth)
            field("old", old.toCell())
            field("new", new.toCell())
        }
    }

    public companion object {
        @JvmStatic
        public fun <X> tlbCodec(
            x: TlbCodec<X>
        ): TlbCodec<MerkleUpdate<X>> = MerkleUpdateTlbConstructor(x).asTlbCombinator()
    }
}

private class MerkleUpdateTlbConstructor<X>(
    x: TlbCodec<X>
) : TlbConstructor<MerkleUpdate<X>>(
    schema = "!merkle_update#04 {X:Type} old_hash:bits256 new_hash:bits256 old_depth:uint16 new_depth:uint16 old:^X new:^X = MERKLE_UPDATE X"
) {
    val xCellRef = CellRef.tlbCodec(x)

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: MerkleUpdate<X>
    ) = cellBuilder {
        isExotic = true
        storeBits(value.oldHash)
        storeBits(value.newHash)
        storeUInt16(value.oldDepth)
        storeUInt16(value.newDepth)
        storeTlb(xCellRef, value.old)
        storeTlb(xCellRef, value.new)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): MerkleUpdate<X> = cellSlice {
        val oldHash = loadBits(256)
        val newHash = loadBits(256)
        val oldDepth = loadUInt16()
        val newDepth = loadUInt16()
        val old = loadTlb(xCellRef)
        val new = loadTlb(xCellRef)
        MerkleUpdate(oldHash, newHash, oldDepth, newDepth, old, new)
    }
}
