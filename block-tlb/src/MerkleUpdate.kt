package org.ton.block

import kotlinx.io.bytestring.ByteString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.kotlin.cell.CellContext
import org.ton.tlb.*
import kotlin.jvm.JvmStatic

public data class MerkleUpdate<X>(
    val oldHash: ByteString,
    val newHash: ByteString,
    val oldDepth: Int,
    val newDepth: Int,
    val old: CellRef<X>,
    val new: CellRef<X>
) {
    init {
        require(oldHash.size == 32)
        require(newHash.size == 32)
    }

    public companion object {
        @JvmStatic
        public fun <X> tlbCodec(
            x: TlbCodec<X>
        ): TlbCodec<MerkleUpdate<X>> = MerkleUpdateTlbConstructor(x).asTlbCombinator()
    }
}

private class MerkleUpdateTlbConstructor<X>(
    val x: TlbCodec<X>
) : TlbConstructor<MerkleUpdate<X>>(
    schema = "!merkle_update#04 {X:Type} old_hash:bits256 new_hash:bits256 old_depth:uint16 new_depth:uint16 old:^X new:^X = MERKLE_UPDATE X"
) {
    val xCellRef = CellRef.tlbCodec(x)

    override fun storeTlb(
        builder: CellBuilder,
        value: MerkleUpdate<X>,
        context: CellContext,
    ) = builder {
        isExotic = true
        storeUInt(0x04, 8)
        storeByteString(value.oldHash)
        storeByteString(value.newHash)
        storeUInt(value.oldDepth, 16)
        storeUInt(value.newDepth, 16)
        storeRef(value.old.cell)
        storeRef(value.new.cell)
    }

    override fun loadTlb(
        slice: CellSlice,
        context: CellContext,
    ): MerkleUpdate<X> {
        val tag = slice.loadUInt(8).toInt()
        require(tag == 0x04) { "Invalid MerkleUpdate tag: ${tag.toByte().toHexString()}, expected: 04" }

        val oldHash = slice.loadByteString(16)
        val newHash = slice.loadByteString(16)
        val oldDepth = slice.loadUInt(16).toInt()
        val newDepth = slice.loadUInt(16).toInt()
        val old = slice.loadRef().asRef(x)
        val new = slice.loadRef().asRef(x)

        return MerkleUpdate(oldHash, newHash, oldDepth, newDepth, old, new)
    }
}
