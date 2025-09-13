package org.ton.kotlin.adnl

import io.ktor.utils.io.*
import kotlinx.io.bytestring.ByteString
import kotlinx.io.write

class AdnlQuery(
    val peerPair: AdnlPeerPair,
    val input: ByteString,
    val output: ByteWriteChannel
) {
    @OptIn(InternalAPI::class)
    suspend fun respond(message: ByteString) {
        output.writeBuffer.write(message)
        output.flushAndClose()
    }
}
