package org.ton.kotlin.adnl.connection

import io.ktor.utils.io.core.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.ton.kotlin.api.http.functions.HttpRequest
import kotlin.coroutines.CoroutineContext

public class AdnlRequestData(
    public val body: ByteArray,
    public val executionContext: Job
)

public class AdnlResponseData(
    public val requestTime: Instant,
    public val body: ByteReadPacket,
    public val callContext: CoroutineContext
) {
    public val responseTime: Instant = Clock.System.now()
}

internal data class RequestTask(
    val request: HttpRequest,
    val response: CompletableDeferred<AdnlResponseData>,
    val context: CoroutineContext
)
