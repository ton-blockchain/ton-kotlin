package org.ton.kotlin.dht

sealed class Quorum {

    abstract fun eval(total: Int): Int

    object One : Quorum() {
        override fun eval(total: Int): Int {
            return 1
        }
    }

    object Majority : Quorum() {
        override fun eval(total: Int): Int {
            return (total / 2) + 1
        }
    }

    object All : Quorum() {
        override fun eval(total: Int): Int {
            return total
        }
    }

    class N(val n: Int) : Quorum() {
        init {
            require(n > 0) { "N must be greater than 0" }
        }

        override fun eval(total: Int): Int {
            require(total >= 0) { "Total must be non-negative" }
            return minOf(n, total)
        }
    }
}
