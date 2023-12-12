package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.bitstring.BitString
import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.hashmap.HmEdge
import org.ton.tlb.*
import org.ton.tlb.constructor.tlbCodec
import org.ton.tlb.providers.TlbConstructorProvider

@Serializable
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
        storeBits(value.configAddr)
        storeRef(hashmap, value.config)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): ConfigParams = cellSlice {
        val configAddr = loadBits(256)
        val config = loadRef(hashmap)
        ConfigParams(configAddr, config)
    }
}
