package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.bigint.toInt
import org.ton.bitstring.BitString
import org.ton.bitstring.toBitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.kotlin.cell.CellSize
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor
import kotlin.jvm.JvmStatic


@SerialName("addr_var")
public data class AddrVar(
    val anycast: Maybe<Anycast>,
    @SerialName("addr_len") val addrLen: Int,
    @SerialName("workchain_id") override val workchainId: Int,
    override val address: BitString
) : MsgAddressInt {
    init {
        require(address.size == addrLen) { "required: address.size == addr_len, actual: ${address.size}" }
    }

    public constructor(workchainId: Int, address: ByteArray) : this(null, workchainId, address)
    public constructor(workchainId: Int, address: BitString) : this(null, workchainId, address)
    public constructor(anycast: Anycast?, workchainId: Int, address: BitString) : this(
        anycast.toMaybe(),
        address.size,
        workchainId,
        address
    )

    public constructor(anycast: Anycast?, workchainId: Int, address: ByteArray) : this(
        anycast.toMaybe(),
        address.size,
        workchainId,
        address.toBitString()
    )

    override val cellSize: CellSize
        get() = CellSize(2 + 1 + 9 + 32 + addrLen, 0).let { cellSize ->
            anycast.value?.let { anycast -> anycast.cellSize + cellSize } ?: cellSize
        }

    override fun toAddrStd(): AddrStd = AddrStd(workchainId, address)

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type("addr_var") {
            field("anycast", anycast)
            field("addr_len", addrLen)
            field("workchain_id", workchainId)
            field("address", address)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbCodec<AddrVar> by AddrVarTlbConstructor {
        @JvmStatic
        public fun tlbCodec(): TlbConstructor<AddrVar> = AddrVarTlbConstructor
    }
}

private object AddrVarTlbConstructor : TlbConstructor<AddrVar>(
    schema = "addr_var\$11 anycast:(Maybe Anycast) addr_len:(## 9) workchain_id:int32 address:(bits addr_len) = MsgAddressInt;"
) {
    private val MaybeAnycast = Maybe(Anycast)

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: AddrVar
    ) = cellBuilder {
        storeTlb(MaybeAnycast, value.anycast)
        storeUInt(value.addrLen, 9)
        storeInt(value.workchainId, 32)
        storeBits(value.address)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): AddrVar = cellSlice {
        val anycast = loadTlb(MaybeAnycast)
        val addrLen = loadUInt(9).toInt()
        val workchainId = loadInt(32).toInt()
        val address = loadBits(addrLen)
        AddrVar(anycast, addrLen, workchainId, address)
    }
}
