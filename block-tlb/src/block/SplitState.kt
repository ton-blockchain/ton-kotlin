package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.*
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.TlbPrettyPrinter
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.tlb.storeTlb


@SerialName("split_state")
public data class SplitState(
    val left: ShardStateUnsplit,
    val right: ShardStateUnsplit
) : ShardState {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("split_state") {
        field("left", left)
        field("right", right)
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<SplitState> by SplitStateTlbConstructor
}

private object SplitStateTlbConstructor : TlbConstructor<SplitState>(
    schema = "split_state#5f327da5 left:^ShardStateUnsplit right:^ShardStateUnsplit = ShardState;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: SplitState
    ) = cellBuilder {
        storeRef {
            storeTlb(ShardStateUnsplit, value.left)
        }
        storeRef {
            storeTlb(ShardStateUnsplit, value.right)
        }
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): SplitState = cellSlice {
        val left = loadRef {
            loadTlb(ShardStateUnsplit)
        }
        val right = loadRef {
            loadTlb(ShardStateUnsplit)
        }
        SplitState(left, right)
    }
}
