@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.kotlin.overlay.sertificate

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.kotlin.adnl.AdnlIdFull
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
sealed interface OverlayCertificate {
    @Serializable
    data object Empty : OverlayCertificate

    @Serializable
    data class V1(
        val issuedBy: AdnlIdFull,
        val expireAt: Int,
        val maxSize: Int,
        val signature: ByteString
    ) : OverlayCertificate

    @Serializable
    data class V2(
        val issuedBy: AdnlIdFull,
        val expireAt: Int,
        val maxSize: Int,
        val flags: Int,
        val signature: ByteString
    ) : OverlayCertificate
}
