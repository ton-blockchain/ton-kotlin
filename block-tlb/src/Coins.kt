@file:Suppress("DEPRECATION")

package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.sdk.bigint.BigInt
import org.ton.sdk.bigint.pow
import org.ton.sdk.bigint.times
import org.ton.sdk.bigint.toBigInt
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.math.pow

/**
 * Variable-length 120-bit integer. Used for native currencies.
 *
 * Stored as 4 bits of `len` (0..=15), followed by `len` bytes.
 *
 * @see [CurrencyCollection]
 */
@SerialName("nanocoins")

public data class Coins(
    @get:JvmName("amount")
    val amount: VarUInteger = VarUInteger(0)
) : TlbObject {
    public constructor(amount: Long) : this(VarUInteger(amount))
    public constructor(amount: BigInt) : this(VarUInteger(amount))

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("nanocoins") {
        field("amount", amount)
    }

    override fun toString(): String = amount.value.toString()

    public fun toString(decimals: Int): String =
        amount.value.toString().let {
            it.dropLast(decimals).ifEmpty { "0" } + "." + it.takeLast(decimals).padStart(decimals, '0')
        }

    public operator fun plus(other: Coins): Coins = Coins(amount + other.amount)
    public operator fun minus(other: Coins): Coins = Coins(amount - other.amount)
    public operator fun times(other: Coins): Coins = Coins(amount * other.amount)
    public operator fun div(other: Coins): Coins = Coins(amount / other.amount)
    public operator fun rem(other: Coins): Coins = Coins(amount % other.amount)
    public operator fun inc(): Coins = Coins(amount + VarUInteger(1, 1.toBigInt()))
    public operator fun dec(): Coins = Coins(amount - VarUInteger(1, 1.toBigInt()))

    public companion object : TlbConstructorProvider<Coins> by CoinsTlbConstructor {
        public val ZERO: Coins = Coins(0)

        private const val DECIMALS = 9

        @JvmStatic
        public fun tlbCodec(): TlbCodec<Coins> = CoinsTlbConstructor

        @JvmStatic
        public fun of(coins: Long, decimals: Int = DECIMALS): Coins =
            Coins(VarUInteger(coins.toBigInt() * 10.toBigInt().pow(decimals)))

        @JvmStatic
        @Deprecated(message = "Double is very dangerous, scheduled to remove", level = DeprecationLevel.ERROR)
        public fun of(coins: Double, decimals: Int = DECIMALS): Coins =
            Coins(
                VarUInteger(
                    (coins * 10.0.pow(decimals)).toLong().toBigInt()
                )
            )

        @JvmStatic
        public fun ofNano(coins: Long): Coins = Coins(VarUInteger(coins))

        @JvmStatic
        public fun ofNano(coins: BigInt): Coins = Coins(VarUInteger(coins))
    }
}

private object CoinsTlbConstructor : TlbConstructor<Coins>(
    schema = "nanocoins\$_ amount:(VarUInteger 16) = Coins;"
) {
    private val varUIntegerCodec = VarUInteger.tlbCodec(16)

    override fun storeTlb(
        builder: CellBuilder, value: Coins
    ) = builder {
        storeTlb(varUIntegerCodec, value.amount)
    }

    override fun loadTlb(
        slice: CellSlice
    ): Coins = slice {
        val amount = loadTlb(varUIntegerCodec)
        Coins(amount)
    }
}
