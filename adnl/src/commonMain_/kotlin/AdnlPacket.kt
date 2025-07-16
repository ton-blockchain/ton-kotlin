package org.ton.kotlin.adnl

import org.ton.kotlin.adnl.message.AdnlMessage

data class AdnlPacket(
    val messages: List<AdnlMessage> = emptyList(),
    val seqno: Long = 0,
    val confirmSeqno: Long = 0,
) {
    companion object {
        fun deserialize(bytes: ByteArray): AdnlPacket {
            TODO()
        }

        fun serialize(packet: AdnlPacket): ByteArray {
            TODO()
        }
    }
}
