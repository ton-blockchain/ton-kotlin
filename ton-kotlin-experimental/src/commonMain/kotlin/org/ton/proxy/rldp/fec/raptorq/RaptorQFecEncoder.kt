package org.ton.proxy.rldp.fec.raptorq

import org.ton.kotlin.adnl.fec.FecRaptorQ
import org.ton.proxy.rldp.fec.FecEncoder
import org.ton.proxy.rldp.fec.raptorq.math.MatrixGF256

class RaptorQFecEncoder(
    dataSize: Int,
    symbolSize: Int,
    internal val params: Params,
    private val symbols: Array<Symbol>,
    private val relaxed: MatrixGF256 = Solver(params, symbols),
) : FecEncoder {
    constructor(data: ByteArray, symbolSize: Int = 768) : this(
        data,
        symbolSize,
        Params.of(data.size, symbolSize),
    )

    constructor(data: ByteArray, symbolSize: Int, params: Params) : this(
        data.size,
        symbolSize,
        params,
        Symbol.fromBytes(data, params.kPadded, symbolSize),
    )

    override val fecType = FecRaptorQ(
        data_size = dataSize,
        symbol_size = symbolSize,
        symbol_count = (dataSize + symbolSize - 1) / symbolSize
    )

    override fun encode(seqno: Int, output: ByteArray): ByteArray {
        if (seqno < params.k) {
            symbols[seqno].data.copyInto(output)
        } else {
            params.genSymbol(relaxed, fecType.symbol_size, seqno + params.kPadded - params.k).copyInto(output)
        }
        return output
    }
}
