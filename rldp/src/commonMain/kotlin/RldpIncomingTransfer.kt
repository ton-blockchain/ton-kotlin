package org.ton.kotlin.rldp

import io.ktor.utils.io.core.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.io.Sink
import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.fec.FecDecoder
import org.ton.kotlin.rldp.congestion.Ack
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private val CONFIRM_INTERVAL = 10.milliseconds

@OptIn(ExperimentalTime::class)
internal class RldpIncomingTransfer(
    val transferId: ByteString,
    val sink: Sink,
    val incoming: ReceiveChannel<Rldp2MessagePart>,
    val outgoing: SendChannel<Rldp2MessagePart.Acknowledgment>,
) {
    var remainingBytes = -1L
        private set
    var totalSize = -1L
        private set

    suspend fun receive() = coroutineScope {
        var partId = 0
        do {
            val partReceiver = PartReceiver(partId++)
            partReceiver.receive()
        } while (remainingBytes > 0 && isActive)
    }

    private inner class PartReceiver(
        val partId: Int
    ) {
        private var lastConfirm = Instant.DISTANT_PAST
        private var lastConfirmSeqno = -1
        private val ack = Ack()
        private var fecDecoder: FecDecoder? = null
        private var buf = byteArrayOf()
        private var isDone = false

        suspend fun receive() = coroutineScope {
            while (!isDone && isActive) {
                val msg = incoming.receive()
                processMessage(msg)
            }
        }

        private suspend fun processMessage(msg: Rldp2MessagePart) {
            when (msg) {
                is Rldp2MessagePart.Part -> processSymbol(msg)
                is Rldp2MessagePart.Complete -> {
                    // ignore
                }

                is Rldp2MessagePart.Confirm -> {
                    // ignore
                }
            }
        }

        private suspend fun processSymbol(msg: Rldp2MessagePart.Part) {
            if (totalSize == -1L) {
                totalSize = msg.totalSize
                remainingBytes = msg.totalSize
            } else if (totalSize != msg.totalSize) {
                return
            }
            if (msg.transferId != transferId) {
                return
            }
            if (msg.part != partId) {
                outgoing.send(Rldp2MessagePart.Complete(transferId, msg.part))
                return
            }
            if (!ack.onReceivedPacket(msg.seqno)) {
                sendConfirm()
                return
            } else if (lastConfirmSeqno == -1) {
                sendConfirm()
            } else if (msg.seqno <= lastConfirmSeqno) {
                sendConfirm()
            } else if (msg.seqno - lastConfirmSeqno >= 24) {
                sendConfirm()
            } else if (Clock.System.now() - lastConfirm >= CONFIRM_INTERVAL) {
                sendConfirm()
            }

            val decoder = fecDecoder ?: msg.fecType.createDecoder().also {
                fecDecoder = it
                buf = ByteArray(it.parameters.dataSize)
            }
            if (decoder.parameters != msg.fecType) {
                return
            }
            val canTryDecode = decoder.addSymbol(msg.seqno, msg.data.toByteArray())
            if (canTryDecode) {
                isDone = decoder.decodeFullyIntoByteArray(buf)
                if (isDone) {
                    remainingBytes -= decoder.parameters.dataSize
                    outgoing.send(Rldp2MessagePart.Complete(transferId, partId))
                    sink.writeFully(buf)
                }
            }
        }

        private suspend fun sendConfirm() {
            val confirm = Rldp2MessagePart.Confirm(
                transferId = transferId,
                part = partId,
                maxSeqno = ack.maxSeqno,
                receivedMask = ack.receivedMask,
                receivedCount = ack.receivedCount
            )
            outgoing.send(confirm)
            lastConfirm = Clock.System.now()
            lastConfirmSeqno = ack.maxSeqno
        }
    }
}
