import org.ton.kotlin.crypto.crc16
import kotlin.test.Test
import kotlin.test.assertEquals

class Crc16Test {
    @Test
    fun `sanity tests`() {
        assertEquals(
            0x0000,
            crc16("")
        )
        assertEquals(
            0x31C3,
            crc16("123456789")
        )
        assertEquals(
            0x9DD6,
            crc16("abc")
        )
        assertEquals(
            0x3994,
            crc16("ABC")
        )
        assertEquals(
            0x21E3,
            crc16("This is a string")
        )
    }
}
