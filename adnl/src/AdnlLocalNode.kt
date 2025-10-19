package org.ton.kotlin.adnl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.io.bytestring.ByteString

public interface AdnlLocalNode : CoroutineScope {
    public val id: AdnlIdFull

    public val shortId: AdnlIdShort get() = id.shortId

    public val incomingChannels: SharedFlow<AdnlChannel>

    public fun createChannel(node: AdnlNode): AdnlChannel
}

internal typealias AdnlMessageHandler = suspend AdnlChannel.(ByteString) -> Unit
internal typealias AdnlQueryHandler = suspend AdnlChannel.(AdnlQuery) -> Unit

public class AdnlLocalNodeBuilder() {
    internal val onMessageHandlers = ArrayList<AdnlMessageHandler>()
    internal val onQueryHandlers = ArrayList<AdnlQueryHandler>()

    public fun onMessage(block: suspend AdnlChannel.(message: ByteString) -> Unit) {
        onMessageHandlers.add(block)
    }

    public fun onQuery(block: suspend AdnlChannel.(query: AdnlQuery) -> Unit) {
        onQueryHandlers.add(block)
    }
}


public class OnMessageContext internal constructor()

public class OnQueryContext internal constructor()
