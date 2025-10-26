package org.ton.tlb.constructor

import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.sdk.bigint.BigInt
import org.ton.sdk.bigint.toLong
import org.ton.tlb.TlbConstructor

public class IntTlbConstructor(
    public val length: Int
) : TlbConstructor<BigInt>(
    schema = "int\$_ = int;"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: BigInt
    ): Unit = builder {
        storeInt(value, length)
    }

    override fun loadTlb(
        slice: CellSlice
    ): BigInt = slice {
        loadInt(length)
    }

    public companion object {
        public fun byte(length: Int = Byte.SIZE_BITS): TlbConstructor<Byte> =
            number(encode = { storeInt(it, length) }, decode = { loadInt(length).toLong().toByte() })

        public fun short(length: Int = Short.SIZE_BITS): TlbConstructor<Short> =
            number(encode = { storeInt(it, length) }, decode = { loadInt(length).toLong().toShort() })

        public fun int(length: Int = Int.SIZE_BITS): TlbConstructor<Int> =
            number(encode = { storeInt(it, length) }, decode = { loadInt(length).toLong().toInt() })

        public fun long(length: Int = Long.SIZE_BITS): TlbConstructor<Long> =
            number(encode = { storeLong(it, length) }, decode = { loadInt(length).toLong() })

        private fun <T : Number> number(encode: CellBuilder.(T) -> Unit, decode: CellSlice.() -> T): TlbConstructor<T> =
            object : TlbConstructor<T>("") {
                override fun storeTlb(
                    builder: CellBuilder,
                    value: T
                ) {
                    encode(builder, value)
                }

                override fun loadTlb(
                    slice: CellSlice
                ): T {
                    return decode(slice)
                }
            }
    }
}
