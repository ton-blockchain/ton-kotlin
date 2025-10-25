package org.ton.sdk.blockchain.address

import kotlinx.io.Buffer
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.hexToByteString
import kotlinx.io.bytestring.toHexString
import kotlinx.io.readULong
import kotlinx.io.write
import org.ton.sdk.blockchain.ShardId
import org.ton.sdk.crypto.HashBytes
import org.ton.sdk.crypto.crc16
import kotlin.io.encoding.Base64
import kotlin.jvm.JvmStatic

/**
 * Generic blockchain address.
 *
 * In TON, an account address consists of a workchain identifier and an account identifier
 * (usually a 256-bit hash of the account’s initial state). See the TON whitepaper:
 * - “New smart contracts and their addresses” (§1.7; docs/tblkch.tex)
 * - “Message addresses and next-hop computation” (§2.1; docs/tblkch.tex)
 *
 * Implementations may also support anycast information as defined by the TL‑B schema.
 *
 * @property workchain Signed workchain id; `0` is the basechain, `-1` is the masterchain.
 * @property prefix First 64 bits of the 256‑bit account id; commonly used in routing
 * (cf. whitepaper next‑hop algorithm compares the first 64 bits; see §2.1).
 */
public sealed interface Address {
    public val workchain: Int

    public val prefix: ULong
}

/**
 * Standard internal address for accounts with a 256‑bit id (addr_std in TL‑B).
 *
 * TL‑B (§3.1; docs/tblkch.tex):
 * anycast_info$_ depth:(#<= 30) { depth >= 1 } rewrite_pfx:(bits depth) = Anycast;
 * addr_std$10 anycast:(Maybe Anycast) workchain_id:int8 address:bits256 = MsgAddressInt;
 *
 * This class models that structure and provides parsing/formatting helpers for:
 * - Raw form: "workchain:hex256"
 * - User‑friendly Base64 form used in wallets and explorers
 *
 * @property anycast Optional anycast prefix information. When present, routing uses an effective
 * destination computed by replacing the first d bits of the account id with the rewrite prefix
 * (whitepaper “Support for anycast addresses”, §2.1).
 * @property workchain Signed workchain id of this address. ShardId.MASTERCHAIN.workchain is -1.
 * @property address 256‑bit account id (HashBytes of 32 bytes).
 */
