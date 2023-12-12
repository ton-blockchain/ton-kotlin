package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.bigint.BigInt
import org.ton.bigint.pow
import org.ton.bigint.times
import org.ton.bigint.toBigInt
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.*
import org.ton.tlb.providers.TlbConstructorProvider
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.math.pow

@SerialName("nanocoins")
@Serializable
public data class Coins(
    @get:JvmName("amount")
    val amount: VarUInteger = VarUInteger(0)
) : TlbObject {
    public constructor(amount: Long) : this(VarUInteger(amount))
    public constructor(amount: BigInt) : this(VarUInteger(amount))

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("nanocoins") {
        field("amount", amount)
    }

    override fun toString(): String = toString(decimals = DECIMALS)

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
        private const val DECIMALS = 9

        @JvmStatic
        public fun tlbCodec(): TlbCodec<Coins> = CoinsTlbConstructor

        @JvmStatic
        public fun of(coins: Long, decimals: Int = DECIMALS): Coins =
            Coins(VarUInteger(coins.toBigInt() * 10.toBigInt().pow(decimals)))

        @JvmStatic
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
        cellBuilder: CellBuilder, value: Coins
    ) = cellBuilder {
        storeTlb(varUIntegerCodec, value.amount)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): Coins = cellSlice {
        val amount = loadTlb(varUIntegerCodec)
        Coins(amount)
    }
}
