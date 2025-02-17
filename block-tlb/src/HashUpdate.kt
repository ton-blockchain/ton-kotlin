@file:UseSerializers(HexByteArraySerializer::class)

package org.ton.block

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.serialization.UseSerializers
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.crypto.HexByteArraySerializer
import org.ton.kotlin.cell.CellContext
import org.ton.tlb.TlbCodec

/**
 * Account state hash update.
 */
public data class HashUpdate(
    /**
     * Old account state hash.
     */
    val oldHash: ByteString,

    /**
     * New account state hash.
     */
    val newHash: ByteString
) {
    init {
        require(oldHash.size == 32) { "oldHash must be 32 bytes" }
        require(newHash.size == 32) { "newHast must be 32 bytes" }
    }

    override fun toString(): String = "HashUpdate(oldHash=${oldHash.toHexString()}, newHash=${newHash.toHexString()})"

    public companion object : TlbCodec<HashUpdate> by HashUpdateTlbCodec
}

private object HashUpdateTlbCodec : TlbCodec<HashUpdate> {
    private const val TAG = 0x72

    override fun storeTlb(
        builder: CellBuilder,
        value: HashUpdate,
        context: CellContext
    ) {
        builder.storeUInt(TAG, 8)
        builder.storeByteString(value.oldHash)
        builder.storeByteString(value.newHash)
    }

    override fun loadTlb(
        slice: CellSlice,
        context: CellContext
    ): HashUpdate {
        val tag = slice.loadUInt(8).toInt()
        require(tag == TAG) { "Invalid HashUpdate tag: ${tag.toHexString()}, expected: ${TAG.toHexString()}" }

        val oldHash = slice.loadByteString(32)
        val newHash = slice.loadByteString(32)

        return HashUpdate(oldHash, newHash)
    }
}
