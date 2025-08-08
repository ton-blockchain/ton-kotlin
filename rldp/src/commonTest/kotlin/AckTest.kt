import org.ton.kotlin.rldp.congestion.Ack
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AckTest {
    @Test
    fun ackTest() {
        val ack = Ack()
        assertTrue(ack.onReceivedPacket(5))
        assertTrue(!ack.onReceivedPacket(5))
        assertEquals(5, ack.maxSeqno)
        assertEquals(1, ack.receivedCount)
        assertEquals(1, ack.receivedMask)

        assertTrue(ack.onReceivedPacket(3))
        assertTrue(!ack.onReceivedPacket(3))
        assertEquals(5, ack.maxSeqno)
        assertEquals(2, ack.receivedCount)
        assertEquals(5, ack.receivedMask)

        assertTrue(ack.onReceivedPacket(7))
        assertTrue(!ack.onReceivedPacket(7))
        assertEquals(7, ack.maxSeqno)
        assertEquals(3, ack.receivedCount)
        assertEquals(21, ack.receivedMask)

        assertTrue(ack.onReceivedPacket(100))
        assertTrue(!ack.onReceivedPacket(100))
        assertTrue(!ack.onReceivedPacket(8))
        assertTrue(!ack.onReceivedPacket(7))
        assertEquals(4, ack.receivedCount)
        assertEquals(1, ack.receivedMask)
    }
}
