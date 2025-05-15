package org.ton.experimental

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.*
import org.ton.kotlin.adnl.fec.FecRaptorQ
import org.ton.kotlin.adnl.rldp.RldpMessagePart
import org.ton.kotlin.adnl.rldp.RldpMessagePartData
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.crypto.encodeHex
import org.ton.proxy.rldp.RldpInputTransfer
import org.ton.proxy.rldp.RldpOutputTransfer
import kotlin.test.Test
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

class RldpTest {
    @Test
    fun test() = runBlocking {
        val output = newSingleThreadContext("RLDP output")
        val input = newSingleThreadContext("RLDP input")


        repeat(10) {
            val originalData = ByteArray(1024 * 1024 * 100)

            val outputTransfer = RldpOutputTransfer.of(originalData)
            val inputTransfer = RldpInputTransfer.of(outputTransfer.id)

            launch(output) {
                outputTransfer.transferPackets().collect { part ->
                    launch {
                        inputTransfer.receiveRldpMessagePart(part)
                    }
                }
            }

            launch(input) {
                inputTransfer.transferPackets().collect { part ->
                    launch {
                        outputTransfer.receiveRldpMessagePart(part)
                    }
                }
            }

            val (value, time) = measureTimedValue {
                inputTransfer.byteChannel.discard()
            }
            // speed in megabytes per second
            val speed = value.toDouble() * 8.0 / time.toDouble(DurationUnit.MILLISECONDS) * 1000 / 1024 / 1024
            println("speed: $speed MB/s")
        }
    }

//    @Test
//    fun raptorQFec() = runBlocking {
//        val data = Random.nextBytes(1024 * 20)
//        val encoder = RaptorQFecEncoder(data)
//        println(encoder.fecType)
//        val symbols = Array(encoder.fecType.symbol_count) {
//            val array = ByteArray(encoder.fecType.symbol_size)
//            val seqno = encoder.encode(it, array)
//            seqno to array
//        }
//    }

    @Test
    fun testSerializtion() {
        val packet = RldpMessagePartData(
            transfer_id = BitString(ByteArray(32)),
            fec_type = FecRaptorQ(
                data_size = 1024,
                symbol_size = 768,
                symbol_count = 10,
            ),
            part = 0,
            total_size = 1000,
            seqno = 12,
            data = ByteArray(768),
        )
        val bytes = RldpMessagePart.encode(packet)
        println(packet)
        println(bytes.encodeHex())
        val decoded = RldpMessagePart.decode(bytes)
        println(decoded)
    }
}
