package org.ton.kotlin.api.adnl

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.api.SignedTlObject
import org.ton.kotlin.api.adnl.message.AdnlMessage
import org.ton.kotlin.api.pk.PrivateKey
import org.ton.kotlin.api.pub.PublicKey
import org.ton.kotlin.tl.*
import kotlin.jvm.JvmName
import kotlin.random.Random

// total packet length:
//   for full packet:
//     32 (dst) + 64 (encryption overhead) + 4 (magic) + 36 (pubkey) + 4 + M (sum of messages) +
//              + A1 + A2 + 8 + 8 + 4 + 4 + 4 + 4 + 68 (signature) + 16 (r1) + 16 (r2) =
//              = 272 + M + A1 + A2
//   for channel:
//     32 (channel id) + 32 (encryption overhead) + 4 (magic) + 4 + M (sum of messages) +
//              + A1 + A2 + 8 + 8 + 4 + 4 + 16(r1) + 16(r2) = 128 + M + A1 + A2
@Serializable
@SerialName("adnl.packetContents")
public data class AdnlPacketContents(
    @get:JvmName("rand1")
    @Serializable(ByteStringBase64Serializer::class)
    val rand1: ByteString,

    @get:JvmName("flags")
    val flags: Int,

    @get:JvmName("from")
    val from: PublicKey?,

    @SerialName("from_short")
    @get:JvmName("fromShort")
    val fromShort: AdnlIdShort?,

    @get:JvmName("message")
    val message: AdnlMessage?,

    @get:JvmName("messages")
    val messages: List<AdnlMessage>?,

    @get:JvmName("address")
    val address: AdnlAddressList?,

    @SerialName("priority_address")
    @get:JvmName("priorityAddress")
    val priorityAddress: AdnlAddressList?,

    @get:JvmName("seqno")
    val seqno: Long?,

    @SerialName("confirm_seqno")
    @get:JvmName("confirmSeqno")
    val confirmSeqno: Long?,

    @SerialName("recv_addr_list_version")
    @get:JvmName("recvAddrListVersion")
    val recvAddrListVersion: Int?,

    @SerialName("recv_priority_addr_list_version")
    @get:JvmName("recvPriorityAddrListVersion")
    val recvPriorityAddrListVersion: Int?,

    @SerialName("reinit_date")
    @get:JvmName("reinitDate")
    val reinitDate: Int?,

    @SerialName("dst_reinit_date")
    @get:JvmName("dstReinitDate")
    val dstReinitDate: Int?,

    @get:JvmName("signature")
    @Serializable(ByteStringBase64Serializer::class)
    override val signature: ByteString?,

    @get:JvmName("rand2")
    @Serializable(ByteStringBase64Serializer::class)
    val rand2: ByteString
) : SignedTlObject<AdnlPacketContents> {
    public constructor(
        rand1: ByteString = ByteString(*Random.Default.nextBytes(if (Random.nextBoolean()) 7 else 15)),
        from: PublicKey? = null,
        from_short: AdnlIdShort? = null,
        message: AdnlMessage? = null,
        messages: List<AdnlMessage>? = null,
        address: AdnlAddressList? = null,
        priority_address: AdnlAddressList? = null,
        seqno: Long? = null,
        confirm_seqno: Long? = null,
        recv_addr_list_version: Int? = null,
        recv_priority_addr_list_version: Int? = null,
        reinit_date: Int? = null,
        dst_reinit_date: Int? = null,
        signature: ByteString? = null,
        rand2: ByteString = ByteString(*Random.Default.nextBytes(if (Random.nextBoolean()) 7 else 15))
    ) : this(
        rand1 = rand1,
        flags = flags(
            from != null,
            from_short != null,
            message != null,
            messages != null,
            address != null,
            priority_address != null,
            seqno != null,
            confirm_seqno != null,
            recv_addr_list_version != null,
            recv_priority_addr_list_version != null,
            reinit_date != null,
            signature != null
        ),
        from,
        from_short,
        message,
        messages,
        address,
        priority_address,
        seqno,
        confirm_seqno,
        recv_addr_list_version,
        recv_priority_addr_list_version,
        reinit_date,
        dst_reinit_date,
        signature,
        rand2
    )

    init {
        if (message != null && messages != null) {
            throw IllegalArgumentException("both fields `message` and `messages` set")
        }
        if (from != null && fromShort != null && from.toAdnlIdShort() != fromShort) {
            throw IllegalArgumentException("`from` and `from_short` mismatch")
        }
//        if (address != null && address.addrs.isEmpty()) {
//            throw IllegalArgumentException("`address` contains empty list")
//        }
        if (priorityAddress != null && priorityAddress.addrs.isEmpty()) {
            throw IllegalArgumentException("`priority_address` contains empty list")
        }
    }

    public fun collectMessages(): List<AdnlMessage> = message?.let { listOf(it) } ?: messages ?: emptyList()

    override fun signed(privateKey: PrivateKey): AdnlPacketContents {
        val encoded = tlCodec().encodeToByteArray(
            AdnlPacketContents(
                rand1 = rand1,
                from = from,
                from_short = fromShort,
                message = message,
                messages = messages,
                address = address,
                priority_address = priorityAddress,
                seqno = seqno,
                confirm_seqno = confirmSeqno,
                recv_addr_list_version = recvAddrListVersion,
                recv_priority_addr_list_version = recvPriorityAddrListVersion,
                reinit_date = reinitDate,
                dst_reinit_date = dstReinitDate,
                signature = null,
                rand2 = rand2
            )
        )
        val signature = privateKey.sign(encoded)
        return AdnlPacketContents(
            rand1 = rand1,
            from = from,
            from_short = fromShort,
            message = message,
            messages = messages,
            address = address,
            priority_address = priorityAddress,
            seqno = seqno,
            confirm_seqno = confirmSeqno,
            recv_addr_list_version = recvAddrListVersion,
            recv_priority_addr_list_version = recvPriorityAddrListVersion,
            reinit_date = reinitDate,
            dst_reinit_date = dstReinitDate,
            signature = ByteString(*signature),
            rand2 = rand2
        )
    }

    override fun verify(publicKey: PublicKey): Boolean {
        val encoded = tlCodec().encodeToByteArray(
            AdnlPacketContents(
                rand1 = rand1,
                from = from,
                from_short = fromShort,
                message = message,
                messages = messages,
                address = address,
                priority_address = priorityAddress,
                seqno = seqno,
                confirm_seqno = confirmSeqno,
                recv_addr_list_version = recvAddrListVersion,
                recv_priority_addr_list_version = recvPriorityAddrListVersion,
                reinit_date = reinitDate,
                dst_reinit_date = dstReinitDate,
                signature = null,
                rand2 = rand2
            )
        )
        return publicKey.verify(encoded, signature?.toByteArray())
    }

    override fun tlCodec(): TlCodec<AdnlPacketContents> = AdnlPacketContentsTlConstructor


    public companion object : TlCodec<AdnlPacketContents> by AdnlPacketContentsTlConstructor {
        public fun flags(
            from: Boolean = false,
            from_short: Boolean = false,
            message: Boolean = false,
            messages: Boolean = false,
            address: Boolean = false,
            priority_address: Boolean = false,
            seqno: Boolean = false,
            confirm_seqno: Boolean = false,
            recv_addr_list_version: Boolean = false,
            recv_priority_addr_list_version: Boolean = false,
            reinit_date: Boolean = false,
            signature: Boolean = false
        ): Int {
            var flags = 0
            if (from) flags = flags or 1
            if (from_short) flags = flags or 2
            if (message) flags = flags or 4
            if (messages) flags = flags or 8
            if (address) flags = flags or 16
            if (priority_address) flags = flags or 32
            if (seqno) flags = flags or 64
            if (confirm_seqno) flags = flags or 128
            if (recv_addr_list_version) flags = flags or 256
            if (recv_priority_addr_list_version) flags = flags or 512
            if (reinit_date) flags = flags or 1024
            if (signature) flags = flags or 2048
            return flags
        }
    }
}

