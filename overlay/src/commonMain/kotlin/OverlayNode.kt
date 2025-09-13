@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.kotlin.overlay

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.encodeToByteArray
import org.ton.kotlin.adnl.AdnlIdFull
import org.ton.kotlin.adnl.AdnlIdShort
import org.ton.kotlin.crypto.PrivateKey
import org.ton.kotlin.tl.TL
import org.ton.kotlin.tl.TlConstructorId
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
@SerialName("overlay.node")
@TlConstructorId(0xb86b8a83)
data class OverlayNodeInfo(
    val id: AdnlIdFull,
    val overlay: OverlayIdShort,
    val version: Int = -1,
    val signature: ByteString = ByteString()
) {
    fun toSign(): OverlayNodeInfoToSign = OverlayNodeInfoToSign(this)

    fun signed(privateKey: PrivateKey) = copy(
        signature = ByteString(
            *privateKey.createDecryptor().signToByteArray(
                TL.Boxed.encodeToByteArray(toSign())
            )
        )
    )
}

@Serializable
@SerialName("overlay.node.toSign")
@TlConstructorId(0x03d8a8e1)
data class OverlayNodeInfoToSign(
    val id: AdnlIdShort,
    val overlay: OverlayIdShort,
    val version: Int = -1,
) {
    constructor(nodeInfo: OverlayNodeInfo) : this(nodeInfo.id.shortId, nodeInfo.overlay, nodeInfo.version)
}

@Serializable
@SerialName("overlay.nodes")
@TlConstructorId(0xe487290e)
data class OverlayNodeInfoList(
    val nodes: List<OverlayNodeInfo> = listOf(),
) : List<OverlayNodeInfo> by nodes {
    constructor(vararg nodes: OverlayNodeInfo) : this(nodes.toList())
    constructor(nodes: Collection<OverlayNodeInfo>) : this(nodes.toList())
}
