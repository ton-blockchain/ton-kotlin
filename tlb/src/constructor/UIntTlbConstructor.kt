package org.ton.kotlin.tlb.constructor

import org.ton.kotlin.bigint.BigInt
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor

public open class UIntTlbConstructor(
    public val length: Int
) : TlbConstructor<BigInt>(
    schema = "uint\$_ = uint;",
    id = BitString.empty()
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: BigInt
    ) {
        cellBuilder.storeUInt(value, length)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): BigInt = cellSlice {
        loadUInt(length)
    }

    public companion object {
        public fun byte(length: Int = Byte.SIZE_BITS): TlbConstructor<UByte> =
            number(encode = { storeUInt(it.toByte(), length) }, decode = { loadUInt(length).toByte().toUByte() })

        public fun short(length: Int = Short.SIZE_BITS): TlbConstructor<UShort> =
            number(encode = { storeUInt(it.toShort(), length) }, decode = { loadUInt(length).toShort().toUShort() })

        public fun int(length: Int = Int.SIZE_BITS): TlbConstructor<UInt> =
            number(encode = { storeUInt(it.toInt(), length) }, decode = { loadUInt(length).toInt().toUInt() })

        public fun long(length: Int = Long.SIZE_BITS): TlbConstructor<ULong> =
            number(encode = { storeUInt(it.toLong(), length) }, decode = { loadUInt(length).toLong().toULong() })

        private fun <T : Any> number(encode: CellBuilder.(T) -> Unit, decode: CellSlice.() -> T) =
            object : TlbConstructor<T>("") {
                override fun storeTlb(
                    cellBuilder: CellBuilder,
                    value: T
                ) {
                    encode(cellBuilder, value)
                }

                override fun loadTlb(
                    cellSlice: CellSlice
                ): T {
                    return decode(cellSlice)
                }
            }
    }
}

public fun UInt.Companion.tlbConstructor(): TlbConstructor<UInt> = UIntTlbConstructor.int()
public fun ULong.Companion.tlbConstructor(): TlbConstructor<ULong> = UIntTlbConstructor.long()
