package org.ton.api.dht

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.SignedTlObject
import org.ton.api.adnl.AdnlAddressList
import org.ton.api.adnl.AdnlIdShort
import org.ton.api.adnl.AdnlNode
import org.ton.sdk.crypto.PublicKey
import org.ton.sdk.crypto.SignatureVerifier
import org.ton.sdk.crypto.Signer
import org.ton.sdk.tl.TL
import org.ton.sdk.tl.serializers.ByteStringBase64Serializer
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
public data class DhtNode(
    @get:JvmName("id")
    val id: PublicKey,

    @SerialName("addr_list")
    @get:JvmName("addrList")
    val addrList: AdnlAddressList,

    @get:JvmName("version")
    val version: Int = 0,

    @get:JvmName("signature")
    @Serializable(ByteStringBase64Serializer::class)
    override val signature: ByteString
) : SignedTlObject<DhtNode> {
    public fun toAdnlNode(): AdnlNode = AdnlNode(id, addrList)
    public fun key(): AdnlIdShort = AdnlIdShort(id.computeShortId())

    override fun signed(privateKey: Signer): DhtNode =
        copy(signature = ByteString(*privateKey.signToByteArray(tlCodec().encodeToByteArray(this))))

    override fun verify(publicKey: SignatureVerifier): Boolean =
        publicKey.verifySignature(tlCodec().encodeToByteArray(copy(signature = ByteString())), signature.toByteArray())

    override fun tlCodec(): TlCodec<DhtNode> = DhtNodeTlConstructor

    public companion object : TlCodec<DhtNode> by DhtNodeTlConstructor
}

private object DhtNodeTlConstructor : TlConstructor<DhtNode>(
    schema = "dht.node id:PublicKey addr_list:adnl.addressList version:int signature:bytes = dht.Node"
) {
    override fun encode(writer: TlWriter, value: DhtNode) {
        TL.Boxed.encodeIntoSink(PublicKey.serializer(), value.id, writer.output)
        writer.write(AdnlAddressList, value.addrList)
        writer.writeInt(value.version)
        writer.writeBytes(value.signature)
    }

    override fun decode(reader: TlReader): DhtNode {
        val id = TL.Boxed.decodeFromSource(PublicKey.serializer(), reader.input)
        val addrList = reader.read(AdnlAddressList)
        val version = reader.readInt()
        val signature = reader.readByteString()
        return DhtNode(id, addrList, version, signature)
    }
}
