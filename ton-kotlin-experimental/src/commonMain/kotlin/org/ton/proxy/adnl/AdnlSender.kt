package org.ton.proxy.adnl

import org.ton.kotlin.adnl.adnl.AdnlIdShort
import org.ton.kotlin.adnl.pk.PrivateKey
import org.ton.kotlin.adnl.pub.PublicKey
import org.ton.kotlin.crypto.encodeHex
import org.ton.kotlin.tl.TLFunction
import org.ton.kotlin.tl.TlCodec
import org.ton.kotlin.tl.TlObject
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

interface AdnlSender {
    suspend fun message(
        destination: AdnlIdShort,
        payload: ByteArray
    )

    suspend fun query(
        destination: AdnlIdShort,
        payload: ByteArray,
        timeout: Duration = 5.seconds,
        maxAnswerSize: Long = Long.MAX_VALUE
    ): ByteArray

    @Suppress("UNCHECKED_CAST")
    suspend fun <Q : TLFunction<Q, A>, A : TlObject<A>> query(
        destination: AdnlIdShort,
        query: Q,
        timeout: Duration = 5.seconds,
        maxAnswerSize: Long = Long.MAX_VALUE
    ): A {
        val queryCodec = query.tlCodec() as TlCodec<Q>
        val answerCodec = query.resultTlCodec()

        val queryPayload = queryCodec.encodeBoxed(query)
        val answerPayload = query(destination, queryPayload, timeout, maxAnswerSize)

        return try {
            answerCodec.decodeBoxed(answerPayload)
        } catch (e: Throwable) {
            throw IllegalStateException("Failed to decode answer: ${answerPayload.encodeHex()}", e)
        }
    }

    fun createPeer(
        remoteKey: PublicKey,
        localKey: PrivateKey
    ): AdnlPeerSession
}
