@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.message.address

import kotlinx.io.bytestring.ByteString
import org.ton.bitstring.BitString
import org.ton.bitstring.toBitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.crypto.crc16
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSize
import org.ton.tlb.TlbCodec
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.io.encoding.Base64
import kotlin.jvm.JvmStatic

/**
 * Standard internal address.
 *
 * ```tlb
 * addr_std$10 anycast:(Maybe Anycast)
 *    workchain_id:int8 address:bits256  = MsgAddressInt;
 * ```
 */
public data class AddrStd(
    /**
     * Optional anycast info.
     */
    override val anycast: Anycast?,

    /**
     * Workchain id (one-byte range).
     */
    override val workchain: Int,

    /**
     * Account id.
     */
    override val address: BitString,
) : MsgAddressInt {
    public constructor(anycast: Anycast?, workchain: Int, address: ByteArray) : this(
        anycast,
        workchain,
        address.toBitString()
    )

    public constructor(anycast: Anycast?, workchain: Int, address: ByteString) : this(
        anycast,
        workchain,
        address.toBitString()
    )

    public constructor(workchain: Int, address: BitString) : this(null, workchain, address)
    public constructor(workchain: Int, address: ByteArray) : this(null, workchain, address)
    public constructor(workchain: Int, address: ByteString) : this(null, workchain, address)

    init {
        require(workchain in -128..128) { "workchain must be in range -128..128: $workchain" }
        require(address.size == 256) { "expected address.size == 256, actual: ${address.size}" }
    }

    override val cellSize: CellSize
        get() = anycast?.let { CELL_SIZE_MIN + it.cellSize } ?: CELL_SIZE_MIN

    override fun toAddrStd(): AddrStd = this

    public fun toString(
        userFriendly: Boolean = true,
        urlSafe: Boolean = true,
        testOnly: Boolean = false,
        bounceable: Boolean = true
    ): String = toString(this, userFriendly, urlSafe, testOnly, bounceable)

    override fun toString(): String = if (anycast == null) {
        toString(userFriendly = false)
    } else {
        "AddrStd(anycast=$anycast, ${toString(userFriendly = false)})"
    }

    public companion object : TlbCodec<AddrStd> {
        public const val BITS_MIN: Int = 2 + 1 + 8 + 256
        public val CELL_SIZE_MIN: CellSize = CellSize(BITS_MIN, 0)

        @JvmStatic
        public fun toString(
            address: AddrStd,
            userFriendly: Boolean = true,
            urlSafe: Boolean = true,
            testOnly: Boolean = false,
            bounceable: Boolean = true
        ): String {
            return if (userFriendly) {
                val tag = tag(testOnly, bounceable)
                val workchain = address.workchain
                val rawAddress = address.address.toByteArray()
                val checksum = checksum(tag, workchain, rawAddress)

                val data = ByteArray(36)
                data[0] = tag
                data[1] = workchain.toByte()
                rawAddress.copyInto(data, 2)
                data[32 + 2] = (checksum ushr 8).toByte()
                data[32 + 2 + 1] = (checksum).toByte()

                if (urlSafe) {
                    Base64.UrlSafe.encode(data)
                } else {
                    Base64.encode(data)
                }
            } else {
                "${address.workchain}:${address.address.toHexString()}"
            }
        }

        @JvmStatic
        public fun parse(address: String): AddrStd = try {
            if (address.contains(':')) {
                parseRaw(address)
            } else {
                parseUserFriendly(address)
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Can't parse address: $address", e)
        }

        @JvmStatic
        public fun parseRaw(address: String): AddrStd {
            require(address.contains(':'))
            // 32 bytes, each represented as 2 characters
            require(address.substringAfter(':').length == 32 * 2)
            return AddrStd(
                // toByte() to make sure it fits into 8 bits
                workchain = address.substringBefore(':').toByte().toInt(),
                address = address.substringAfter(':').hexToByteArray()
            )
        }

        @JvmStatic
        public fun parseUserFriendly(address: String): AddrStd {
            val addressBytes = ByteArray(36)

            try {
                Base64.UrlSafe.decode(address).copyInto(addressBytes)
            } catch (e: Exception) {
                try {
                    Base64.decode(address).copyInto(addressBytes)
                } catch (e: Exception) {
                    throw IllegalArgumentException("Can't parse address: $address", e)
                }
            }
            val tag = addressBytes[0]
            val cleanTestOnly = tag and 0x7F.toByte()
            check((cleanTestOnly == 0x11.toByte()) or (cleanTestOnly == 0x51.toByte())) {
                "unknown address tag"
            }
            val workchainId = addressBytes[1].toInt()
            val rawAddress = addressBytes.copyOfRange(fromIndex = 2, toIndex = 2 + 32)
            val expectedChecksum =
                ((addressBytes[2 + 32].toInt() and 0xFF) shl 8) or (addressBytes[2 + 32 + 1].toInt() and 0xFF)

            val actualChecksum = checksum(tag, workchainId, rawAddress)
            check(expectedChecksum == actualChecksum) {
                "CRC check failed"
            }

            return AddrStd(
                workchain = workchainId,
                address = rawAddress
            )
        }

        private fun checksum(tag: Byte, workchainId: Int, address: ByteArray): Int =
            crc16(byteArrayOf(tag, workchainId.toByte()), address)

        // Get the tag byte based on set flags
        private fun tag(testOnly: Boolean, bounceable: Boolean): Byte =
            (if (testOnly) 0x80.toByte() else 0.toByte()) or
                    (if (bounceable) 0x11.toByte() else 0x51.toByte())

        override fun loadTlb(slice: CellSlice, context: CellContext): AddrStd {
            return MsgAddressInt.loadTlb(slice, context) as AddrStd
        }

        override fun storeTlb(builder: CellBuilder, value: AddrStd, context: CellContext) {
            MsgAddressInt.storeTlb(builder, value, context)
        }
    }
}

public fun AddrStd(address: String): AddrStd = AddrStd.parse(address)
