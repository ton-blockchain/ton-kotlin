package org.ton.kotlin.tvm

import org.ton.cell.Cell
import kotlin.math.max

public interface GasCalculator {
    public fun calculateOperationCost(bitCount: Int): Long

    public fun calculateRollCost(elements: Int): Long = max(elements - 255L, 0L)

    public fun calculateCellLoad(cell: Cell): Long

    public fun calculateCellReload(cell: Cell): Long

    public fun calculateCellCreate(cell: Cell): Long

    public fun calculateException(): Long

    public fun calculateImplicitReturn(): Long
}

public object FreeGasCalculator : GasCalculator {
    override fun calculateOperationCost(bitCount: Int): Long = 0

    override fun calculateCellLoad(cell: Cell): Long = 0

    override fun calculateCellReload(cell: Cell): Long = 0

    override fun calculateCellCreate(cell: Cell): Long = 0

    override fun calculateException(): Long = 0

    override fun calculateImplicitReturn(): Long = 0
}

public object DefaultGasCalculator : GasCalculator {
    override fun calculateOperationCost(bitCount: Int): Long = 10 + bitCount.toLong()

    override fun calculateCellLoad(cell: Cell): Long = 100

    override fun calculateCellReload(cell: Cell): Long = 25

    override fun calculateCellCreate(cell: Cell): Long = 500

    override fun calculateException(): Long = 50

    override fun calculateImplicitReturn(): Long = 5
}
