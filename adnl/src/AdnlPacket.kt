@file:OptIn(ExperimentalTime::class)
@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.kotlin.adnl

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.encodeToByteArray
import org.ton.kotlin.adnl.message.AdnlMessage
import org.ton.kotlin.crypto.Signer
import org.ton.kotlin.tl.TL
import org.ton.kotlin.tl.TlConditional
import org.ton.kotlin.tl.TlConstructorId
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private val EMPTY_ARRAY = ByteArray(0)
private const val FLAG_FROM = 1 shl 0
private const val FLAG_FROM_SHORT = 1 shl 1
private const val FLAG_ONE_MESSAGE = 1 shl 2
private const val FLAG_MULTI_MESSAGES = 1 shl 3
private const val FLAG_ADDRESS = 1 shl 4
private const val FLAG_PRIORITY_ADDRESS = 1 shl 5
private const val FLAG_SEQNO = 1 shl 6
private const val FLAG_CONFIRM_SEQNO = 1 shl 7
private const val FLAG_RECV_ADDR_VERSION = 1 shl 8
private const val FLAG_RECV_PRIORITY_ADDR_VERSION = 1 shl 9
private const val FLAG_REINIT_DATE = 1 shl 10
private const val FLAG_SIGNATURE = 1 shl 11
private const val FLAG_PRIORITY = 0x1000
private const val FLAG_ALL = 0x1FFF

@Serializable
@TlConstructorId(0xd142cd89)
public class AdnlPacket(
    public val rand1: ByteString,
    public val flags: Int,
    @TlConditional("flags", FLAG_FROM)
    public val from: AdnlIdFull?,
    @TlConditional("flags", FLAG_FROM_SHORT)
    public val fromShort: AdnlIdShort?,
    @TlConditional("flags", FLAG_ONE_MESSAGE)
    public val message: AdnlMessage?,
    @TlConditional("flags", FLAG_MULTI_MESSAGES)
    public val messages: List<AdnlMessage>?,
    @TlConditional("flags", FLAG_ADDRESS)
    public val address: AdnlAddressList?,
    @TlConditional("flags", FLAG_PRIORITY_ADDRESS)
    public val priorityAddress: AdnlAddressList?,
    @TlConditional("flags", FLAG_SEQNO)
    public val seqno: Long?,
    @TlConditional("flags", FLAG_CONFIRM_SEQNO)
    public val confirmSeqno: Long?,
    @TlConditional("flags", FLAG_RECV_ADDR_VERSION)
    public val recvAddressListVersion: Int?,
    @TlConditional("flags", FLAG_RECV_PRIORITY_ADDR_VERSION)
    public val recvPriorityAddressListVersion: Int?,
    @TlConditional("flags", FLAG_REINIT_DATE)
    public val reinitDate: Int?,
    @TlConditional("flags", FLAG_REINIT_DATE)
    public val dstReinitDate: Int?,
    @TlConditional("flags", FLAG_SIGNATURE)
    public val signature: ByteString?,
    public val rand2: ByteString
) {
    public fun toSign(): AdnlPacket = AdnlPacket(
        rand1 = rand1,
        flags = flags and FLAG_SIGNATURE.inv(),
        from = from,
        fromShort = fromShort,
        message = message,
        messages = messages,
        address = address,
        priorityAddress = priorityAddress,
        seqno = seqno,
        confirmSeqno = confirmSeqno,
        recvAddressListVersion = recvAddressListVersion,
        recvPriorityAddressListVersion = recvPriorityAddressListVersion,
        reinitDate = reinitDate,
        dstReinitDate = dstReinitDate,
        signature = null,
        rand2 = rand2
    )

    public fun signed(signer: Signer): AdnlPacket {
        val toSign = toSign()
        val toSignBytes = TL.Boxed.encodeToByteArray(toSign)
        val signature = ByteString(*signer.signToByteArray(toSignBytes))

        return AdnlPacket(
            rand1 = rand1,
            flags = flags or FLAG_SIGNATURE,
            from = from,
            fromShort = fromShort,
            message = message,
            messages = messages,
            address = address,
            priorityAddress = priorityAddress,
            seqno = seqno,
            confirmSeqno = confirmSeqno,
            recvAddressListVersion = recvAddressListVersion,
            recvPriorityAddressListVersion = recvPriorityAddressListVersion,
            reinitDate = reinitDate,
            dstReinitDate = dstReinitDate,
            signature = signature,
            rand2 = rand2
        )
    }
}


public class AdnlPacketBuilder {
    public var rand1: ByteArray = EMPTY_ARRAY
    public var rand2: ByteArray = EMPTY_ARRAY
    public var isPriority: Boolean = false
    public var signature: ByteString? = null
    public var fromShort: AdnlIdShort? = null
        set(value) {
            if (from == null) {
                field = value
            }
        }
    public var from: AdnlIdFull? = null
        set(value) {
            fromShort = value?.shortId
            field = value
        }

    public var messages: MutableList<AdnlMessage> = mutableListOf()
    public var addressList: AdnlAddressList? = null
    public var priorityAddressList: AdnlAddressList? = null
    public var seqno: Long = -1L
    public var confirmSeqno: Long = -1L
    public var receivedAddressListVersion: Int = -1
    public var receivedPriorityAddressListVersion: Int = -1
    public var reinitDate: Instant? = null
    public var dstReinitDate: Instant? = null

    public fun initRandom() {
        rand1 = Random.nextBytes(if (Random.nextBoolean()) 7 else 15)
        rand2 = Random.nextBytes(if (Random.nextBoolean()) 7 else 15)
    }

    public fun build(): AdnlPacket {
        var flags = 0
        if (from != null) {
            flags = flags or FLAG_FROM
        } else if (fromShort != null) {
            flags = flags or FLAG_FROM_SHORT
        }
        if (messages.size == 1) {
            flags = flags or FLAG_ONE_MESSAGE
        } else if (messages.isNotEmpty()) {
            flags = flags or FLAG_MULTI_MESSAGES
        }
        if (addressList != null) {
            flags = flags or FLAG_ADDRESS
        }
        if (priorityAddressList != null) {
            flags = flags or FLAG_PRIORITY_ADDRESS
        }
        if (receivedAddressListVersion != -1) {
            flags = flags or FLAG_RECV_ADDR_VERSION
        }
        if (receivedPriorityAddressListVersion != -1) {
            flags = flags or FLAG_RECV_PRIORITY_ADDR_VERSION
        }
        if (seqno != -1L) {
            flags = flags or FLAG_SEQNO
        }
        if (confirmSeqno != -1L) {
            flags = flags or FLAG_CONFIRM_SEQNO
        }
        if (reinitDate != null) {
            flags = flags or FLAG_REINIT_DATE
        }
        if (dstReinitDate != null) {
            flags = flags or FLAG_REINIT_DATE
        }
        if (signature != null) {
            flags = flags or FLAG_SIGNATURE
        }
        if (isPriority) {
            flags = flags or FLAG_PRIORITY
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
            reinitDate = if (reinitDate == Instant.DISTANT_PAST) 0 else reinitDate?.epochSeconds?.toInt()
                ?: if (dstReinitDate != null) 0 else null,
            dstReinitDate = if (dstReinitDate == Instant.DISTANT_PAST) 0 else dstReinitDate?.epochSeconds?.toInt()
                ?: if (reinitDate != null) 0 else null,
            signature = signature,
            rand2 = ByteString(rand2)
        )
    }
}
