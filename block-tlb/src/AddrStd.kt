package org.ton.block

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import org.ton.bitstring.BitString
import org.ton.bitstring.toBitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.kotlin.cell.CellSize
import org.ton.sdk.crypto.crc16
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.io.encoding.Base64
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

public inline fun AddrStd(address: String): AddrStd = AddrStd.parse(address)


@SerialName("addr_std")
public data class AddrStd(
    @get:JvmName("anycast")
    val anycast: Maybe<Anycast>,

    @get:JvmName("workchainId")
    override val workchainId: Int,

    @get:JvmName("address")
    override val address: BitString
) : MsgAddressInt {
    public constructor() : this(0, BitString(256))
    public constructor(workchainId: Int, address: BitString) : this(null, workchainId, address)
    public constructor(workchainId: Int, address: ByteArray) : this(null, workchainId, address)
    public constructor(workchainId: Int, address: ByteString) : this(null, workchainId, address.toByteArray())
    public constructor(anycast: Anycast?, workchainId: Int, address: ByteArray) : this(
        anycast.toMaybe(),
        workchainId,
        address.toBitString()
    )

    public constructor(anycast: Anycast?, workchainId: Int, address: BitString) : this(
        anycast.toMaybe(),
        workchainId,
        address.toBitString()
    )

    public constructor(anycast: Anycast?, workchainId: Int, address: ByteString) : this(
        anycast.toMaybe(),
        workchainId,
        address.toBitString()
    )

    init {
        require(address.size == 256) { "expected address.size == 256, actual: ${address.size}" }
    }

    override val cellSize: CellSize
        get() = anycast.value?.let { CELL_SIZE_MIN + it.cellSize } ?: CELL_SIZE_MIN

    override fun toAddrStd(): AddrStd = this

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("addr_std") {
            field("anycast", anycast)
            field("workchain_id", workchainId)
            field("address", address)
        }
    }

    override fun toString(): String = print().toString()

    public fun toString(
        userFriendly: Boolean = true,
        urlSafe: Boolean = true,
        testOnly: Boolean = false,
        bounceable: Boolean = true
    ): String = toString(this, userFriendly, urlSafe, testOnly, bounceable)

    public companion object : TlbCodec<AddrStd> by AddrStdTlbConstructor {
        public const val BITS_MIN: Int = 2 + 1 + 8 + 256
        public val CELL_SIZE_MIN: CellSize = CellSize(BITS_MIN, 0)

        @JvmStatic
        public fun tlbCodec(): TlbConstructor<AddrStd> = AddrStdTlbConstructor

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
                val workchain = address.workchainId
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
                "${address.workchainId}:${address.address.toHexString()}"
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
                workchainId = address.substringBefore(':').toByte().toInt(),
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
            var workchainId = addressBytes[1].toInt()
            var rawAddress = addressBytes.copyOfRange(fromIndex = 2, toIndex = 2 + 32)
            var expectedChecksum =
                ((addressBytes[2 + 32].toInt() and 0xFF) shl 8) or (addressBytes[2 + 32 + 1].toInt() and 0xFF)

            val actualChecksum = checksum(tag, workchainId, rawAddress)
            check(expectedChecksum == actualChecksum) {
                "CRC check failed"
            }

            return AddrStd(
                workchainId = workchainId,
                address = rawAddress
            )
        }

        private fun checksum(tag: Byte, workchainId: Int, address: ByteArray): Int =
            crc16(byteArrayOf(tag, workchainId.toByte()), address)

        // Get the tag byte based on set flags
        private fun tag(testOnly: Boolean, bounceable: Boolean): Byte =
            (if (testOnly) 0x80.toByte() else 0.toByte()) or
                    (if (bounceable) 0x11.toByte() else 0x51.toByte())
    }
}

private object AddrStdTlbConstructor : TlbConstructor<AddrStd>(
    schema = "addr_std\$10 anycast:(Maybe Anycast) workchain_id:int8 address:bits256 = MsgAddressInt;"
) {
    private val MaybeAnycast = Maybe(Anycast)

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: AddrStd
    ) = cellBuilder {
        storeTlb(MaybeAnycast, value.anycast)
        storeInt(value.workchainId, 8)
        storeBits(value.address)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): AddrStd = cellSlice {
        val anycast = loadTlb(MaybeAnycast)
        val workchainId = loadInt(8).toInt()
        val address = loadBits(256)
        AddrStd(anycast, workchainId, address)
    }
}
//1000100000000001011010100101010000101101011101000110111001101010101000000011101111100011001100110010111010101010000111001111110110101001100010000010111010101001110111001010000000111011110110110001010100001000001000010001000110011100100001001111000101101100110110111100100000000111011100001111111111010011011110101011010110100100011010111101110000101001100110100011111001001110000010000110010111100100010001111001111010001101110101010011111000010101100110110101011111111011010010100011111011010100001000001101110000011101110110011100001010001111110111010101010010000110101110011100001100100110001100001100111000111110101101001001001000010000111100000101111001111001100000100100011010111100110011101000101011110101000110001100101010100110000110111001001010001000111010110100000010010000001010000000000000000000000000000000000000011
//1000100000000001011010100101010000101101011101000110111001101010101000000011101111100011001100110010111010101010000111001111110110101001100010000010111010101001110111001010000000111011110110110001010100001000001000010001000110011100100001001111000101101100110110111100100000000101110110001111011100100010000111111001010101011110101110100110100110101000001101100010000100011100011111001001010001100100000010010001000100010101110000100000101111010010101011011001010010111011111000101100110010000101111001101101000111110100010001010011100110001010000010100010101010110110100000101110101000111011011001011100011010100001100100100000100101011000101100111110101110001101111011001000011100100101100110100110110110001110011100101000110000100010010011011111100101100100010000110001111010100000010000000000000000000000000000000000000000011
