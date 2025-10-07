package org.ton.api.adnl.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.adnl.AdnlNodes
import org.ton.tl.*
import kotlin.jvm.JvmName

@SerialName("adnl.config.global")
@Serializable
public data class AdnlConfigGlobal(
    @SerialName("static_nodes")
    @get:JvmName("staticNodes")
    val staticNodes: AdnlNodes = AdnlNodes()
) {
    public companion object : TlCodec<AdnlConfigGlobal> by AdnlConfigGlobalTlConstructor
}

private object AdnlConfigGlobalTlConstructor : TlConstructor<AdnlConfigGlobal>(
    schema = "adnl.config.global static_nodes:adnl.nodes = adnl.config.Global"
) {
    override fun encode(writer: TlWriter, value: AdnlConfigGlobal) = writer {
        write(AdnlNodes, value.staticNodes)
    }

    override fun decode(reader: TlReader): AdnlConfigGlobal = reader {
        val staticNodes = read(AdnlNodes)
        AdnlConfigGlobal(staticNodes)
    }
}
