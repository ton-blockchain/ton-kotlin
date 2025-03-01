package org.ton.kotlin.tvm

import org.ton.bigint.*
import org.ton.cell.*
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.tvm.OpCodes.ADD
import org.ton.kotlin.tvm.OpCodes.ADDCONST
import org.ton.kotlin.tvm.OpCodes.BLKDROP2
import org.ton.kotlin.tvm.OpCodes.BLKSWAP
import org.ton.kotlin.tvm.OpCodes.BLKSWX
import org.ton.kotlin.tvm.OpCodes.BLK_SUBSET
import org.ton.kotlin.tvm.OpCodes.CHKDEPTH
import org.ton.kotlin.tvm.OpCodes.DEC
import org.ton.kotlin.tvm.OpCodes.DEPTH
import org.ton.kotlin.tvm.OpCodes.DROP
import org.ton.kotlin.tvm.OpCodes.DROP2
import org.ton.kotlin.tvm.OpCodes.DROPX
import org.ton.kotlin.tvm.OpCodes.DUP
import org.ton.kotlin.tvm.OpCodes.DUP2
import org.ton.kotlin.tvm.OpCodes.INC
import org.ton.kotlin.tvm.OpCodes.MUL
import org.ton.kotlin.tvm.OpCodes.MULCONST
import org.ton.kotlin.tvm.OpCodes.NEGATE
import org.ton.kotlin.tvm.OpCodes.NIP
import org.ton.kotlin.tvm.OpCodes.NOP
import org.ton.kotlin.tvm.OpCodes.ONLYTOPX
import org.ton.kotlin.tvm.OpCodes.ONLYX
import org.ton.kotlin.tvm.OpCodes.OVER
import org.ton.kotlin.tvm.OpCodes.OVER2
import org.ton.kotlin.tvm.OpCodes.PICK
import org.ton.kotlin.tvm.OpCodes.POP_10
import org.ton.kotlin.tvm.OpCodes.POP_11
import org.ton.kotlin.tvm.OpCodes.POP_12
import org.ton.kotlin.tvm.OpCodes.POP_13
import org.ton.kotlin.tvm.OpCodes.POP_14
import org.ton.kotlin.tvm.OpCodes.POP_15
import org.ton.kotlin.tvm.OpCodes.POP_2
import org.ton.kotlin.tvm.OpCodes.POP_3
import org.ton.kotlin.tvm.OpCodes.POP_4
import org.ton.kotlin.tvm.OpCodes.POP_5
import org.ton.kotlin.tvm.OpCodes.POP_6
import org.ton.kotlin.tvm.OpCodes.POP_7
import org.ton.kotlin.tvm.OpCodes.POP_8
import org.ton.kotlin.tvm.OpCodes.POP_9
import org.ton.kotlin.tvm.OpCodes.POP_L
import org.ton.kotlin.tvm.OpCodes.PUSH2
import org.ton.kotlin.tvm.OpCodes.PUSHCONT_0
import org.ton.kotlin.tvm.OpCodes.PUSHCONT_1
import org.ton.kotlin.tvm.OpCodes.PUSHCONT_10
import org.ton.kotlin.tvm.OpCodes.PUSHCONT_11
import org.ton.kotlin.tvm.OpCodes.PUSHCONT_12
import org.ton.kotlin.tvm.OpCodes.PUSHCONT_13
import org.ton.kotlin.tvm.OpCodes.PUSHCONT_14
import org.ton.kotlin.tvm.OpCodes.PUSHCONT_15
import org.ton.kotlin.tvm.OpCodes.PUSHCONT_2
import org.ton.kotlin.tvm.OpCodes.PUSHCONT_3
import org.ton.kotlin.tvm.OpCodes.PUSHCONT_4
import org.ton.kotlin.tvm.OpCodes.PUSHCONT_5
import org.ton.kotlin.tvm.OpCodes.PUSHCONT_6
import org.ton.kotlin.tvm.OpCodes.PUSHCONT_7
import org.ton.kotlin.tvm.OpCodes.PUSHCONT_8
import org.ton.kotlin.tvm.OpCodes.PUSHCONT_9
import org.ton.kotlin.tvm.OpCodes.PUSHCONT_REF_0_3
import org.ton.kotlin.tvm.OpCodes.PUSHCONT_REF_4
import org.ton.kotlin.tvm.OpCodes.PUSHINT_0
import org.ton.kotlin.tvm.OpCodes.PUSHINT_1
import org.ton.kotlin.tvm.OpCodes.PUSHINT_10
import org.ton.kotlin.tvm.OpCodes.PUSHINT_2
import org.ton.kotlin.tvm.OpCodes.PUSHINT_3
import org.ton.kotlin.tvm.OpCodes.PUSHINT_4
import org.ton.kotlin.tvm.OpCodes.PUSHINT_5
import org.ton.kotlin.tvm.OpCodes.PUSHINT_6
import org.ton.kotlin.tvm.OpCodes.PUSHINT_7
import org.ton.kotlin.tvm.OpCodes.PUSHINT_8
import org.ton.kotlin.tvm.OpCodes.PUSHINT_9
import org.ton.kotlin.tvm.OpCodes.PUSHINT_LONG
import org.ton.kotlin.tvm.OpCodes.PUSHINT_MINUS_1
import org.ton.kotlin.tvm.OpCodes.PUSHINT_MINUS_2
import org.ton.kotlin.tvm.OpCodes.PUSHINT_MINUS_3
import org.ton.kotlin.tvm.OpCodes.PUSHINT_MINUS_4
import org.ton.kotlin.tvm.OpCodes.PUSHINT_MINUS_5
import org.ton.kotlin.tvm.OpCodes.PUSHINT_XX
import org.ton.kotlin.tvm.OpCodes.PUSHINT_XXXX
import org.ton.kotlin.tvm.OpCodes.PUSHNEGPOW2
import org.ton.kotlin.tvm.OpCodes.PUSHPOW2
import org.ton.kotlin.tvm.OpCodes.PUSHPOW2DEC
import org.ton.kotlin.tvm.OpCodes.PUSHREF
import org.ton.kotlin.tvm.OpCodes.PUSHREFCONT
import org.ton.kotlin.tvm.OpCodes.PUSHREFSLICE
import org.ton.kotlin.tvm.OpCodes.PUSHSLICE
import org.ton.kotlin.tvm.OpCodes.PUSHSLICE_LONG
import org.ton.kotlin.tvm.OpCodes.PUSHSLICE_SHORT
import org.ton.kotlin.tvm.OpCodes.PUSH_10
import org.ton.kotlin.tvm.OpCodes.PUSH_11
import org.ton.kotlin.tvm.OpCodes.PUSH_12
import org.ton.kotlin.tvm.OpCodes.PUSH_13
import org.ton.kotlin.tvm.OpCodes.PUSH_14
import org.ton.kotlin.tvm.OpCodes.PUSH_15
import org.ton.kotlin.tvm.OpCodes.PUSH_2
import org.ton.kotlin.tvm.OpCodes.PUSH_3
import org.ton.kotlin.tvm.OpCodes.PUSH_4
import org.ton.kotlin.tvm.OpCodes.PUSH_5
import org.ton.kotlin.tvm.OpCodes.PUSH_6
import org.ton.kotlin.tvm.OpCodes.PUSH_7
import org.ton.kotlin.tvm.OpCodes.PUSH_8
import org.ton.kotlin.tvm.OpCodes.PUSH_9
import org.ton.kotlin.tvm.OpCodes.PUSH_L
import org.ton.kotlin.tvm.OpCodes.PUXC
import org.ton.kotlin.tvm.OpCodes.REVERSE
import org.ton.kotlin.tvm.OpCodes.REVX
import org.ton.kotlin.tvm.OpCodes.ROLLREV
import org.ton.kotlin.tvm.OpCodes.ROLLX
import org.ton.kotlin.tvm.OpCodes.ROT
import org.ton.kotlin.tvm.OpCodes.ROTREV
import org.ton.kotlin.tvm.OpCodes.SUB
import org.ton.kotlin.tvm.OpCodes.SUBR
import org.ton.kotlin.tvm.OpCodes.SWAP
import org.ton.kotlin.tvm.OpCodes.SWAP2
import org.ton.kotlin.tvm.OpCodes.TUCK
import org.ton.kotlin.tvm.OpCodes.XCHG
import org.ton.kotlin.tvm.OpCodes.XCHG2
import org.ton.kotlin.tvm.OpCodes.XCHG3_0
import org.ton.kotlin.tvm.OpCodes.XCHG3_1
import org.ton.kotlin.tvm.OpCodes.XCHG3_10
import org.ton.kotlin.tvm.OpCodes.XCHG3_11
import org.ton.kotlin.tvm.OpCodes.XCHG3_12
import org.ton.kotlin.tvm.OpCodes.XCHG3_13
import org.ton.kotlin.tvm.OpCodes.XCHG3_14
import org.ton.kotlin.tvm.OpCodes.XCHG3_15
import org.ton.kotlin.tvm.OpCodes.XCHG3_2
import org.ton.kotlin.tvm.OpCodes.XCHG3_3
import org.ton.kotlin.tvm.OpCodes.XCHG3_4
import org.ton.kotlin.tvm.OpCodes.XCHG3_5
import org.ton.kotlin.tvm.OpCodes.XCHG3_6
import org.ton.kotlin.tvm.OpCodes.XCHG3_7
import org.ton.kotlin.tvm.OpCodes.XCHG3_8
import org.ton.kotlin.tvm.OpCodes.XCHG3_9
import org.ton.kotlin.tvm.OpCodes.XCHGX
import org.ton.kotlin.tvm.OpCodes.XCHG_0
import org.ton.kotlin.tvm.OpCodes.XCHG_0_10
import org.ton.kotlin.tvm.OpCodes.XCHG_0_11
import org.ton.kotlin.tvm.OpCodes.XCHG_0_12
import org.ton.kotlin.tvm.OpCodes.XCHG_0_13
import org.ton.kotlin.tvm.OpCodes.XCHG_0_14
import org.ton.kotlin.tvm.OpCodes.XCHG_0_15
import org.ton.kotlin.tvm.OpCodes.XCHG_0_2
import org.ton.kotlin.tvm.OpCodes.XCHG_0_3
import org.ton.kotlin.tvm.OpCodes.XCHG_0_4
import org.ton.kotlin.tvm.OpCodes.XCHG_0_5
import org.ton.kotlin.tvm.OpCodes.XCHG_0_6
import org.ton.kotlin.tvm.OpCodes.XCHG_0_7
import org.ton.kotlin.tvm.OpCodes.XCHG_0_8
import org.ton.kotlin.tvm.OpCodes.XCHG_0_9
import org.ton.kotlin.tvm.OpCodes.XCHG_1_10
import org.ton.kotlin.tvm.OpCodes.XCHG_1_11
import org.ton.kotlin.tvm.OpCodes.XCHG_1_12
import org.ton.kotlin.tvm.OpCodes.XCHG_1_13
import org.ton.kotlin.tvm.OpCodes.XCHG_1_14
import org.ton.kotlin.tvm.OpCodes.XCHG_1_15
import org.ton.kotlin.tvm.OpCodes.XCHG_1_2
import org.ton.kotlin.tvm.OpCodes.XCHG_1_3
import org.ton.kotlin.tvm.OpCodes.XCHG_1_4
import org.ton.kotlin.tvm.OpCodes.XCHG_1_5
import org.ton.kotlin.tvm.OpCodes.XCHG_1_6
import org.ton.kotlin.tvm.OpCodes.XCHG_1_7
import org.ton.kotlin.tvm.OpCodes.XCHG_1_8
import org.ton.kotlin.tvm.OpCodes.XCHG_1_9
import org.ton.kotlin.tvm.OpCodes.XCHG_PUSH_SUBSET
import org.ton.kotlin.tvm.OpCodes.XCPU
import org.ton.kotlin.tvm.exception.IntegerOverflowTvmException
import org.ton.kotlin.tvm.exception.InvalidOpcodeException
import org.ton.kotlin.tvm.exception.StackUnderflowException
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public class Tvm(
    public val cellContext: CellContext = CellContext.EMPTY,
    public val gasCalculator: GasCalculator = DefaultGasCalculator
) : CellContext {
    private val loadedCells = mutableMapOf<Cell, DataCell>()
    private var baseGas = Long.MAX_VALUE
    private var remainingGas = baseGas
    public val usedGas: Long get() = baseGas - remainingGas

    override fun loadCell(cell: Cell): DataCell {
        var loadedCell = loadedCells[cell]
        if (loadedCell == null) {
            loadedCell = cellContext.loadCell(cell)
            loadedCells[cell] = loadedCell
            consumeGas(gasCalculator.calculateCellLoad(cell))
        } else {
            consumeGas(gasCalculator.calculateCellReload(cell))
        }
        return loadedCell
    }

    override fun finalizeCell(builder: CellBuilder): Cell {
        val cell = cellContext.finalizeCell(builder)
        consumeGas(gasCalculator.calculateCellCreate(cell))
        return cell
    }

    public fun execute(stack: Stack, code: Cell) {
        executeCp0(stack, code.beginParse())
    }

    public fun execute(stack: Stack, code: CellSlice) {
        executeCp0(stack, code)
    }

    private fun consumeGas(gas: Long) {
        remainingGas -= gas
    }

    private fun executeCp0(
        stack: Stack,
        code: CellSlice,
    ) {
        var steps: Int = 0
        loop@ while (code.remainingBits > 0) {
            println("steps: ${steps++}, stack: ${stack}")
            val opcode = code.loadUInt(8).toInt()
            when (opcode) {
                NOP -> {
                    consumeGas(gasCalculator.calculateOperationCost(8))

                    logOpcode { "execute NOP" }
                }

                SWAP -> {
                    consumeGas(gasCalculator.calculateOperationCost(8))

                    logOpcode { "execute SWAP" }
                    stack.swap(0, 1)
                }

                // (x ... y ... - y ... x ...)
                XCHG_0_2, XCHG_0_3, XCHG_0_4, XCHG_0_5, XCHG_0_6, XCHG_0_7, XCHG_0_8, XCHG_0_9,
                XCHG_0_10, XCHG_0_11, XCHG_0_12, XCHG_0_13, XCHG_0_14, XCHG_0_15 -> {
                    consumeGas(gasCalculator.calculateOperationCost(8))

                    val arg = opcode
                    logOpcode { "execute XCHG0 s$arg" }
                    stack.swap(0, arg)
                }

                // (x ... y ... - y ... x ...)
                XCHG -> {
                    consumeGas(gasCalculator.calculateOperationCost(16))

                    val arg = code.loadUInt(8).toInt()
                    val i = arg ushr 4
                    val j = arg and 0xF
                    logOpcode { "execute XCHG s$i,s$j" }
                    stack.swap(i, j)
                }

                XCHG_0 -> {
                    consumeGas(gasCalculator.calculateOperationCost(16))

                    val arg = code.loadUInt(8).toInt()
                    logOpcode { "execute XCHG0 s$arg" }
                    stack.swap(0, arg)
                }

                XCHG_1_2, XCHG_1_3, XCHG_1_4, XCHG_1_5, XCHG_1_6, XCHG_1_7, XCHG_1_8, XCHG_1_9,
                XCHG_1_10, XCHG_1_11, XCHG_1_12, XCHG_1_13, XCHG_1_14, XCHG_1_15 -> {
                    consumeGas(gasCalculator.calculateOperationCost(8))

                    val arg = opcode and 0xF
                    logOpcode { "execute XCHG s1,s$arg" }
                    stack.swap(0, arg)
                }

                // (x ... - x ... x)
                DUP, OVER, PUSH_2, PUSH_3, PUSH_4, PUSH_5, PUSH_6, PUSH_7, PUSH_8, PUSH_9,
                PUSH_10, PUSH_11, PUSH_12, PUSH_13, PUSH_14, PUSH_15 -> {
                    consumeGas(gasCalculator.calculateOperationCost(8))

                    val arg = opcode and 0xF
                    logOpcode {
                        when (arg) {
                            0 -> "execute DUP"
                            1 -> "execute OVER"
                            else -> "execute PUSH s$arg"
                        }
                    }
                    stack.pushCopy(arg)
                }

                DROP -> {
                    consumeGas(gasCalculator.calculateOperationCost(8))

                    logOpcode { "execute DROP" }
                    stack.pop()
                }

                NIP -> {
                    consumeGas(gasCalculator.calculateOperationCost(8))

                    logOpcode { "execute NIP" }
                    stack.swap(0, 1)
                    stack.pop()
                }

                // (x ... y - y ...)
                POP_2, POP_3, POP_4, POP_5, POP_6, POP_7, POP_8, POP_9,
                POP_10, POP_11, POP_12, POP_13, POP_14, POP_15 -> {
                    consumeGas(gasCalculator.calculateOperationCost(8))

                    val arg = opcode and 0xF
                    logOpcode { "execute POP s$arg" }
                    stack.swap(0, arg)
                    stack.pop()
                }

                // (x ... y ... z ... a b c - c ... b ... a ... z y x)
                // XCHG s(2), s(i); XCHG s(1) s(j); XCHG s(0), s(k)
                XCHG3_0, XCHG3_1, XCHG3_2, XCHG3_3, XCHG3_4, XCHG3_5, XCHG3_6, XCHG3_7, XCHG3_8, XCHG3_9,
                XCHG3_10, XCHG3_11, XCHG3_12, XCHG3_13, XCHG3_14, XCHG3_15 -> {
                    val i = opcode and 0xF
                    val arg = code.loadInt(8).toInt()

                    val j = arg ushr 4
                    val k = arg and 0xF
                    logOpcode { "execute XCHG3 s$i,s$j,s$k" }
                    stack.swap(2, i)
                    stack.swap(1, j)
                    stack.swap(0, k)
                }

                // (x ... y ... a b - a ... b ... x y)
                // XCHG s(1),s(i); XCHG s(0),s(j).
                XCHG2 -> {
                    val arg = code.loadInt(8).toInt()

                    val i = arg ushr 4
                    val j = arg and 0xF
                    logOpcode { "execute XCHG2 s$i,s$j" }
                    stack.swap(1, i)
                    stack.swap(0, j)
                }

                // (x ... y ... a - x ... a ... y x)
                // XCHG s(i), PUSH s(j)
                XCPU -> {
                    val arg = code.loadInt(8).toInt()

                    val i = arg ushr 4
                    val j = arg and 0xF
                    logOpcode { "execute XCPU s$i,s$j" }
                    stack.swap(0, i)
                    stack.pushCopy(j)
                }

                // (x ... y ... a - a ... y ... y x)
                // PUSH s(i); SWAP; XCHG s(j)
                PUXC -> {
                    val arg = code.loadInt(8).toInt()

                    val i = arg ushr 4
                    val j = arg and 0xF
                    logOpcode { "execute PUXC s$i,s${j - 1}" }
                    stack.pushCopy(i)
                    stack.swap(0, 1)
                    stack.swap(1, j)
                }

                // (x ... y ... - x ... y ... x y)
                // PUSH s(i); PUSH s(j+1)
                PUSH2 -> {
                    val arg = code.loadInt(8).toInt()

                    val i = arg ushr 4
                    val j = arg and 0xF
                    logOpcode { "execute PUSH2 s$i,s$j" }
                    stack.pushCopy(i)
                    stack.pushCopy(j + 1)
                }

                XCHG_PUSH_SUBSET -> {
                    val arg = code.loadInt(16).toInt()

                    val subOpcode = arg ushr 12
                    val i = arg ushr 8
                    val j = arg ushr 4
                    val k = arg and 0xF
                    when (subOpcode) {
                        // (x ... y ... z ... a b c - c ... b ... a ... z y x)
                        // XCHG s(2), s(i); XCHG s(1) s(j); XCHG s(0), s(k)
                        0 -> {
                            logOpcode { "execute XCHG3 s$i,s$j,s$k" }
                            stack.swap(2, i)
                            stack.swap(1, j)
                            stack.swap(0, k)
                        }
                        // (x ... y ... z ... a b - x ... a ... b ... z y x)
                        // XCHG2 s(i), s(j); PUSH s(k)
                        1 -> {
                            logOpcode { "execute XC2PU s$i,s$j,s$k" }
                            stack.swap(1, i)
                            stack.swap(0, j)
                            stack.pushCopy(k)
                        }
                        // (x ... y ... z ... a b - b ... y ... a ... z y x)
                        // XCHG s(1), s(i); PUSH s(j); SWAP; XCHG s(k)
                        2 -> {
                            logOpcode { "execute XCPUXC s$i,s$j,s${k - 1}" }
                            stack.swap(1, i)
                            stack.pushCopy(j)
                            stack.swap(0, 1)
                            stack.swap(0, k)
                        }
                        // (x ... y ... z ... a - x ... y ... a ... z y x)
                        // XCHG s(i), PUSH s(j), PUSH s(k+1)
                        3 -> {
                            logOpcode { "execute XC2PU s$i,s$j,s$k" }
                            stack.swap(0, i)
                            stack.pushCopy(j)
                            stack.pushCopy(k + 1)
                        }
                        // (x ... y ... z ... a b - a ... b ... z ... z y x)
                        // PUSH s(i); XCHG s2; XCHG2 s(j), s(k)
                        4 -> {
                            logOpcode { "execute PUXC2 s$i,s${j - 1},s${k - 1}" }
                            stack.pushCopy(i)
                            stack.swap(2, 0)
                            stack.swap(1, j)
                            stack.swap(0, k)
                        }
                        // (x ... y ... z ... a - x ... a ... z ... z y x)
                        // PUSH s(i); SWAP; XCHG s(j); PUSH s(k)
                        5 -> {
                            logOpcode { "execute PUXCPU s$i,s${j - 1},s${k - 1}" }
                            stack.pushCopy(i)
                            stack.swap(0, 1)
                            stack.swap(0, j)
                            stack.pushCopy(k)
                        }
                        // (x ... y ... z ... a - a... y ... z ... z y x)
                        // PUSH s(i); SWAP; PUSH s(j); SWAP; XCHG s(k)
                        6 -> {
                            logOpcode { "execute PU2XC s$i,s${j - 1},s${k - 2}" }
                            stack.pushCopy(i)
                            stack.swap(0, 1)
                            stack.pushCopy(j)
                            stack.swap(0, 1)
                            stack.swap(0, k)
                        }
                        // (x ... y ... z ...  - x ... y ... z... x y z)
                        // PUSH s(i); PUSH2 s(j+1),s(k+1)
                        7 -> {
                            logOpcode { "execute PUSH3 s$i,s$j,s$k" }
                            stack.pushCopy(i)
                            stack.pushCopy(j + 1)
                            stack.pushCopy(k + 2)
                        }
                    }
                }

                // (a(j+i-1)...a(j) a(j-1)...a(0) - a(j-1)...a(0) a(j+i-1)..a(j))
                // Example: BLKSWAP 2, 4:
                // (8 7 6 {5 4} {3 2 1 0} - 8 7 6 {3 2 1 0} {5 4})
                BLKSWAP -> {
                    val arg = code.loadInt(8).toInt()
                    val i = arg ushr 4
                    val j = arg and 0xF
                    logOpcode {
                        when {
                            i == 0 -> "execute ROLL s${i + 1}"
                            j == 0 -> "execute ROLLREV s${j + 1}"
                            i == 1 && j == 3 -> "execute ROT2"
                            else -> "execute BLKSWAP s${i + 1},s${j + 1}"
                        }
                    }
                    stack.blockSwap(i, j)
                }

                // (x ... - x ... x)
                PUSH_L -> {
                    val arg = code.loadUInt(8).toInt()
                    logOpcode { "execute PUSH s$arg" }
                    stack.pushCopy(arg)
                }

                // (x ... y - y ...)
                POP_L -> {
                    val arg = code.loadUInt(8).toInt()
                    logOpcode { "execute POP s$arg" }
                    stack.swap(0, arg)
                    stack.pop()
                }

                // (a b c - b c a)
                ROT -> {
                    logOpcode { "execute ROT" }
                    stack.rot()
                }

                // (a b c - c a b)
                ROTREV -> {
                    logOpcode { "execute ROTREV" }
                    stack.rotRev()
                }

                // (a b c d - c d a b)
                SWAP2 -> {
                    logOpcode { "execute SWAP2" }
                    stack.swap(1, 3)
                    stack.swap(0, 2)
                }

                // (a b - )
                DROP2 -> {
                    logOpcode { "execute DROP2" }
                    stack.dropTop(2)
                }

                // (a b - a b a b)
                DUP2 -> {
                    logOpcode { "execute DUP2" }
                    stack.pushCopy(1)
                    stack.pushCopy(1)
                }

                // (a b c d - a b c d a b)
                OVER2 -> {
                    logOpcode { "execute OVER2" }
                    stack.pushCopy(3)
                    stack.pushCopy(3)
                }

                // (a(j+i-1)...a(j) ... - a(j)...a(j+i-1) ...)
                REVERSE -> {
                    val arg = code.loadUInt(8).toInt()
                    val i = arg ushr 4
                    val j = arg and 0xF
                    logOpcode { "execute REVERSE ${i + 2},${j}" }
                    stack.reverse(j, j + i)
                }

                BLK_SUBSET -> {
                    val arg = code.loadUInt(8).toInt()
                    val i = arg ushr 4
                    val j = arg and 0xF
                    when (i) {
                        // (xi ... x1 - )
                        0 -> {
                            logOpcode { "execute BLKDROP $j" }
                            stack.dropTop(j)
                        }
                        // (x(j) ... - x(j) ... { x(j) } i times)
                        else -> {
                            logOpcode { "execute BLKPUSH $i,$j" }
                            for (k in 0 until i) {
                                stack.pushCopy(j)
                            }
                        }
                    }
                }

                // (i - s(i))
                PICK -> {
                    logOpcode { "execute PICK" }
                    val i = stack.popInt().toInt()
                    stack.pushCopy(i)
                }

                // (x a(i)...a(1) i - a(i)...a(1) x)
                ROLLX -> {
                    logOpcode { "execute ROLLX" }

                    val i = stack.popInt().toInt()
                    stack.blockSwap(0, i)
                }

                // (a(i+1)...a(2) x i - x a(i+1)...a(2))
                ROLLREV -> {
                    logOpcode { "execute ROLLREV" }

                    val i = stack.popInt().toInt()
                    stack.blockSwap(i, 0)
                }

                // (a(j+i+1)...a(j+2) a(j+1)...a(2) j i - a(j+1)...a(2) a(j+i+1)...a(j+2))
                BLKSWX -> {
                    val j = stack.popInt().toInt()
                    val i = stack.popInt().toInt()
                    logOpcode { "execute BLKSWX $i,$j" }
                    stack.blockSwap(i, j)
                }

                // (a(j+i+1)...a(j+2) ... j i - a(j+2)...a(j+i+1) ...)
                REVX -> {
                    val j = stack.popInt().toInt()
                    val i = stack.popInt().toInt()
                    logOpcode { "execute REVX $i,$j" }
                    stack.reverse(j, j + i)
                }

                // (a(i)...a(1) i - )
                DROPX -> {
                    val i = stack.popInt().toInt()
                    logOpcode { "execute DROPX $i" }
                    stack.dropTop(i)
                }

                // (x y - y x y)
                TUCK -> {
                    logOpcode { "execute TUCK" }
                    stack.pushCopy(0)
                    stack.swap(1, 2)
                }

                // (a(i+1)...a(1) i - a(1)...a(i+1))
                XCHGX -> {
                    val i = stack.popInt().toInt()
                    logOpcode { "execute XCHGX $i" }
                    stack.swap(0, i)
                }

                // ( - stack_depth)
                DEPTH -> {
                    logOpcode { "execute DEPTH" }
                    stack.pushInt(stack.depth.toBigInt())
                }

                // (i - ), throws exception if depth < i
                CHKDEPTH -> {
                    logOpcode { "execute CHKDEPTH" }
                    val i = stack.popInt().toInt()
                    if (stack.depth < i) {
                        throw StackUnderflowException()
                    }
                }

                // ( ... a(i)...a(1) i - a(i)...a(1))
                ONLYTOPX -> {
                    val i = stack.popInt().toInt()
                    logOpcode { "execute ONLYTOPX $i" }
                    stack.onlyTop(i)
                }

                // (a(depth)...a(depth-i+1) ... i - a(depth)...a(depth-i+1))
                ONLYX -> {
                    val i = stack.popInt().toInt()
                    logOpcode { "execute ONLYX $i" }
                    stack.dropTop(stack.depth - i)
                }

                BLKDROP2 -> {
                    val arg = code.loadUInt(8).toInt()
                    val i = arg ushr 4
                    if (i == 0) {
                        throw InvalidOpcodeException((opcode shl 8) or arg)
                    }
                    val j = arg and 0xF
                    logOpcode { "execute BLKDROP2 $i,$j" }
                    stack.blockDrop(i, j)
                }

                PUSHINT_0, PUSHINT_1, PUSHINT_2, PUSHINT_3, PUSHINT_4, PUSHINT_5,
                PUSHINT_6, PUSHINT_7, PUSHINT_8, PUSHINT_9, PUSHINT_10,
                PUSHINT_MINUS_1, PUSHINT_MINUS_2, PUSHINT_MINUS_3, PUSHINT_MINUS_4, PUSHINT_MINUS_5 -> {
                    val x = INTS[opcode and 0xF]
                    logOpcode { "execute PUSHINT $x" }
                    stack.pushInt(x)
                }

                PUSHINT_XX -> {
                    val x = code.loadInt(8)
                    logOpcode { "execute PUSHINT $x" }
                    stack.pushInt(x)
                }

                PUSHINT_XXXX -> {
                    val x = code.loadInt(16)
                    logOpcode { "execute PUSHINT $x" }
                    stack.pushInt(x)
                }

                PUSHINT_LONG -> {
                    val l = code.loadUInt(5).toInt()
                    val x = code.loadInt(8 * l + 19)
                    logOpcode { "execute PUSHINT $x" }
                    stack.pushInt(x)
                }

                PUSHPOW2 -> {
                    val x = code.loadUInt(8).toInt() + 1
                    logOpcode { "execute ${if (x == 256) "PUSHNAN" else "PUSHPOW2 $x"}" }
                    stack.pushInt(ONE shl x)
                }

                PUSHPOW2DEC -> {
                    val x = code.loadUInt(8).toInt() + 1
                    logOpcode { "execute PUSHPOW2DEC $x" }
                    stack.pushInt((ONE shl x).minus(ONE))
                }

                PUSHNEGPOW2 -> {
                    val x = code.loadUInt(8).toInt() + 1
                    logOpcode { "execute PUSHNEGPOW2 $x" }
                    stack.pushInt((ONE shl x).unaryMinus())
                }

                PUSHREF -> {
                    val ref = code.loadRef()
                    logOpcode { "execute PUSHREF" }
                    stack.pushCell(ref)
                }

                PUSHREFSLICE -> {
                    logOpcode { "execute PUSHREFSLICE" }
                    val slice = loadCell(code.loadRef()).beginParse()
                    stack.pushSlice(slice)
                }

                PUSHREFCONT -> {
                    logOpcode { "execute PUSHREFCONT" }
                    val slice = loadCell(code.loadRef()).beginParse()
                    val cont = TvmContinuation.ordinary(slice)
                    stack.pushContinuation(cont)
                }

                PUSHSLICE_SHORT -> {
                    val len = code.loadUInt(4).toInt() * 8 + 4
                    val bitString = code.loadBitString(len)
                    logOpcode { "execute PUSHSLICE $bitString" }
                    val slice = Cell(bitString).beginParse()
                    stack.pushSlice(slice)
                }

                PUSHSLICE -> {
                    val arg = code.loadUInt(7).toInt()
                    val refCount = (arg ushr 5) + 1
                    val bitCount = (arg and 0b11111) * 8 + 1
                    val refs = code.loadRefs(refCount)
                    val bits = code.loadBitString(bitCount)
                    logOpcode { "execute PUSHSLICE $bits,${refCount}r" }
                    val slice = buildCell {
                        storeRefs(refs)
                        storeBitString(bits)
                    }.beginParse()
                    stack.pushSlice(slice)
                }

                PUSHSLICE_LONG -> {
                    val arg = code.loadUInt(10).toInt()
                    val refCount = arg ushr 7
                    val bitCount = (arg and 0b0111_1111) * 8 + 6
                    val refs = code.loadRefs(refCount)
                    val bits = code.loadBitString(bitCount)
                    logOpcode { "execute PUSHSLICE $bits,${refCount}r" }
                    val slice = buildCell {
                        storeRefs(refs)
                        storeBitString(bits)
                    }.beginParse()
                    stack.pushSlice(slice)
                }

                PUSHCONT_REF_0_3 -> {
                    val arg = code.loadUInt(8).toInt()
                    val refCount = arg ushr 5
                    val bitCount = (arg and 0b0111_1111) * 8
                    val refs = code.loadRefs(refCount)
                    val bits = code.loadBitString(bitCount)
                    logOpcode { "execute PUSHCONT $bits,${refCount}r" }
                    val slice = buildCell {
                        storeRefs(refs)
                        storeBitString(bits)
                    }.beginParse()
                    val cont = TvmContinuation.ordinary(slice)
                    stack.pushContinuation(cont)
                }

                PUSHCONT_REF_4 -> {
                    val arg = code.loadUInt(8).toInt()
                    consumeGas(gasCalculator.calculateOperationCost(16))

                    val refCount = (arg ushr 5) + 4
                    val bitCount = (arg and 0b0111_1111) * 8
                    val bits = code.loadBitString(bitCount)
                    val refs = code.loadRefs(refCount)
                    val slice = CellSlice(bits, refs)
                    logOpcode { "execute PUSHCONT $slice" }

                    val cont = TvmContinuation.ordinary(slice)
                    stack.pushContinuation(cont)
                }

                PUSHCONT_0, PUSHCONT_1, PUSHCONT_2, PUSHCONT_3, PUSHCONT_4, PUSHCONT_5, PUSHCONT_6,
                PUSHCONT_7, PUSHCONT_8, PUSHCONT_9, PUSHCONT_10, PUSHCONT_11, PUSHCONT_12, PUSHCONT_13,
                PUSHCONT_14, PUSHCONT_15 -> {
                    // 8 - because subslice is free, no cell creation, no actual bit reading
                    consumeGas(gasCalculator.calculateOperationCost(8))

                    // TODO: rewrite to use actual cell of cc for CellSlice
                    val slice = CellSlice(code.loadBitString((opcode and 0xF) * 8))
                    logOpcode { "execute PUSHCONT $slice" }
                    val cont = TvmContinuation.ordinary(slice)
                    stack.pushContinuation(cont)
                }

                ADD -> {
                    consumeGas(gasCalculator.calculateOperationCost(8))

                    logOpcode { "execute ADD" }
                    val x = stack.popInt()
                    val y = stack.popInt()
                    val result = x + y
                    if (result.isNan() || x.isNan() || y.isNan()) {
                        throw IntegerOverflowTvmException()
                    } else {
                        stack.pushInt(result)
                    }
                }

                SUB -> {
                    consumeGas(gasCalculator.calculateOperationCost(8))

                    logOpcode { "execute SUB" }
                    val y = stack.popInt()
                    val x = stack.popInt()
                    val result = x - y
                    if (result.isNan() || x.isNan() || y.isNan()) {
                        throw IntegerOverflowTvmException()
                    } else {
                        stack.pushInt(result)
                    }
                }

                SUBR -> {
                    consumeGas(gasCalculator.calculateOperationCost(8))

                    logOpcode { "execute SUBR" }
                    val y = stack.popInt()
                    val x = stack.popInt()
                    val result = y - x
                    if (result.isNan() || x.isNan() || y.isNan()) {
                        throw IntegerOverflowTvmException()
                    } else {
                        stack.pushInt(result)
                    }
                }

                NEGATE -> {
                    consumeGas(gasCalculator.calculateOperationCost(8))

                    logOpcode { "execute NEGATE" }
                    val x = stack.popInt()
                    val negatedX = x.unaryMinus()
                    if (x.isNan() || negatedX.isNan()) {
                        throw IntegerOverflowTvmException()
                    } else {
                        stack.pushInt(x)
                    }
                }

                INC -> {
                    consumeGas(gasCalculator.calculateOperationCost(8))

                    logOpcode { "execute INC" }
                    val x = stack.popInt()
                    val result = x.inc()
                    if (result.isNan() || x.isNan()) {
                        throw IntegerOverflowTvmException()
                    } else {
                        stack.pushInt(result)
                    }
                }

                DEC -> {
                    consumeGas(gasCalculator.calculateOperationCost(8))

                    logOpcode { "execute DEC" }
                    val x = stack.popInt()
                    val result = x.dec()
                    if (result.isNan() || x.isNan()) {
                        throw IntegerOverflowTvmException()
                    } else {
                        stack.pushInt(result)
                    }
                }

                ADDCONST -> {
                    val arg = code.loadUInt(8).toInt()
                    consumeGas(gasCalculator.calculateOperationCost(16))

                    logOpcode { "execute ADDCONST $arg" }
                    val x = stack.popInt()
                    val result = x + arg.toBigInt()
                    if (result.isNan() || x.isNan()) {
                        throw IntegerOverflowTvmException()
                    } else {
                        stack.pushInt(result)
                    }
                }

                MULCONST -> {
                    val arg = code.loadUInt(8).toInt()
                    consumeGas(gasCalculator.calculateOperationCost(16))

                    logOpcode { "execute MULCONST $arg" }
                    val x = stack.popInt()
                    val result = x * arg.toBigInt()
                    if (result.isNan() || x.isNan()) {
                        throw IntegerOverflowTvmException()
                    } else {
                        stack.pushInt(result)
                    }
                }

                MUL -> {
                    consumeGas(gasCalculator.calculateOperationCost(8))

                    logOpcode { "execute MUL" }
                    val x = stack.popInt()
                    val y = stack.popInt()
                    val result = x * y
                    if (result.isNan() || x.isNan() || y.isNan()) {
                        throw IntegerOverflowTvmException()
                    } else {
                        stack.pushInt(result)
                    }
                }






//
//                OpCodes.PUSHINT_LONG -> {
//                    val l = code.loadUInt(5).toInt()
//                    val x = code.loadInt(8 * l + 19)
//                    logOpcode { "execute PUSHINT $x" }
//                    stack.setInt(top, x)
//                    top = top + 1
//                }
//
//
//                OpCodes.EQUAL -> {
//                    logOpcode { "execute EQUAL" }
//                    stack.setBoolean(top - 2, stack.getInt(top - 1) == stack.getInt(top - 2))
//                    top = top - 1
//                }
//
//                OpCodes.IFNOTRET -> {
//                    logOpcode { "execute IFNOTRET" }
//                    if (!stack.getBoolean(--top)) {
//                        ret()
//                    }
//                }
//
//                OpCodes.SETCP -> {
//                    val arg = code.loadUInt(8).toInt()
//                    if (arg and 0xF0 == 0) {
//                        logOpcode { "execute SETCP $arg" }
//                        if (arg == 0) continue@loop
//                        throw RuntimeException("Unknown CP: $arg")
//                    } else {
//                        val x = (arg and 0x0F) - 16
//                        logOpcode { "execute SETCPX $x" }
//                        throw RuntimeException("Unknown CP: $x")
//                    }
//                }

                else -> throw InvalidOpcodeException(opcode)
            }
        }

        logOpcode { "execute implicit RET" }
        consumeGas(gasCalculator.calculateImplicitReturn())

        logTvm { "steps: $steps gas: used=$usedGas, max=$baseGas" }
    }

    private fun logOpcode(message: () -> String) {
        contract {
            callsInPlace(message, InvocationKind.AT_MOST_ONCE)
        }
        println(message.invoke())
    }

    private fun logTvm(message: () -> String) {
        contract {
            callsInPlace(message, InvocationKind.AT_MOST_ONCE)
        }
        println(message.invoke())
    }

    private companion object {
        private val ZERO = 0.toBigInt()
        private val ONE = 1.toBigInt()
        private val INTS = arrayOf(
            ZERO,
            ONE,
            2.toBigInt(),
            3.toBigInt(),
            4.toBigInt(),
            5.toBigInt(),
            6.toBigInt(),
            7.toBigInt(),
            8.toBigInt(),
            9.toBigInt(),
            10.toBigInt(),
            (-5).toBigInt(),
            (-4).toBigInt(),
            (-3).toBigInt(),
            (-2).toBigInt(),
            (-1).toBigInt()
        )
    }
}

public fun BigInt.isNan(): Boolean = this.bitLength > 256
