@file:Suppress("OPT_IN_USAGE")

package org.ton.block

import kotlinx.serialization.SerialName
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbCombinator
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbObject
import org.ton.tlb.TlbPrettyPrinter
import org.ton.tlb.providers.TlbCombinatorProvider
import org.ton.tlb.providers.TlbConstructorProvider


@JsonClassDiscriminator("@type")
public sealed interface FutureSplitMerge : TlbObject {

    @SerialName("fsm_none")
    public object FsmNone : FutureSplitMerge,
        TlbConstructorProvider<FsmNone> by FutureSplitMergeNoneTlbConstructor {
        override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("fsm_none")

        override fun toString(): String = print().toString()
    }


    @SerialName("fsm_merge")
    public data class FsmMerge(
        val mergeUtime: UInt,
        val interval: UInt
    ) : FutureSplitMerge {
        override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
            type("fsm_merge") {
                field("merge_utime", mergeUtime)
                field("interval", interval)
            }
        }

        override fun toString(): String = print().toString()

        public companion object : TlbConstructorProvider<FsmMerge> by FutureSplitMergeMergeTlbConstructor
    }


    @SerialName("fsm_split")
    public data class FsmSplit(
        val splitUtime: UInt,
        val interval: UInt
    ) : FutureSplitMerge {
        override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
            type("fsm_split") {
                field("split_utime", splitUtime)
                field("interval", interval)
            }
        }

        override fun toString(): String = print().toString()

        public companion object : TlbConstructorProvider<FsmSplit> by FsmSplitTlbConstructor
    }

    public companion object : TlbCombinatorProvider<FutureSplitMerge> by FutureSplitMergeTlbCombinator
}

private object FutureSplitMergeTlbCombinator : TlbCombinator<FutureSplitMerge>(
    FutureSplitMerge::class,
    FutureSplitMerge.FsmNone::class to FutureSplitMerge.FsmNone,
    FutureSplitMerge.FsmSplit::class to FutureSplitMerge.FsmSplit,
    FutureSplitMerge.FsmMerge::class to FutureSplitMerge.FsmMerge,
)

private object FutureSplitMergeNoneTlbConstructor : TlbConstructor<FutureSplitMerge.FsmNone>(
    schema = "fsm_none\$0 = FutureSplitMerge;"
) {
    override fun storeTlb(builder: CellBuilder, value: FutureSplitMerge.FsmNone) = Unit
    override fun loadTlb(slice: CellSlice): FutureSplitMerge.FsmNone = FutureSplitMerge.FsmNone
}

private object FsmSplitTlbConstructor : TlbConstructor<FutureSplitMerge.FsmSplit>(
    schema = "fsm_split\$10 split_utime:uint32 interval:uint32 = FutureSplitMerge;"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: FutureSplitMerge.FsmSplit
    ) = builder {
        storeUInt32(value.splitUtime)
        storeUInt32(value.interval)
    }

    override fun loadTlb(
        slice: CellSlice
    ): FutureSplitMerge.FsmSplit = slice {
        val splitUtime = loadUInt32()
        val interval = loadUInt32()
        FutureSplitMerge.FsmSplit(splitUtime, interval)
    }
}


private object FutureSplitMergeMergeTlbConstructor : TlbConstructor<FutureSplitMerge.FsmMerge>(
    schema = "fsm_merge\$11 merge_utime:uint32 interval:uint32 = FutureSplitMerge;"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: FutureSplitMerge.FsmMerge
    ) = builder {
        storeUInt32(value.mergeUtime)
        storeUInt32(value.interval)
    }

    override fun loadTlb(
        slice: CellSlice
    ): FutureSplitMerge.FsmMerge = slice {
        val mergeUtime = loadUInt32()
        val interval = loadUInt32()
        FutureSplitMerge.FsmMerge(mergeUtime, interval)
    }
}
