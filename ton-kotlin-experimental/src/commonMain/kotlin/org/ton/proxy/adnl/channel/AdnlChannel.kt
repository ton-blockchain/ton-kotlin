@file:Suppress("NOTHING_TO_INLINE")

package org.ton.proxy.adnl.channel

import io.ktor.util.*
import io.ktor.utils.io.core.*
import kotlinx.atomicfu.atomic
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.ton.kotlin.adnl.pk.PrivateKeyAes
import org.ton.kotlin.adnl.pk.PrivateKeyEd25519
import org.ton.kotlin.adnl.pub.PublicKeyAes
import org.ton.kotlin.adnl.pub.PublicKeyEd25519
import org.ton.proxy.adnl.AdnlPeerSession
import kotlin.jvm.JvmStatic

inline fun AdnlChannel(
    peerSession: AdnlPeerSession,
    channelLocalKey: PrivateKeyEd25519,
    channelRemoteKey: PublicKeyEd25519,
    isReady: Boolean = false,
    date: Instant = Clock.System.now(),
): AdnlChannel = AdnlChannel.of(peerSession, channelLocalKey, channelRemoteKey, isReady, date)

interface AdnlChannel {
    val peerSession: AdnlPeerSession
    val input: AdnlInputChannel
    val output: AdnlOutputChannel
    val publicKey: PublicKeyEd25519
    var isReady: Boolean
    val date: Instant

    suspend fun sendDatagram(payload: ByteArray)
    fun receiveDatagram(payload: ByteArray)

    companion object {
        @JvmStatic
        fun of(
            peerSession: AdnlPeerSession,
            channelLocalKey: PrivateKeyEd25519,
            channelRemoteKey: PublicKeyEd25519,
            isReady: Boolean = false,
            date: Instant = Clock.System.now()
        ): AdnlChannel {
            val sharedKey = channelLocalKey.sharedKey(channelRemoteKey)
            val reversedSharedKey = sharedKey.reversedArray()

            val input: AdnlInputChannel
            val output: AdnlOutputChannel
            val localId = peerSession.localKey.toAdnlIdShort()
            val remoteId = peerSession.remoteKey.toAdnlIdShort()
            when (localId.compareTo(remoteId)) {
                -1 -> {
                    input = AdnlInputChannel(PrivateKeyAes(sharedKey))
                    output = AdnlOutputChannel(PublicKeyAes(reversedSharedKey))
                }

                1 -> {
                    input = AdnlInputChannel(PrivateKeyAes(reversedSharedKey))
                    output = AdnlOutputChannel(PublicKeyAes(sharedKey))
                }

                else -> {
                    input = AdnlInputChannel(PrivateKeyAes(sharedKey))
                    output = AdnlOutputChannel(PublicKeyAes(sharedKey))
                }
            }
            return AdnlChannelImpl(peerSession, input, output, channelRemoteKey, isReady, date)
        }
    }
}

private class AdnlChannelImpl(
    override val peerSession: AdnlPeerSession,
    override val input: AdnlInputChannel,
    override val output: AdnlOutputChannel,
    override val publicKey: PublicKeyEd25519,
    isReady: Boolean,
    override val date: Instant,
) : AdnlChannel {
    private val _isReady = atomic(isReady)
    override var isReady by _isReady

    override suspend fun sendDatagram(payload: ByteArray) {
        val destId = output.id.id
        val encryptedPayload = output.key.encrypt(payload)
        val datagram = ByteArray(destId.size + encryptedPayload.size)
        destId.copyInto(datagram)
        encryptedPayload.copyInto(datagram, destId.size)
        peerSession.adnl.sendDatagram(peerSession.remoteKey.toAdnlIdShort(), datagram)
    }

    override fun receiveDatagram(payload: ByteArray) {
        val decryptedPayload = input.key.decrypt(payload)
        peerSession.receiveDatagram(decryptedPayload, this)
    }
}
