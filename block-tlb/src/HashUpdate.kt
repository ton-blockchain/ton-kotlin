@file:UseSerializers(HexByteArraySerializer::class)

package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.bitstring.BitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.crypto.HexByteArraySerializer
import org.ton.tlb.*

@Serializable
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
        cellBuilder: CellBuilder,
        value: HashUpdate
    ) = cellBuilder {
        storeBits(value.oldHash)
        storeBits(value.newHash)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): HashUpdate = cellSlice {
        val oldHash = loadBits(256)
        val newHash = loadBits(256)
        HashUpdate(oldHash, newHash)
    }
}
