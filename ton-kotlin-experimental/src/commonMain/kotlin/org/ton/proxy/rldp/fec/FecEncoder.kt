package org.ton.proxy.rldp.fec

import org.ton.kotlin.adnl.fec.FecType

interface FecEncoder {
    val fecType: FecType

    fun encode(seqno: Int): ByteArray = encode(seqno, ByteArray(fecType.symbol_size))

    fun encode(seqno: Int, output: ByteArray): ByteArray
}
