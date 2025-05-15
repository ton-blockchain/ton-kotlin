package org.ton.experimental

import org.ton.kotlin.crypto.encodeHex
import org.ton.proxy.rldp.fec.raptorq.RaptorQFecDecoder
import org.ton.proxy.rldp.fec.raptorq.RaptorQFecEncoder
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals

class RaptorQTest {
    @Test
    fun testEncode() {
        val str =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
        val data = str.toByteArray()
        val encoder = RaptorQFecEncoder(data, 20)

        val sx = encoder.encode(1000)
        println(sx.encodeHex())
    }

    @Test
    fun testDecode() {
        val str =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
        val data = str.toByteArray()

        val encoder = RaptorQFecEncoder(data, 20)
        val decoder = RaptorQFecDecoder(encoder.fecType)

        for (i in 0 until 30) {
            val sx = encoder.encode(i + 10000)
            decoder.addSymbol(i + 10000, sx)
        }

        val decodedData = decoder.decode()!!

        println(data.encodeHex())
        println(decodedData.encodeHex())
    }

    @Test
    fun testFuzz() = repeat(100) {
        val str = Random.nextBytes(4096)
        val symbolSize = Random.nextInt(1, 10) * 10
        val encoder = RaptorQFecEncoder(str, symbolSize)
        val decoder = RaptorQFecDecoder(encoder.fecType)

        decoder.addSymbol(2, encoder.encode(2))

        for (i in 0 until encoder.params.k) {
            val sx = encoder.encode(i + 10000)
            decoder.addSymbol(i + 10000, sx)
        }

        decoder.decode()
    }

    @Test
    fun testDataTransfer() {
        val originalData = Random.nextBytes(1_000_000)
        val encoder = RaptorQFecEncoder(originalData)
        val decoder = RaptorQFecDecoder(encoder.fecType)

        val lossChance = 0.1
        var seqno = 0
        var transferredData: ByteArray? = null
        while (transferredData == null) {
            encoder.encode(seqno).let { sx ->
                if (Random.nextDouble() > lossChance) {
                    decoder.addSymbol(seqno, sx)
                }
            }
            transferredData = decoder.decode()
            seqno++
        }

        assertContentEquals(originalData, transferredData)
    }
}