//public class AdnlPacketContentsBuilder() {
//    public var flags: Int = 0
//    public var from: PublicKey? = null
//    public var fromShort: AdnlIdShort? = null
//    public var rand1: ByteString = Random.Default.nextBytes(if (Random.nextBoolean()) 7 else 15).asByteString()
//    public var rand2: ByteString = Random.Default.nextBytes(if (Random.nextBoolean()) 7 else 15).asByteString()
//    public var messages: MutableList<AdnlMessage> = ArrayList()
//    public var addresses: AdnlAddressList = AdnlAddressList()
//
//    fun build() {
//        AdnlPacketContents(
//            rand1 = rand1,
//            flags = 0,
//            from = from,
//            fromShort = fromShort,
//            message = if (messages.size <= 1) messages.firstOrNull() else null,
//            messages = if (messages.size > 1) messages else null,
//            address =addresses,
//            priorityAddress = AdnlAddressList(),
//            seqno = 0,
//            confirmSeqno = 0,
//            recvAddrListVersion = 0,
//        )
//    }
//}

private object AdnlPacketContentsTlConstructor : TlConstructor<AdnlPacketContents>(
    schema = "adnl.packetContents" +
            " rand1:bytes" +
            " flags:#" +
            " from:flags.0?PublicKey" +
            " from_short:flags.1?adnl.id.short" +
            " message:flags.2?adnl.Message" +
            " messages:flags.3?(vector adnl.Message)" +
            " address:flags.4?adnl.addressList" +
            " priority_address:flags.5?adnl.addressList" +
            " seqno:flags.6?long" +
            " confirm_seqno:flags.7?long" +
            " recv_addr_list_version:flags.8?int" +
            " recv_priority_addr_list_version:flags.9?int" +
            " reinit_date:flags.10?int" +
            " dst_reinit_date:flags.10?int" +
            " signature:flags.11?bytes" +
            " rand2:bytes" +
            " = adnl.PacketContents",
    id = -784151159
) {
    override fun decode(reader: TlReader): AdnlPacketContents {
        val rand1 = reader.readByteString()
        val flags = reader.readInt()
        val from = reader.readNullable(flags, 0) { read(PublicKey) }
        val from_short = reader.readNullable(flags, 1) { read(AdnlIdShort) }
        val message = reader.readNullable(flags, 2) { read(AdnlMessage) }
        val messages = reader.readNullable(flags, 3) { readVector { read(AdnlMessage) } }
        val address = reader.readNullable(flags, 4) { read(AdnlAddressList) }
        val priority_address = reader.readNullable(flags, 5) { read(AdnlAddressList) }
        val seqno = reader.readNullable(flags, 6) { readLong() }
        val confirm_seqno = reader.readNullable(flags, 7) { readLong() }
        val recv_addr_list_version = reader.readNullable(flags, 8) { readInt() }
        val recv_priority_addr_list_version = reader.readNullable(flags, 9) { readInt() }
        val reinit_date = reader.readNullable(flags, 10) { readInt() }
        val dst_reinit_date = reader.readNullable(flags, 10) { readInt() }
        val signature = reader.readNullable(flags, 11) { readByteString() }
        val rand2 = reader.readByteString()

        return AdnlPacketContents(
            rand1,
            flags,
            from,
            from_short,
            message,
            messages,
            address,
            priority_address,
            seqno,
            confirm_seqno,
            recv_addr_list_version,
            recv_priority_addr_list_version,
            reinit_date,
            dst_reinit_date,
            signature,
            rand2
        )
    }

    override fun encode(writer: TlWriter, value: AdnlPacketContents) {
        writer.writeBytes(value.rand1)
        writer.writeInt(value.flags)
        val flags = value.flags
        writer.writeNullable(flags, 0, value.from) { write(PublicKey, it) }
        writer.writeNullable(flags, 1, value.fromShort) { write(AdnlIdShort, it) }
        writer.writeNullable(flags, 2, value.message) { write(AdnlMessage, it) }
        writer.writeNullable(flags, 3, value.messages) { list ->
            writeVector(list) { element ->
                write(AdnlMessage, element)
            }
        }
        writer.writeNullable(flags, 4, value.address) { write(AdnlAddressList, it) }
        writer.writeNullable(flags, 5, value.priorityAddress) { write(AdnlAddressList, it) }
        writer.writeNullable(flags, 6, value.seqno) { writeLong(it) }
        writer.writeNullable(flags, 7, value.confirmSeqno) { writeLong(it) }
        writer.writeNullable(flags, 8, value.recvAddrListVersion) { writeInt(it) }
        writer.writeNullable(flags, 9, value.recvPriorityAddrListVersion) { writeInt(it) }
        writer.writeNullable(flags, 10, value.reinitDate) { writeInt(it) }
        writer.writeNullable(flags, 10, value.dstReinitDate) { writeInt(it) }
        writer.writeNullable(flags, 11, value.signature) { writeBytes(it) }
        writer.writeBytes(value.rand2)
    }
}
