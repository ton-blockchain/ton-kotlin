package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.Cell
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.hashmap.HmEdge
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.constructor.tlbCodec
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


public data class ConfigParams(
    @SerialName("config_addr") val configAddr: BitString,
    val config: CellRef<HmEdge<Cell>>
) : TlbObject {
    init {
        require(configAddr.size == 256)
    }

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type {
        field("config_addr", configAddr)
        field("config", config)
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<ConfigParams> by ConfigParamsTlbConstructor
}

private object ConfigParamsTlbConstructor : TlbConstructor<ConfigParams>(
    schema = "_ config_addr:bits256 config:^(Hashmap 32 ^Cell) = ConfigParams;"
) {
    val hashmap = HmEdge.tlbCodec(32, Cell.tlbCodec())

    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: ConfigParams
    ) = cellBuilder {
        storeBitString(value.configAddr)
        storeRef(hashmap, value.config)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): ConfigParams = cellSlice {
        val configAddr = loadBitString(256)
        val config = loadRef(hashmap)
        ConfigParams(configAddr, config)
    }
}
