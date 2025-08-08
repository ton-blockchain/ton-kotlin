package org.ton.kotlin.adnl.server

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

class Server(
    parentCoroutineContext: CoroutineContext,
) : ServerCallPipeline(), CoroutineScope {
    private val applicationJob = SupervisorJob(parentCoroutineContext[Job])

    override val coroutineContext: CoroutineContext = parentCoroutineContext + applicationJob
}
