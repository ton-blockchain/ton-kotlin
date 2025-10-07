package org.ton.adnl.connection

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.io.Source
import org.ton.api.http.functions.HttpRequest
import kotlin.coroutines.CoroutineContext

public class AdnlRequestData(
    public val body: ByteArray,
    public val executionContext: Job
)

public class AdnlResponseData(
    public val requestTime: Instant,
    public val body: Source,
    public val callContext: CoroutineContext
) {
    public val responseTime: Instant = Clock.System.now()
}

internal data class RequestTask(
    val request: HttpRequest,
    val response: CompletableDeferred<AdnlResponseData>,
    val context: CoroutineContext
)
