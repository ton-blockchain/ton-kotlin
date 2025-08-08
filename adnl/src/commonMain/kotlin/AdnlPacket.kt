@file:OptIn(ExperimentalTime::class)
@file:UseSerializers(ByteStringSerializer::class)

package org.ton.kotlin.adnl

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.encodeToByteArray
import org.ton.kotlin.adnl.message.AdnlMessage
import org.ton.kotlin.adnl.serializers.ByteStringSerializer
import org.ton.kotlin.crypto.PrivateKey
import org.ton.kotlin.tl.TL
import org.ton.kotlin.tl.TlConditional
import org.ton.kotlin.tl.TlConstructorId
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
@TlConstructorId(0xd142cd89)
data class AdnlPacket(
    val rand1: ByteString,
    val flags: Int,
    @TlConditional("flags", FLAG_FROM)
    val from: AdnlIdFull?,
    @TlConditional("flags", FLAG_FROM_SHORT)
    val fromShort: AdnlIdShort?,
    @TlConditional("flags", FLAG_ONE_MESSAGE)
    val message: AdnlMessage?,
    @TlConditional("flags", FLAG_MULTI_MESSAGES)
    val messages: List<AdnlMessage>?,
    @TlConditional("flags", FLAG_ADDRESS)
    val address: AdnlAddressList?,
    @TlConditional("flags", FLAG_PRIORITY_ADDRESS)
    val priorityAddress: AdnlAddressList?,
    @TlConditional("flags", FLAG_SEQNO)
    val seqno: Long?,
    @TlConditional("flags", FLAG_CONFIRM_SEQNO)
    val confirmSeqno: Long?,
    @TlConditional("flags", FLAG_RECV_ADDR_VERSION)
    val recvAddressListVersion: Int?,
    @TlConditional("flags", FLAG_RECV_PRIORITY_ADDR_VERSION)
    val recvPriorityAddressListVersion: Int?,
    @TlConditional("flags", FLAG_REINIT_DATE)
    val reinitDate: Int?,
    @TlConditional("flags", FLAG_REINIT_DATE)
    val dstReinitDate: Int?,
    @TlConditional("flags", FLAG_SIGNATURE)
    val signature: ByteString?,
    val rand2: ByteString
) {
    fun toSign() = copy(
        flags = flags and FLAG_SIGNATURE.inv(),
        signature = null
    )

    fun sign(privateKey: PrivateKey): ByteString {
        val raw = TL.Boxed.encodeToByteArray(toSign())
        return ByteString(*privateKey.createDecryptor().signToByteArray(raw))
    }

    fun signed(privateKey: PrivateKey): AdnlPacket {
        val signature = sign(privateKey)
        return copy(
            flags = flags or FLAG_SIGNATURE,
            signature = signature
        )
    }

    companion object {
        const val FLAG_FROM = 1 shl 0
        const val FLAG_FROM_SHORT = 1 shl 1
        const val FLAG_ONE_MESSAGE = 1 shl 2
        const val FLAG_MULTI_MESSAGES = 1 shl 3
        const val FLAG_ADDRESS = 1 shl 4
        const val FLAG_PRIORITY_ADDRESS = 1 shl 5
        const val FLAG_SEQNO = 1 shl 6
        const val FLAG_CONFIRM_SEQNO = 1 shl 7
        const val FLAG_RECV_ADDR_VERSION = 1 shl 8
        const val FLAG_RECV_PRIORITY_ADDR_VERSION = 1 shl 9
        const val FLAG_REINIT_DATE = 1 shl 10
        const val FLAG_SIGNATURE = 1 shl 11
        const val FLAG_PRIORITY = 0x1000
        const val FLAG_ALL = 0x1FFF
    }
}

class AdnlPacketBuilder {
    var rand1: ByteArray = EMPTY_ARRAY
    var rand2: ByteArray = EMPTY_ARRAY
    var isPriority: Boolean = false
    var signature: ByteString? = null
    var fromShort: AdnlIdShort? = null
        set(value) {
            if (from == null) {
                field = value
            }
        }
    var from: AdnlIdFull? = null
        set(value) {
            fromShort = value?.idShort
            field = value
        }

    var messages: MutableList<AdnlMessage> = mutableListOf()
    var addressList: AdnlAddressList? = null
    var priorityAddressList: AdnlAddressList? = null
    var seqno: Long = -1L
    var confirmSeqno: Long = -1L
    var receivedAddressListVersion: Int = -1
    var receivedPriorityAddressListVersion: Int = -1
    var reinitDate: Instant? = null
    var dstReinitDate: Instant? = null

    fun initRandom() {
        rand1 = Random.nextBytes(if (Random.nextBoolean()) 7 else 15)
        rand2 = Random.nextBytes(if (Random.nextBoolean()) 7 else 15)
    }

    fun toSign(): ByteArray {
        val currentSignature = signature
        try {
            signature = null
            return TL.Boxed.encodeToByteArray(build())
        } finally {
            signature = currentSignature
        }
    }

    fun build(): AdnlPacket {
        var flags = 0
        if (from != null) {
            flags = flags or AdnlPacket.FLAG_FROM
        } else if (fromShort != null) {
            flags = flags or AdnlPacket.FLAG_FROM_SHORT
        }
        if (messages.size == 1) {
            flags = flags or AdnlPacket.FLAG_ONE_MESSAGE
        } else if (messages.isNotEmpty()) {
            flags = flags or AdnlPacket.FLAG_MULTI_MESSAGES
        }
        if (addressList != null) {
            flags = flags or AdnlPacket.FLAG_ADDRESS
        }
        if (priorityAddressList != null) {
            flags = flags or AdnlPacket.FLAG_PRIORITY_ADDRESS
        }
        if (receivedAddressListVersion != -1) {
            flags = flags or AdnlPacket.FLAG_RECV_ADDR_VERSION
        }
        if (receivedPriorityAddressListVersion != -1) {
            flags = flags or AdnlPacket.FLAG_RECV_PRIORITY_ADDR_VERSION
        }
        if (seqno != -1L) {
            flags = flags or AdnlPacket.FLAG_SEQNO
        }
        if (confirmSeqno != -1L) {
            flags = flags or AdnlPacket.FLAG_CONFIRM_SEQNO
        }
        if (reinitDate != null) {
            flags = flags or AdnlPacket.FLAG_REINIT_DATE
        }
        if (signature != null) {
            flags = flags or AdnlPacket.FLAG_SIGNATURE
        }
        if (isPriority) {
            flags = flags or AdnlPacket.FLAG_PRIORITY
        }
        return AdnlPacket(
            rand1 = ByteString(rand1),
            flags = flags,
            from = from,
            fromShort = if (from == null) fromShort else null,
            message = if (messages.size == 1) messages.first() else null,
            messages = if (messages.size > 1) messages.toList() else null,
            address = addressList,
            priorityAddress = priorityAddressList,
            seqno = if (seqno != -1L) seqno else null,
            recvAddressListVersion = if (receivedAddressListVersion != -1) receivedAddressListVersion else null,
            recvPriorityAddressListVersion = if (receivedPriorityAddressListVersion != -1) receivedPriorityAddressListVersion else null,
            confirmSeqno = if (confirmSeqno != -1L) confirmSeqno else null,
            reinitDate = reinitDate?.epochSeconds?.toInt(),
            dstReinitDate = dstReinitDate?.epochSeconds?.toInt(),
            signature = signature,
            rand2 = ByteString(rand2)
        )
    }

    companion object {
        private val EMPTY_ARRAY = ByteArray(0)
    }
}
