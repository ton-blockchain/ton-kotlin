package org.ton.kotlin.tvm

import kotlin.math.max

public interface GasCalculator {
    public fun calculateOperationCost(bitCount: Int): Long

    public fun calculateRollCost(elements: Int): Long = max(elements - 255L, 0L)
}

public object FreeGasCalculator : GasCalculator {
    override fun calculateOperationCost(bitCount: Int): Long = 0
}
