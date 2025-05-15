package org.ton.kotlin.hashmap

import org.ton.kotlin.bitstring.BitString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class HashMapLabelTest {
    @Test
    fun `test hml_same construction`() {
        assertNull(HmlSame.of(BitString.binary("0001")))
        assertNull(HmlSame.of(BitString.binary("1110")))
        assertNull(HmlSame.of(BitString.binary("1110111")))
        assertNull(HmlSame.of(BitString.binary("0001000")))

        assertEquals(HmlSame(true, 1), HmlSame.of(BitString.binary("1")))
        assertEquals(HmlSame(true, 2), HmlSame.of(BitString.binary("11")))
        assertEquals(HmlSame(true, 3), HmlSame.of(BitString.binary("111")))
        assertEquals(HmlSame(false, 1), HmlSame.of(BitString.binary("0")))
        assertEquals(HmlSame(false, 2), HmlSame.of(BitString.binary("00")))
        assertEquals(HmlSame(false, 3), HmlSame.of(BitString.binary("000")))
    }
}
