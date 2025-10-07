package org.ton.api.dht

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.SignedTlObject
import org.ton.api.pk.PrivateKey
import org.ton.api.pub.PublicKey
import org.ton.kotlin.crypto.SignatureVerifier
import org.ton.kotlin.crypto.Signer
import org.ton.kotlin.tl.*
import kotlin.jvm.JvmStatic

@Serializable
public data class DhtKeyDescription(
    val key: DhtKey,
    val id: PublicKey,
    @SerialName("update_rule")
    val updateRule: DhtUpdateRule = DhtUpdateRule.SIGNATURE,
    @Serializable(ByteStringBase64Serializer::class)
    override val signature: ByteString = ByteString()
) : SignedTlObject<DhtKeyDescription> {
    override fun signed(privateKey: Signer): DhtKeyDescription =
        copy(
            signature = ByteString(
                *privateKey.signToByteArray(
                    copy(signature = ByteString()).toByteArray()
                )
            )
        )

    override fun verify(publicKey: SignatureVerifier): Boolean =
        publicKey.verifySignature(tlCodec().encodeToByteArray(copy(signature = ByteString())), signature.toByteArray())

    override fun tlCodec(): TlCodec<DhtKeyDescription> = DhtKeyDescriptionTlConstructor

    public companion object : TlCodec<DhtKeyDescription> by DhtKeyDescriptionTlConstructor {
        @JvmStatic
        public fun signed(name: String, key: Signer): DhtKeyDescription {
            val keyDescription = DhtKeyDescription(
                id = key.publicKey(),
                key = DhtKey(key.publicKey().toAdnlIdShort().id, name)
            )
            return keyDescription.signed(key)
        }
    }
}

private object DhtKeyDescriptionTlConstructor : TlConstructor<DhtKeyDescription>(
    schema = "dht.keyDescription key:dht.key id:PublicKey update_rule:dht.UpdateRule signature:bytes = dht.KeyDescription"
) {
    override fun encode(writer: TlWriter, value: DhtKeyDescription) {
        writer.write(DhtKey, value.key)
        writer.write(PublicKey, value.id)
        writer.write(DhtUpdateRule, value.updateRule)
        writer.writeBytes(value.signature)
    }

    override fun decode(reader: TlReader): DhtKeyDescription {
        val key = reader.read(DhtKey)
        val id = reader.read(PublicKey)
        val updateRule = reader.read(DhtUpdateRule)
        val signature = reader.readByteString()
        return DhtKeyDescription(key, id, updateRule, signature)
    }
}
