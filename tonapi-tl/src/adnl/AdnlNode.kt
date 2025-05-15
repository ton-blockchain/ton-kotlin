package org.ton.kotlin.api.adnl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.api.pub.PublicKey
import org.ton.kotlin.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("adnl.node")
public data class AdnlNode(
    @get:JvmName("id")
    val id: PublicKey,

    @SerialName("addr_list")
    @get:JvmName("addrList")
    val addrList: AdnlAddressList
) {
    public companion object : TlConstructor<AdnlNode>(
        schema = "adnl.node id:PublicKey addr_list:adnl.addressList = adnl.Node"
    ) {
        override fun encode(writer: TlWriter, value: AdnlNode) {
            writer.write(PublicKey, value.id)
            writer.write(AdnlAddressList, value.addrList)
        }

        override fun decode(reader: TlReader): AdnlNode {
            val id = reader.read(PublicKey)
            val addrList = reader.read(AdnlAddressList)
            return AdnlNode(id, addrList)
        }
    }
}
