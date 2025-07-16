package org.ton.kotlin.adnl

import org.ton.kotlin.adnl.channel.AdnlChannel
import org.ton.kotlin.adnl.message.AdnlMessage
import org.ton.kotlin.crypto.PrivateKeyEd25519
import org.ton.kotlin.crypto.PublicKeyEd25519
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class AdnlPeerPair(
    val localKey: PrivateKeyEd25519,
    val remoteKey: PublicKeyEd25519,
    initialAddress: Address
) {
    val localId = AdnlIdShort(localKey.computeShortId())
    val remoteId = AdnlIdShort(remoteKey.computeShortId())

    private var channelKey = PrivateKeyEd25519.random()
    private var reinitDate = Clock.System.now()
    private var channel: AdnlChannel? = null
    private var activeAddress = initialAddress

    fun sendMessage(message: AdnlMessage) {
        val channel = channel

        val additionalMessage = when {
            channel == null -> AdnlMessage.CreateChannel(
                key = channelKey.publicKey(),
                date = Clock.System.now()
            )

            !channel.isReady -> AdnlMessage.ConfirmChannel(
                key = channelKey.publicKey(),
                peerKey = channel.remoteKey,
                date = Clock.System.now()
            )

            else -> null
        }
        val totalSize = (additionalMessage?.size ?: 0) + message.size
        val MAX_ADNL_MESSAGE_SIZE = 1024

        if (totalSize <= MAX_ADNL_MESSAGE_SIZE) {

        } else {
            // TODO: multi part message
            throw IllegalStateException("Message size exceeds maximum allowed size: $totalSize > $MAX_ADNL_MESSAGE_SIZE")
        }
    }

    private fun sendPacket()

    fun processMessage(message: AdnlMessage) {
        when (message) {
            is AdnlMessage.Answer -> TODO()
            is AdnlMessage.ConfirmChannel -> TODO()
            is AdnlMessage.CreateChannel -> createChannel(message.key, message.date)
            is AdnlMessage.Custom -> TODO()
            AdnlMessage.Nop -> TODO()
            is AdnlMessage.Part -> TODO()
            is AdnlMessage.Reinit -> TODO()
        }
    }

    private fun createChannel(key: PublicKeyEd25519, date: Instant) {
        TODO("Not yet implemented")
    }


}
