package org.ton.proxy.rldp.fec.raptorq

import org.ton.kotlin.adnl.fec.FecType
import org.ton.proxy.rldp.fec.FecDecoder
import kotlin.math.min

class RaptorQFecDecoder(
    override val fecType: FecType
) : FecDecoder {
    private val parameters = Params.of(fecType.data_size, fecType.symbol_size)
    private val symbols = HashMap<Int, ByteArray>()
    private var fastSymbols = 0
    private var slowSymbols = 0
    override val isComplete: Boolean
        get() = fastSymbols + slowSymbols >= parameters.k

    override fun decode(output: ByteArray): Boolean {
        if (!isComplete) return false
        if (fastSymbols < parameters.k) {
            val toRelax = ArrayList<Symbol>()

            // add known symbols
            symbols.forEach { (seqno, data) ->
                val newSeqno = if (seqno >= parameters.k) {
                    seqno + parameters.kPadded - parameters.k
                } else seqno
                toRelax.add(Symbol(newSeqno, data))
            }

            // add padding empty symbols
            val zeroData = ByteArray(fecType.symbol_size)
            for (i in toRelax.size until parameters.kPadded) {
                toRelax.add(Symbol(i, zeroData))
            }

            // try recover fast symbols from slow
            val relaxed = Solver(parameters, toRelax.toTypedArray())
            for (i in 0 until parameters.k) {
                if (symbols.containsKey(i)) continue
                symbols[i] = parameters.genSymbol(relaxed, fecType.symbol_size, i)
            }
        }

        for (i in 0 until parameters.k) {
            symbols[i]!!.copyInto(
                output,
                i * fecType.symbol_size,
                0,
                min(output.size - i * fecType.symbol_size, fecType.symbol_size)
            )
        }
        return true
    }

    override fun addSymbol(seqno: Int, data: ByteArray): Boolean {
        require(data.size == fecType.symbol_size) {
            "Invalid symbol size, expected: ${fecType.symbol_size}, actual: ${data.size}"
        }

        if (seqno < parameters.k) {
            if (!symbols.containsKey(seqno)) {
                fastSymbols++
                symbols[seqno] = data.copyOf()
            }
        } else {
            if (fastSymbols + slowSymbols >= parameters.k + 10) return true
            if (!symbols.containsKey(seqno)) {
                slowSymbols++
                symbols[seqno] = data.copyOf()
            }
        }

        return isComplete
    }
}
