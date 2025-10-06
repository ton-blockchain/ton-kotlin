package org.ton.api.control

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.api.pk.PrivateKey
import org.ton.kotlin.tl.ByteStringBase64Serializer

@Serializable
@Polymorphic
@SerialName("control.config.local")
@JsonClassDiscriminator("@type")
public data class ControlConfigLocal(
    val priv: PrivateKey,
    @Serializable(ByteStringBase64Serializer::class)
    val pub: ByteString,
    val port: Int
)
