package org.ton.kotlin.blockchain.currency

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.bigint.*

/**
 * Variable-length 120-bit integer. Used for native currencies.
 */
@Serializable(Coins.Serializer::class)
public class Coins(
    public val value: BigInt
) : Comparable<Coins> {
    init {
        require(MIN_VALUE <= value && value <= MAX_VALUE)
    }

    override fun compareTo(other: Coins): Int = value.compareTo(other.value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Coins

        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String = "Coins($value)"

    public companion object {
        private val MIN_VALUE = 0.toBigInt()
        private val MAX_VALUE = (1.toBigInt() shl 120) - 1.toBigInt()

        public val MIN: Coins = Coins(MIN_VALUE)
        public val MAX: Coins = Coins(MAX_VALUE)
    }

    private object Serializer : KSerializer<Coins> {
        private val serializer = String.serializer()

        override val descriptor: SerialDescriptor = SerialDescriptor("coins", serializer.descriptor)

        override fun serialize(
            encoder: Encoder,
            value: Coins
        ) {
            serializer.serialize(encoder, value.value.toString())
        }

        override fun deserialize(decoder: Decoder): Coins {
            return Coins(serializer.deserialize(decoder).toBigInt())
        }
    }
}