public class AddressStd(
    public val anycast: Anycast?,
    public override val workchain: Int,
    public val address: HashBytes
) : Address {
    public constructor(workchain: Int, address: HashBytes) : this(anycast = null, workchain, address)

    /** True if this address belongs to the masterchain ([workchain] = -1). */
    public val isMasterchain: Boolean get() = workchain == ShardId.MASTERCHAIN.workchain

    /**
     * First 64 bits of the account id, interpreted as an unsigned big‑endian integer.
     * This prefix is commonly used in routing decisions (cf. whitepaper next‑hop algorithm,
     * which may compare the first 64 bits of account ids; see §2.1).
     */
    public override val prefix: ULong
        get() = with(Buffer()) {
            write(address.value, 0, 8)
            readULong()
        }

    /**
     * Returns this address in the user‑friendly Base64 form defined by TON.
     * Equivalent to toBase64String(Base64.Default, bounceable, testnet = false).
     *
     * @param bounceable When true, sets tag 0x11; when false, sets tag 0x51.
     * @return 48‑character Base64 string.
     */
    public fun toBase64String(bounceable: Boolean): String =
        toBase64String(format = Base64.Default, bounceable, testnet = false)

    /**
     * Returns this address in the user‑friendly Base64 form using the default Base64 alphabet.
     *
     * - bounceable = true sets tag 0x11; false sets tag 0x51.
     * - testnet = true sets the highest bit (0x80) of the tag to mark “test‑only”.
     *
     * The encoded payload is 36 bytes: [tag][workchain:int8][account_id:32][crc16:2]. The CRC16 is
     * computed over the first 34 bytes (tag, workchain, 32‑byte id) with the same polynomial used by
     * org.ton.kotlin.crypto.crc16 (CRC‑16/CCITT/XMODEM table), then the result is appended big‑endian.
     *
     * @param bounceable When true, sets tag 0x11; when false, sets tag 0x51.
     * @param testnet When true, sets the highest bit (0x80) of the tag to mark the address as test-only.
     * @return 48‑character Base64 string.
     */
    public fun toBase64String(bounceable: Boolean, testnet: Boolean): String =
        toBase64String(format = Base64.Default, bounceable, testnet)

    /**
     * Returns this address in the user‑friendly Base64 form using the given alphabet.
     *
     * Tag byte:
     * - 0x11 if bounceable = true (bounceable address)
     * - 0x51 if bounceable = false (non‑bounceable)
     * - If testnet = true, set the highest bit (0x80) of the tag (yielding 0x91 or 0xD1).
     *
     * Encoded payload (36 bytes):
     * [tag][workchain:int8][account_id:32][crc16:2], where crc16 is computed over the first 34 bytes
     * (tag + workchain + account_id) using CRC‑16/CCITT/XMODEM and appended big‑endian.
     *
     * @param format Base64 alphabet to use (Base64.Default or Base64.UrlSafe).
     * @param bounceable Whether to mark the address bounceable (affects base tag: 0x11 vs 0x51).
     * @param testnet Whether to mark the address as test‑only (sets the tag’s high bit 0x80).
     * @return 48‑character Base64 string.
     */
    public fun toBase64String(format: Base64 = Base64.Default, bounceable: Boolean, testnet: Boolean = false): String {
        val bytes = ByteArray(36)
        bytes[0] = (0x51 - (if (bounceable) 0x40 else 0) + (if (testnet) 0x80 else 0)).toByte()
        bytes[1] = workchain.toByte()
        address.value.copyInto(bytes, destinationOffset = 2)

        val crc = crc16(bytes.copyOf(34))
        bytes[34] = (crc ushr 8).toByte()
        bytes[35] = (crc and 0xFF).toByte()

        return format.encode(bytes)
    }

    /**
     * Returns this address in raw form "workchain:hex256" using the default hex format.
     *
     * @return Raw string "workchain:hex256".
     */
    public fun toRawString(): String = toRawString(format = HexFormat.Default)

    /**
     * Returns this address in raw form "workchain:hex256" using the provided hex format.
     *
     * @param format Hex formatting options.
     * @return Raw string "workchain:hex256".
     */
    public fun toRawString(format: HexFormat): String = "$workchain:${address.value.toHexString(format)}"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AddressStd) return false
        if (workchain != other.workchain) return false
        if (anycast != other.anycast) return false
        if (address != other.address) return false
        return true
    }

    override fun hashCode(): Int {
        var result = workchain
        result = 31 * result + (anycast?.hashCode() ?: 0)
        result = 31 * result + address.hashCode()
        return result
    }

    override fun toString(): String {
        if (anycast == null) {
            return "AddressStd(${toRawString()})"
        }
        return "AddressStd(anycast=$anycast,${toRawString()})"
    }

    public companion object {
        /**
         * Parses an address from either user‑friendly Base64 (48 chars) or raw "workchain:hex256" form.
         *
         * - Base64: validated by CRC16; URL‑safe alphabet is auto‑detected when the string contains
         *   '_' or '-' (otherwise the default Base64 alphabet is used).
         * - Raw: accepts 66–69 characters (minus sign for negative workchain, optional 0x prefix is not supported).
         *
         * Throws IllegalArgumentException for unsupported formats.
         *
         * @param addressString Source string in Base64 (48 chars) or raw form.
         * @return Parsed AddressStd instance.
         */
        @JvmStatic
        public fun parse(addressString: String): AddressStd {
            return when (addressString.length) {
                48 -> addrStdParseBase64(addressString)
                in 66..69 -> addrStdParseHex(addressString)
                else -> throw IllegalArgumentException(
                    "Expected either a 48-char string in base64 format or a hexadecimal raw format, " +
                            "but was \"${addressString.truncateForErrorMessage(64)}\" of length ${addressString.length}"
                )
            }
        }

        /**
         * Parses a raw "workchain:hex256" address.
         *
         * @param addressString Raw address string in the form "workchain:hex256".
         * @return Parsed AddressStd instance.
         */
        @JvmStatic
        public fun parseRaw(addressString: String): AddressStd {
            require(addressString.length in 66..69) {
                "Expected a 66..69-char hexadecimal string, " +
                        "but was \"${addressString.truncateForErrorMessage(64)}\" " +
                        "of length ${addressString.length}"
            }
            return addrStdParseHex(addressString)
        }

        /**
         * Parses a 48‑character user‑friendly Base64 address (default or URL‑safe alphabets).
         *
         * @param addressString Base64 string (48 chars). URL-safe alphabet is allowed.
         * @return Parsed AddressStd instance.
         */
        @JvmStatic
        public fun parseBase64(addressString: String): AddressStd {
            require(addressString.length == 48) {
                "Expected a 48-char base64 string, " +
                        "but was \"${addressString.truncateForErrorMessage(64)}\" " +
                        "of length ${addressString.length}"
            }
            return addrStdParseBase64(addressString)
        }
    }
}

private fun addrStdParseBase64(base64String: String): AddressStd {
    val format = if (base64String.indexOfAny(charArrayOf('_', '-')) != -1) {
        Base64.UrlSafe
    } else {
        Base64.Default
    }
    val bytes = format.decode(base64String)
    val crc = crc16(bytes.copyOf(34))
    require((bytes[34].toInt() and 0xFF) == (crc ushr 8) && bytes[35].toInt() and 0xFF == crc and 0xFF) {
        "Invalid crc"
    }
    return AddressStd(
        workchain = bytes[1].toInt(),
        address = HashBytes(ByteString(*bytes.copyOfRange(2, 34)))
    )
}

private fun addrStdParseHex(hexString: String): AddressStd {
    val (rawWorkchain, rawAddress) = hexString.split(":")
    return AddressStd(rawWorkchain.toInt(), HashBytes(rawAddress.hexToByteString()))
}

private fun String.truncateForErrorMessage(maxLength: Int): String {
    return if (length <= maxLength) this else substring(0, maxLength) + "..."
}
