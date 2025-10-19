package org.ton.kotlin.adnl

import io.ktor.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.io.bytestring.ByteString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

public interface AdnlChannel : CoroutineScope {
    public val attributes: Attributes

    public val localNode: AdnlLocalNode

    public val remoteNode: AdnlNode

    public suspend fun sendMessage(message: ByteString)

    public suspend fun sendQuery(query: ByteString, timeout: Duration = 5.seconds): ByteString
}
