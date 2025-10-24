package org.ton.kotlin.provider.liteapi

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.serializer
import org.ton.kotlin.provider.liteapi.internal.LiteTcpConnection
import org.ton.kotlin.provider.liteapi.model.LiteApiGetMasterchainInfo
import org.ton.kotlin.provider.liteapi.model.LiteApiMasterchainInfo
import org.ton.kotlin.provider.liteapi.model.LiteServerDesc
import org.ton.kotlin.provider.liteapi.model.LiteServerQuery
import org.ton.kotlin.tl.TL

public class LiteApiClientImpl(
    public val liteServerDesc: LiteServerDesc
) : LiteApiClient {

    override suspend fun getMasterchainInfo(): LiteApiMasterchainInfo =
        query(LiteApiGetMasterchainInfo)

    private suspend inline fun <reified Q, reified A> query(query: Q): A = query(
        query,
        serializer<Q>(),
        serializer<A>(),
    )

    private suspend fun <Q, A> query(
        query: Q,
        querySerializer: SerializationStrategy<Q>,
        answerSerializer: DeserializationStrategy<A>
    ): A {
        val rawQuery = TL.Boxed.encodeToByteArray(
            LiteServerQuery(
                TL.Boxed.encodeToByteString(querySerializer, query)
            )
        )

        val rawAnswer = LiteTcpConnection(liteServerDesc).call(rawQuery)
        return TL.Boxed.decodeFromByteArray(answerSerializer, rawAnswer)
    }
}
