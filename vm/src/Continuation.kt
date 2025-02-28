package org.ton.kotlin.tvm

import org.ton.cell.CellSlice

public sealed interface TvmContinuation {
    public data class Ordinary(
        val code: CellSlice,
    ) : TvmContinuation

    public companion object {
        public fun ordinary(code: CellSlice): TvmContinuation = Ordinary(code)
    }
}

//public class ControlData(
//    public val nargs: Int? = null,
//    public val stack: Stack? = null,
//    val stackDepth: Int = 0,
//    val save: ControlRegs = ControlRegs(),
//    val cp: Int = 0
//)
//
//public class ControlRegs(
//    public val c: Array<TvmContinuation?> = arrayOfNulls(4),
//    public val d: Array<Cell?> = arrayOfNulls(2),
//    public val c7: List<Any?> = emptyList()
//)

/*



class TvmContinuation private constructor(
    val tag: Byte,
    val data: ControlData = ControlData(),
    val code: CellSlice = CellSlice(BitString.empty()),
    val value: Long = 0,
    val condition: TvmContinuation? = null,
    val body: TvmContinuation? = null,
    val next: TvmContinuation? = null,
) {
    companion object {
        val ORD_TAG = 0b00.toByte()
        val ORD_EXT_TAG = 0b01.toByte()
        val QUIT_TAG = 0b1000.toByte()
        val QUIT_EXC_TAG = 0b1001.toByte()
        val REPEAT_TAG = 0b10100.toByte()
        val UNTIL_TAG = 0b110000.toByte()
        val AGAIN_TAG = 0b110001.toByte()
        val WHILE_COND_TAG = 0b110010.toByte()
        val WHILE_BODY_TAG = 0b110011.toByte()
        val PUSHINT_TAG = 0b1111.toByte()

        fun ordinary() =
            TvmContinuation(tag = ORD_TAG)

        fun ordinary(code: CellSlice, cp: Int) =
            TvmContinuation(tag = ORD_TAG, code = code, data = ControlData(cp = cp))

        fun ordinary(code: CellSlice, cp: Int, stack: List<Any>, nargs: Int = -1) =
            TvmContinuation(
                tag = ORD_TAG,
                code = code,
                data = ControlData(
                    cp = cp,
                    stackDepth = stack.size,
                    stack = Stack(stack),
                    nargs = nargs,
                )
            )
    }
}
 */
