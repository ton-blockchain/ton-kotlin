package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.bitstring.BitString
import org.ton.bitstring.toBitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.crypto.crc16
import org.ton.crypto.encoding.base64
import org.ton.crypto.encoding.base64url
import org.ton.crypto.hex
import org.ton.tlb.*
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

public inline fun AddrStd(address: String): AddrStd = AddrStd.parse(address)

@Serializable
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

    init {
        require(address.size == 256) { "expected address.size == 256, actual: ${address.size}" }
    }

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
                    base64url(data)
                } else {
                    base64(data)
                }
            } else {
                "${address.workchainId}:${address.address.toHex()}"
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
                address = hex(address.substringAfter(':'))
            )
        }

        @JvmStatic
        public fun parseUserFriendly(address: String): AddrStd {
            val addressBytes = ByteArray(36)

            try {
                base64url(address).copyInto(addressBytes)
            } catch (e: Exception) {
                try {
                    base64(address).copyInto(addressBytes)
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
            var expectedChecksum = ((addressBytes[2 + 32].toInt() and 0xFF) shl 8) or (addressBytes[2 + 32 + 1].toInt() and 0xFF)

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
