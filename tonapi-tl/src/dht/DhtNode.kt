package org.ton.kotlin.api.dht

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.api.SignedTlObject
import org.ton.kotlin.api.adnl.AdnlAddressList
import org.ton.kotlin.api.adnl.AdnlIdShort
import org.ton.kotlin.api.adnl.AdnlNode
import org.ton.kotlin.api.pk.PrivateKey
import org.ton.kotlin.api.pub.PublicKey
import org.ton.kotlin.tl.*
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
    public fun key(): AdnlIdShort = id.toAdnlIdShort()

    override fun signed(privateKey: PrivateKey): DhtNode =
        copy(signature = ByteString(*privateKey.sign(tlCodec().encodeToByteArray(this))))

    override fun verify(publicKey: PublicKey): Boolean =
        publicKey.verify(tlCodec().encodeToByteArray(copy(signature = ByteString())), signature.toByteArray())

    override fun tlCodec(): TlCodec<DhtNode> = DhtNodeTlConstructor

    public companion object : TlCodec<DhtNode> by DhtNodeTlConstructor
}

private object DhtNodeTlConstructor : TlConstructor<DhtNode>(
    schema = "dht.node id:PublicKey addr_list:adnl.addressList version:int signature:bytes = dht.Node"
) {
    override fun encode(writer: TlWriter, value: DhtNode) {
        writer.write(PublicKey, value.id)
        writer.write(AdnlAddressList, value.addrList)
        writer.writeInt(value.version)
        writer.writeBytes(value.signature)
    }

    override fun decode(reader: TlReader): DhtNode {
        val id = reader.read(PublicKey)
        val addrList = reader.read(AdnlAddressList)
        val version = reader.readInt()
        val signature = reader.readByteString()
        return DhtNode(id, addrList, version, signature)
    }
}
