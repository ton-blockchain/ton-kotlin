@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.kotlin.provider.liteapi.model

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.kotlin.tl.Bits256
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
public data class LiteApiZeroStateIdExt(
    val workchain: Int,
    @Bits256
    val rootHash: ByteString,
    @Bits256
    val fileHash: ByteString
)
