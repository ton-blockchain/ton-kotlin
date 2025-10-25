@file:UseSerializers(
    HashBytesAsBase64Serializer::class,
    ByteStringBase64Serializer::class
)

package org.ton.sdk.toncenter.model

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.json.JsonObject
import org.ton.sdk.crypto.HashBytes
import org.ton.sdk.tl.serializers.ByteStringBase64Serializer
import org.ton.sdk.toncenter.internal.serializers.HashBytesAsBase64Serializer

@Serializable
public class TonCenterMessageContent(
    public val hash: HashBytes,
    public val body: ByteString,
    public val decoded: JsonObject?
)
