package org.ton.proxy.rldp.fec

import org.ton.kotlin.adnl.fec.FecType

interface FecDecoder {
    val fecType: FecType
    val isComplete: Boolean

    fun addSymbol(seqno: Int, data: ByteArray): Boolean
    fun decode(output: ByteArray): Boolean
    fun decode(): ByteArray? = if (isComplete) {
        val output = ByteArray(fecType.data_size)
        decode(output)
        output
    } else null
}
