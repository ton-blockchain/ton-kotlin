package org.ton.kotlin.overlay

import kotlinx.coroutines.flow.Flow

interface OverlayNodeFinder {
    suspend fun findOverlayNodes(id: OverlayIdFull): Flow<OverlayNodeInfoList> =
        findOverlayNodes(id.shortId)

    suspend fun findOverlayNodes(id: OverlayIdShort): Flow<OverlayNodeInfoList>
}
