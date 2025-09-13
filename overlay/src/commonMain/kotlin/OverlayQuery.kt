package org.ton.kotlin.overlay

import io.ktor.utils.io.*
import kotlinx.io.bytestring.ByteString
import kotlinx.io.write
import org.ton.kotlin.adnl.AdnlPeerPair

class OverlayQuery(
    val overlay: Overlay,
    val peer: AdnlPeerPair,
    val input: ByteString,
    val output: ByteWriteChannel
) {
    @OptIn(InternalAPI::class)
    suspend fun respond(data: ByteString) {
        output.writeBuffer.write(data)
        output.flushAndClose()
    }
}
