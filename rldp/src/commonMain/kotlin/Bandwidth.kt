package org.ton.kotlin.rldp

import kotlin.jvm.JvmInline
import kotlin.math.roundToLong
import kotlin.time.Duration

@JvmInline
value class Bandwidth(
    val bitsPerSecond: Long
) : Comparable<Bandwidth> {
    val kBitsPerSecond: Long get() = bitsPerSecond / 1000

    val bytesPerSecond: Long get() = bitsPerSecond / 8
    val kBytesPerSecond: Long get() = bytesPerSecond / 8000

    fun toBytesPerPeriod(period: Duration): Long {
        return (bitsPerSecond * period.inWholeMicroseconds) / 8_000_000
    }

    fun toKBytesPerPeriod(period: Duration): Long {
        return (bitsPerSecond * period.inWholeMicroseconds) / 8_000_000 / 1000
    }

    operator fun times(duration: Duration): Bandwidth {
        return fromBitsPerSecond((bitsPerSecond * duration.inWholeMicroseconds) / 1_000_000)
    }

    operator fun times(number: Double): Bandwidth {
        return fromBitsPerSecond((bitsPerSecond * number).roundToLong())
    }

    override fun toString(): String {
        if (bitsPerSecond < 80000) {
            return "$bitsPerSecond bits/s (${bitsPerSecond / 8} bytes/s)"
        }
        val divisor: Double
        val unit: String
        when {
            bitsPerSecond < 8 * 1000 * 1000 -> {
                divisor = 1e3
                unit = "k"
            }

            bitsPerSecond < 8 * 1000 * 1000 * 1000 -> {
                divisor = 1e6
                unit = "M"
            }

            else -> {
                divisor = 1e9
                unit = "G"
            }
        }
        val bitsPerSecondWithUnit = bitsPerSecond / divisor
        val bytesPerSecondWithUnit = bitsPerSecondWithUnit / 8

        fun StringBuilder.appendFormatted(value: Double) {
            val v = (value * 100).roundToLong()
            val intPart = v / 100
            val fracPart = v % 100
            append(intPart)
            append('.')
            if (fracPart < 10) {
                append('0')
            }
            append(fracPart)
        }

        return buildString {
            appendFormatted(bitsPerSecondWithUnit)
            append(' ')
            append(unit)
            append("bits/s (")
            appendFormatted(bytesPerSecondWithUnit)
            append(' ')
            append(unit)
            append("bytes/s)")
        }
    }

    override fun compareTo(other: Bandwidth): Int {
        return bitsPerSecond.compareTo(other.bitsPerSecond)
    }

    companion object {
        private val ZERO = Bandwidth(0L)
        private val INFINITE = Bandwidth(Long.MAX_VALUE)

        fun fromBitsPerSecond(bitsPerSecond: Long): Bandwidth {
            require(bitsPerSecond >= 0) { "bitsPerSecond cannot be negative" }
            if (bitsPerSecond == 0L) return ZERO
            return Bandwidth(bitsPerSecond)
        }

        fun fromBytesPerSecond(bytesPerSecond: Long): Bandwidth {
            require(bytesPerSecond >= 0) { "bytesPerSecond cannot be negative" }
            if (bytesPerSecond == 0L) return ZERO
            return Bandwidth(bytesPerSecond * 8)
        }

        fun fromBytesPerPeriod(bytes: Long, period: Duration): Bandwidth {
            val micros = period.inWholeMicroseconds.coerceAtLeast(1)
            val bytesPerSecond = bytes * 1_000_000.0 / micros
            return fromBytesPerSecond(bytesPerSecond.toLong())
        }
    }
}
