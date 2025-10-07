package org.ton.api.overlay

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.isEmpty
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.SignedTlObject
import org.ton.kotlin.crypto.PublicKey
import org.ton.kotlin.crypto.SignatureVerifier
import org.ton.kotlin.crypto.Signer
import org.ton.kotlin.tl.TL
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer
import org.ton.tl.TlCodec
import org.ton.tl.TlConstructor
import org.ton.tl.TlReader
import org.ton.tl.TlWriter

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

    override fun signed(signer: Signer): OverlayNode =
        copy(
            signature = ByteString(
                *signer.signToByteArray(
                    tlCodec().encodeToByteArray(
                        copy(signature = ByteString())
                    )
                )
            )
        )

    override fun verify(signatureVerifier: SignatureVerifier): Boolean {
        if (signature.isEmpty()) return false
        val check = copy(
            signature = ByteString()
        )
        return signatureVerifier.verifySignature(tlCodec().encodeToByteArray(check), signature.toByteArray())
    }

    override fun tlCodec(): TlCodec<OverlayNode> = Companion

    public companion object : TlConstructor<OverlayNode>(
        schema = "overlay.node id:PublicKey overlay:int256 version:int signature:bytes = overlay.Node",
    ) {
        override fun encode(writer: TlWriter, value: OverlayNode) {
            TL.Boxed.encodeIntoSink(PublicKey.serializer(), value.id, writer.output)
            writer.writeRaw(value.overlay)
            writer.writeInt(value.version)
            writer.writeBytes(value.signature)
        }

        override fun decode(reader: TlReader): OverlayNode {
            val id = TL.Boxed.decodeFromSource(PublicKey.serializer(), reader.input)
            val overlay = reader.readByteString(32)
            val version = reader.readInt()
            val signature = reader.readByteString()
            return OverlayNode(id, overlay, version, signature)
        }
    }
}
