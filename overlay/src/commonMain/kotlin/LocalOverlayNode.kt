package org.ton.kotlin.overlay

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromByteArray
import org.ton.kotlin.adnl.AdnlLocalNode
import org.ton.kotlin.adnl.AdnlNodeResolver
import org.ton.kotlin.adnl.AdnlQuery
import org.ton.kotlin.adnl.util.Hash256Map
import org.ton.kotlin.dht.Dht
import org.ton.kotlin.tl.TL
import kotlin.coroutines.CoroutineContext

class OverlayLocalNode(
    val adnl: AdnlLocalNode,
    val dht: Dht,
    val overlayNodeProvider: OverlayNodeProvider = DhtOverlayNodeProvider(dht),
    val adnlNodeResolver: AdnlNodeResolver = dht
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = adnl.coroutineContext + CoroutineName("OverlayLocalNode")
    private val overlays = Hash256Map<OverlayIdShort, Overlay>({ it.publicKeyHash })

    val key get() = adnl.key
    val id get() = adnl.id
    val shortId get() = adnl.shortId

    private val _queries = MutableSharedFlow<OverlayQuery>()
    val queries = _queries.asSharedFlow()

    private val queryJob = launch {
        adnl.queries.collect { query ->
            receiveQuery(query)
        }
    }

    fun createOverlay(
        overlayId: OverlayIdFull,
        type: OverlayType = OverlayType.Public
    ): Overlay {
        return overlays.getOrPut(overlayId.shortId) {
            Overlay(
                this,
                overlayId,
                type
            )
        }
    }

    private suspend fun receiveQuery(query: AdnlQuery) {
        val queryPrefix = runCatching {
            TL.Boxed.decodeFromByteArray<OverlayFunction.Query>(query.input.toByteArray(0, 32 + 4))
        }.getOrNull() ?: return
        val overlayId = queryPrefix.overlay
        val overlay = overlays[overlayId]
            ?: throw IllegalArgumentException("Unknown overlay ${overlayId}@${adnl.shortId} from ${query.peerPair.remoteNode.shortId}")
        val query = OverlayQuery(overlay, query.peerPair, query.input.substring(32 + 4), query.output)
        _queries.emit(query)
    }
}
