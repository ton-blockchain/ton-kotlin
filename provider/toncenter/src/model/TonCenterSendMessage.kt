@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.kotlin.provider.toncenter.model

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
public data class TonCenterSendMessageRequest(
    val boc: ByteString
)

@Serializable
public data class TonCenterSendMessageResult(
    val messageHash: ByteString,
    val messageHashNorm: ByteString
)
