@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.sdk.toncenter.model

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.sdk.tl.serializers.ByteStringBase64Serializer
import kotlin.jvm.JvmName

@Serializable
public class TonCenterSendMessageRequest(
    @get:JvmName("boc") public val boc: ByteString
)

@Serializable
public class TonCenterSendMessageResult(
    @get:JvmName("messageHash") public val messageHash: ByteString,
    @get:JvmName("messageHashNorm") public val messageHashNorm: ByteString
)
