@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.kotlin.blockchain

import io.github.andreypfau.kotlinx.crypto.sha256
import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.encodeToByteArray
import org.ton.kotlin.overlay.OverlayIdFull
import org.ton.kotlin.tl.Bits256
import org.ton.kotlin.tl.TL
import org.ton.kotlin.tl.TlConstructorId
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
@SerialName("tonNode.shardPublicOverlayId")
@TlConstructorId(0x4d9ed329)
class ShardPublicOverlayId(
    val workchain: Int,
    val shard: Long,
    @Bits256
    val zeroStateFileHash: ByteString
) {
    val overlayId by lazy {
        OverlayIdFull(ByteString(sha256(TL.Boxed.encodeToByteArray(this))))
    }

    companion object {
        fun masterchain(zeroStateFileHash: ByteArray) = ShardPublicOverlayId(
            workchain = -1,
            shard = 1L shl 63,
            zeroStateFileHash = ByteString(*zeroStateFileHash)
        )
    }
}
