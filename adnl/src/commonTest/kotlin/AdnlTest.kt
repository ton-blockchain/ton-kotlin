import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import org.ton.kotlin.adnl.AdnlIdFull
import org.ton.kotlin.adnl.AdnlPacket
import org.ton.kotlin.adnl.AdnlPacketBuilder
import org.ton.kotlin.adnl.message.AdnlMessage
import org.ton.kotlin.crypto.PrivateKeyEd25519
import org.ton.kotlin.tl.TL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private val PACKET = buildString {
    append("89cd42d1")                                                            //   -- TL ID adnl.packetContents
    append("0f 4e0e7dd6d0c5646c204573bc47e567")                                   //   -- rand1, 15 (0f) random bytes
    append("d90d0000")                                                            //   -- flags (0x0dd9) -> 0b0000110111011001
    //   -- from (present because flag's zero bit = 1)
    append("c6b41348")                                                            //      -- TL ID pub.ed25519
    append("   afc46336dd352049b366c7fd3fc1b143a518f0d02d9faef896cb0155488915d6") //      -- key:int256
    //   -- messages (present because flag's third bit = 1)
    append("02000000")                                                            //      -- vector adnl.Message, size = 2 message
    append("   bbc373e6")                                                         //         -- TL ID adnl.message.createChannel
    append("   d59d8e3991be20b54dde8b78b3af18b379a62fa30e64af361c75452f6af019d7") //         -- key
    append("   555c8763")                                                         //         -- date (date of creation)

    append("   7af98bb4")                                                         //         -- TL ID adnl.message.query
    append("   d7be82afbc80516ebca39784b8e2209886a69601251571444514b7f17fcd8875") //         -- query_id
    append("   04 ed4879a9 000000")                                               //         -- query (bytes size 4, padding 3)
    //   -- address (present because flag's fourth bit = 1), without TL ID since it is specified explicitly
    append("00000000")                                                            //      -- addrs (empty vector, because we are in client mode and do not have an address on wiretap)
    append("555c8763")                                                            //      -- version (usually initialization date)
    append("555c8763")                                                            //      -- reinit_date (usually initialization date)
    append("00000000")                                                            //      -- priority
    append("00000000")                                                            //      -- expire_at

    append("0100000000000000")                                                    //   -- seqno (present because flag's sixth bit = 1)
    append("0000000000000000")                                                    //   -- confirm_seqno (present because flag's seventh bit = 1)
    append("555c8763")                                                            //   -- recv_addr_list_version (present because flag's eighth bit = 1, usually initialization date)
    append("555c8763")                                                            //   -- reinit_date (present because flag's tenth bit = 1, usually initialization date)
    append("00000000")                                                            //   -- dst_reinit_date (present because flag's tenth bit = 1)
    append("40 b453fbcbd8e884586b464290fe07475ee0da9df0b8d191e41e44f8f42a63a710") //   -- signature (present because flag's eleventh bit = 1), (bytes size 64, padding 3)
    append("341eefe8ffdc56de73db50a25989816dda17a4ac6c2f72f49804a97ff41df502")    //   --
    append("   000000")                                                           //   --
    append("0f 2b6a8c0509f85da9f3c7e11c86ba22")                                   //   -- rand2, 15 (0f) random bytes
}.replace(" ", "")

class AdnlTest {
    @OptIn(ExperimentalTime::class)
    @Test
    fun testAdnl() {
        val packet = AdnlPacketBuilder().apply {
            initRandom()
            from = AdnlIdFull(PrivateKeyEd25519.random().publicKey())
            messages.add(
                AdnlMessage.CreateChannel(
                    key = PrivateKeyEd25519.random().publicKey(),
                    date = Clock.System.now().epochSeconds.toInt()
                )
            )
        }.build()
        val r = serialize(packet, 10000)
        println(r.toHexString(HexFormat {
            bytes {
                bytesPerLine = 16
                bytesPerGroup = 4
                groupSeparator = " | " // vertical bar
                byteSeparator = "" // one space
                bytePrefix = ""
                byteSuffix = "" // empty string
            }
        }))
        println(deserialize(r, 10000))
    }

    fun serialize(packet: AdnlPacket, iterations: Int = 1): ByteArray {
        var r: ByteArray = TL.Boxed.encodeToByteArray(packet)
        for (i in 0 until iterations - 1) {
            r = TL.Boxed.encodeToByteArray(packet)
        }
        return r
    }

    fun deserialize(packet: ByteArray, iterations: Int = 1): AdnlPacket {
        var r = TL.Boxed.decodeFromByteArray<AdnlPacket>(packet)
        for (i in 0 until iterations - 1) {
            r = TL.Boxed.decodeFromByteArray<AdnlPacket>(packet)
        }
        return r
    }

    @Test
    fun docPacketTest() {
        println(PACKET)
        val packet = TL.Boxed.decodeFromByteArray<AdnlPacket>(PACKET.hexToByteArray())
        println(packet)
        val encoded = TL.Boxed.encodeToByteArray(packet)
        assertEquals(PACKET, encoded.toHexString())
    }
}
