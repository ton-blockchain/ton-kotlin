package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.bitstring.BitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor


@SerialName("update_hashes")
public data class HashUpdate(
    @SerialName("old_hash") val oldHash: BitString, // old_hash : bits256
    @SerialName("new_hash") val newHash: BitString // new_hash : bits256
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("update_hashes") {
            field("old_hash", oldHash)
            field("new_hash", newHash)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbCodec<HashUpdate> by HashUpdateTlbConstructor.asTlbCombinator()
}

private object HashUpdateTlbConstructor : TlbConstructor<HashUpdate>(
    schema = "update_hashes#72 {X:Type} old_hash:bits256 new_hash:bits256 = HASH_UPDATE X;"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: HashUpdate
    ) = builder {
        storeBitString(value.oldHash)
        storeBitString(value.newHash)
    }

    override fun loadTlb(
        slice: CellSlice
    ): HashUpdate = slice {
        val oldHash = loadBitString(256)
        val newHash = loadBitString(256)
        HashUpdate(oldHash, newHash)
    }
}
