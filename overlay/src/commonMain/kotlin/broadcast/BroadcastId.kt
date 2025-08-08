@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.kotlin.overlay.broadcast

import io.github.andreypfau.kotlinx.crypto.sha256
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.encodeToByteArray
import org.ton.kotlin.adnl.AdnlIdFull
import org.ton.kotlin.tl.TL
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
data class BroadcastIdShort(
    val hash: ByteString
) {
    constructor(idFull: BroadcastIdFull) : this(
        hash = ByteString(*sha256(TL.Boxed.encodeToByteArray(idFull)))
    )

    override fun toString(): String = "BroadcastId[${hash.toHexString()}]"
}

@Serializable
data class BroadcastIdFull(
    val source: AdnlIdFull,
    val dataHash: ByteString,
    val flags: Int
) {
    val idShort: BroadcastIdShort by lazy {
        BroadcastIdShort(this)
    }
}
