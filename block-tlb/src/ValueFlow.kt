package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.*
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor


@SerialName("value_flow")
public data class ValueFlow(
    @SerialName("from_prev_blk") val fromPrevBlk: CurrencyCollection,
    @SerialName("to_next_blk") val toNextBlk: CurrencyCollection,
    val imported: CurrencyCollection,
    val exported: CurrencyCollection,
    @SerialName("fees_collected") val feesCollected: CurrencyCollection,
    @SerialName("fees_imported") val feesImported: CurrencyCollection,
    val recovered: CurrencyCollection,
    val created: CurrencyCollection,
    val minted: CurrencyCollection
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return printer.type("value_flow") {
            field("from_prev_blk", fromPrevBlk)
            field("to_next_blk", toNextBlk)
            field("imported", imported)
            field("exported", exported)
            field("fees_collected", feesCollected)
            field("fees_imported", feesImported)
            field("recovered", recovered)
            field("created", created)
            field("minted", minted)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbCodec<ValueFlow> by ValueFlowTlbConstructor.asTlbCombinator()
}

private object ValueFlowTlbConstructor : TlbConstructor<ValueFlow>(
    schema = "value_flow#b8e48dfb ^[ from_prev_blk:CurrencyCollection " +
            "to_next_blk:CurrencyCollection " +
            "imported:CurrencyCollection " +
            "exported:CurrencyCollection ] " +
            "fees_collected:CurrencyCollection " +
            "^[ " +
            "fees_imported:CurrencyCollection " +
            "recovered:CurrencyCollection " +
            "created:CurrencyCollection " +
            "minted:CurrencyCollection " +
            "] = ValueFlow;"
) {
    override fun storeTlb(
        builder: CellBuilder, value: ValueFlow
    ) = builder {
        storeRef {
            storeTlb(CurrencyCollection, value.fromPrevBlk)
            storeTlb(CurrencyCollection, value.toNextBlk)
            storeTlb(CurrencyCollection, value.imported)
            storeTlb(CurrencyCollection, value.exported)
        }
        storeTlb(CurrencyCollection, value.feesCollected)
        storeRef {
            storeTlb(CurrencyCollection, value.feesImported)
            storeTlb(CurrencyCollection, value.recovered)
            storeTlb(CurrencyCollection, value.created)
            storeTlb(CurrencyCollection, value.minted)
        }
    }

    override fun loadTlb(
        slice: CellSlice
    ): ValueFlow = slice {
        val (fromPrevBlk, toNextBlk, imported, exported) = loadRef {
            arrayOf(
                loadTlb(CurrencyCollection),
                loadTlb(CurrencyCollection),
                loadTlb(CurrencyCollection),
                loadTlb(CurrencyCollection),
            )
        }
        val feesCollected = loadTlb(CurrencyCollection)
        val (feesImported, recovered, created, minted) = loadRef {
            arrayOf(
                loadTlb(CurrencyCollection),
                loadTlb(CurrencyCollection),
                loadTlb(CurrencyCollection),
                loadTlb(CurrencyCollection),
            )
        }
        ValueFlow(fromPrevBlk, toNextBlk, imported, exported, feesCollected, feesImported, recovered, created, minted)
    }
}
