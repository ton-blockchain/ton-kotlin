package org.ton.api.overlay

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.isEmpty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.SignedTlObject
import org.ton.api.pub.PublicKey
import org.ton.kotlin.crypto.SignatureVerifier
import org.ton.kotlin.crypto.Signer
import org.ton.kotlin.tl.*

@Serializable
@SerialName("overlay.node")
public data class OverlayNode(
    val id: PublicKey,
    @Serializable(ByteStringBase64Serializer::class)
    val overlay: ByteString,
    val version: Int,
    @Serializable(ByteStringBase64Serializer::class)
    override val signature: ByteString = ByteString()
) : SignedTlObject<OverlayNode> {

    override fun signed(privateKey: Signer): OverlayNode =
        copy(
            signature = ByteString(
                *privateKey.signToByteArray(
                    tlCodec().encodeToByteArray(
                        copy(signature = ByteString())
                    )
                )
            )
        )

    override fun verify(publicKey: SignatureVerifier): Boolean {
        if (signature.isEmpty()) return false
        val check = copy(
            signature = ByteString()
        )
        return publicKey.verifySignature(tlCodec().encodeToByteArray(check), signature.toByteArray())
    }

    override fun tlCodec(): TlCodec<OverlayNode> = Companion

    public companion object : TlConstructor<OverlayNode>(
        schema = "overlay.node id:PublicKey overlay:int256 version:int signature:bytes = overlay.Node",
    ) {
        override fun encode(writer: TlWriter, value: OverlayNode) {
            writer.write(PublicKey, value.id)
            writer.writeRaw(value.overlay)
            writer.writeInt(value.version)
            writer.writeBytes(value.signature)
        }

        override fun decode(reader: TlReader): OverlayNode {
            val id = reader.read(PublicKey)
            val overlay = reader.readByteString(32)
            val version = reader.readInt()
            val signature = reader.readByteString()
            return OverlayNode(id, overlay, version, signature)
        }
    }
}
