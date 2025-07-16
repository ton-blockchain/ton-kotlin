@file:OptIn(ExperimentalTime::class)

package org.ton.kotlin.adnl

import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.adnl.message.AdnlMessage
import org.ton.kotlin.crypto.PublicKey
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class AdnlPacket(
    val rand1: ByteString,
    val flags: Int,
    val from: PublicKey?,
    val fromShort: AdnlIdShort?,
    val message: AdnlMessage?,
    val messages: List<AdnlMessage>,
    val address: AddressList?,
    val seqno: Long?,
    val recvAddressListVersion: Int?,
    val recvPriorityAddressListVersion: Int?,
    val confirmSeqno: Long?,
    val reinitDate: Instant?,
    val dstReinitDate: Instant?,
    val signature: ByteString?,
    val rand2: ByteString
) {
    companion object {
        const val FLAG_FROM = 0x1
        const val FLAG_FROM_SHORT = 0x2
        const val FLAG_ONE_MESSAGE = 0x4
        const val FLAG_MULTI_MESSAGES = 0x8
        const val FLAG_ADDRESS = 0x10
        const val FLAG_PRIORITY_ADDRESS = 0x20
        const val FLAG_SEQNO = 0x40
        const val FLAG_CONFIRM_SEQNO = 0x80
        const val FLAG_RECV_ADDR_VERSION = 0x100
        const val FLAG_RECV_PRIORITY_ADDR_VERSION = 0x200
        const val FLAG_REINIT_DATE = 0x400
        const val FLAG_SIGNATURE = 0x800
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
    var addressList: AddressList? = null
    var seqno: Long = 0L
    var confirmSeqno: Long = 0L
    var receivedAddressListVersion: Int = 0
    var receivedPriorityAddressListVersion: Int = 0
    var reinitDate: Instant? = null
    var dstReinitDate: Instant? = null

    fun initRandom() {
        rand1 = Random.nextBytes(if (Random.nextBoolean()) 7 else 15)
        rand2 = Random.nextBytes(if (Random.nextBoolean()) 7 else 15)
    }

    fun toSign() {
        signature
        signature = null
    }

    fun build() {

    }

    companion object {
        private val EMPTY_ARRAY = ByteArray(0)

    }
}
