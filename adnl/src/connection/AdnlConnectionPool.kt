package org.ton.kotlin.adnl.connection

import io.ktor.util.*
import io.ktor.util.collections.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import org.ton.kotlin.api.liteserver.LiteServerDesc
import kotlin.coroutines.CoroutineContext

public class AdnlConnectionPool : CoroutineScope, Closeable {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val dispatcher: CoroutineDispatcher by lazy {
        newFixedThreadPoolContext(2, "adnl-dispatcher")
    }

    override val coroutineContext: CoroutineContext by lazy {
        SilentSupervisor() + dispatcher + CoroutineName("AdnlConnectionPool")
    }

    private val connections = ConcurrentMap<LiteServerDesc, AdnlConnection>()
    private val connectionFactory = AdnlConnectionFactory()

    public fun selectConnection(
        liteServerDesc: LiteServerDesc
    ): AdnlConnection {
        return connections.computeIfAbsent(liteServerDesc) {
            AdnlConnection(
                liteServerDesc,
                connectionFactory,
                coroutineContext,
                onDone = { connections.remove(liteServerDesc) }
            )
        }
    }

    override fun close() {
        connections.forEach { (_, connection) ->
            connection.close()
        }
    }
}
