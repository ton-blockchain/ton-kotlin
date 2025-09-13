package org.ton.kotlin.overlay

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.ton.kotlin.dht.Dht
import org.ton.kotlin.dht.DhtKey
import org.ton.kotlin.tl.TL

fun interface OverlayNodeProvider {
    suspend fun getNodes(id: OverlayIdShort): Flow<OverlayNodeInfoList>
}

class DhtOverlayNodeProvider(
    val dht: Dht
) : OverlayNodeProvider {
    override suspend fun getNodes(id: OverlayIdShort): Flow<OverlayNodeInfoList> {
        return dht.findValues(DhtKey(id.publicKeyHash, "nodes"))
            .mapNotNull { (_, result) ->
                val dhtValue = result.getOrNull()?.valueOrNull() ?: return@mapNotNull null
                runCatching {
                    TL.Boxed.decodeFromByteString<OverlayNodeInfoList>(dhtValue.value)
                }.getOrNull()
            }
    }
}
