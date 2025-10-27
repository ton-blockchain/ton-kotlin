package org.ton.sdk.crypto

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.hexToByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.serialization.Serializable
import org.ton.sdk.tl.Bits256
import org.ton.sdk.tl.serializers.ByteStringBase64Serializer
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

/**
 * Represents a wrapper around a 256-bit hash value.
 * This class ensures the hash is exactly 32 bytes in size.
 *
 * The hash value is stored as a [ByteString] and can be converted to a byte array or represented as a hexadecimal string.
 * Provides utility methods for parsing hexadecimal strings and two-way conversion between [ByteArray] and [ByteString].
 */
@Serializable
public class HashBytes(
    @Serializable(ByteStringBase64Serializer::class)
    @Bits256
    @get:JvmName("value")
    public val value: ByteString
) {
    /**
     * Secondary constructor for creating a `HashBytes` instance from a `ByteArray`.
     *
     * @param array The byte array used to initialize the `HashBytes` instance.
     * The provided `ByteArray` is wrapped into a `ByteString`, which is passed to the primary constructor.
     */
    public constructor(array: ByteArray) : this(ByteString(array))

    init {
        require(value.size == 32)
    }

    /**
     * Converts the encapsulated ByteString value to a ByteArray representation.
     *
     * @return the ByteArray representation of the ByteString value.
     */
    public fun toByteArray(): ByteArray = value.toByteArray()

    /**
     * Returns the `ByteString` representation of the current `HashBytes` value.
     *
     * @return the `ByteString` instance representing the hash value.
     */
    public fun toByteString(): ByteString = value

    override fun toString(): String = value.toHexString()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as HashBytes
        return value == other.value
    }

    override fun hashCode(): Int = value.hashCode()

    public companion object {
        /**
         * Parses a hexadecimal string into a [HashBytes] instance.
         * The input string must be either 64 characters long or 66 characters long if it includes the `0x` prefix.
         *
         * @param hexString the hexadecimal string to be parsed. It must represent a 256-bit hash
         * (64 characters long) or have a prefix `0x` followed by 64 characters.
         * @return a [HashBytes] object containing the parsed hash data.
         * @throws IllegalArgumentException if the input string does not meet the required format or length.
         */
        @JvmStatic
        public fun parseHex(hexString: String): HashBytes {
            require(hexString.length == 64 || (hexString.startsWith("0x") && hexString.length == 66)) {
                "Hex string must be 64 characters long or 66 characters long with the `0x` prefix"
            }
            return HashBytes(hexString.removePrefix("0x").hexToByteString())
        }
    }
}

/**
 * Constructs a [HashBytes] instance from the given unsigned byte array.
 *
 * @param array the input unsigned byte array used to create a [HashBytes] instance.
 *              The array is converted to a signed byte array before construction.
 * @return a new [HashBytes] instance initialized with the converted byte array.
 */
@Suppress("NOTHING_TO_INLINE")
public inline fun HashBytes(array: UByteArray): HashBytes = HashBytes(array.asByteArray())
