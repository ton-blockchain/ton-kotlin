package org.ton.kotlin.adnl.connection

import io.ktor.utils.io.core.*
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withTimeout
import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.adnl.adnl.message.AdnlMessageAnswer
import org.ton.kotlin.adnl.adnl.message.AdnlMessageQuery
import org.ton.kotlin.adnl.liteserver.LiteServerDesc
import kotlin.random.Random
import kotlin.time.Duration

public class AdnlClientImpl(
    private val liteServerDesc: LiteServerDesc
) : AdnlClient {
    override suspend fun sendQuery(data: ByteArray, timeout: Duration): ByteArray {
        val adnlConnection = connectionPool.selectConnection(liteServerDesc)
        val queryId = ByteString(*Random.nextBytes(32))
        val context = SupervisorJob()
        try {
            return withTimeout(timeout) {
                val response = adnlConnection.execute(
                    AdnlRequestData(
                        AdnlMessageQuery.encodeToByteArray(
                            AdnlMessageQuery(queryId, ByteString(*data)), true
                        ),
                        context
                    ), context
                )
                AdnlMessageAnswer.decodeBoxed(
                    response.body.readBytes()
                ).answer.toByteArray()
            }
        } catch (e: Throwable) {
            throw e
        }
    }

    public companion object {
        private val connectionPool: AdnlConnectionPool = AdnlConnectionPool()
    }
}
