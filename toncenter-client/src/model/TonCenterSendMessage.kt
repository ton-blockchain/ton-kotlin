@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.sdk.toncenter.model

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.sdk.tl.serializers.ByteStringBase64Serializer

@Serializable
public class TonCenterSendMessageRequest(
    public val boc: ByteString
)

@Serializable
public class TonCenterSendMessageResult(
    public val messageHash: ByteString,
    public val messageHashNorm: ByteString
)
