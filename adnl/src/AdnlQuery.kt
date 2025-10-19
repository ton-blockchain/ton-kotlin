package org.ton.kotlin.adnl

import io.ktor.utils.io.*
import kotlinx.io.bytestring.ByteString
import kotlinx.io.write

public class AdnlQuery(
    public val channel: AdnlChannel,
    public val input: ByteString,
    public val output: ByteWriteChannel
) {
    @OptIn(InternalAPI::class)
    public suspend fun respond(message: ByteString) {
        output.writeBuffer.write(message)
        output.flushAndClose()
    }
}
