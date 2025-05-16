@file:UseSerializers(HexByteArraySerializer::class)

package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.UseSerializers
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.crypto.HexByteArraySerializer
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor


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
        storeBitString(value.oldHash)
        storeBitString(value.newHash)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): HashUpdate = cellSlice {
        val oldHash = loadBitString(256)
        val newHash = loadBitString(256)
        HashUpdate(oldHash, newHash)
    }
}
