package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.*
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbPrettyPrinter
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeTlb


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
        builder: CellBuilder,
        value: SplitState
    ) = builder {
        storeRef {
            storeTlb(ShardStateUnsplit, value.left)
        }
        storeRef {
            storeTlb(ShardStateUnsplit, value.right)
        }
    }

    override fun loadTlb(
        slice: CellSlice
    ): SplitState = slice {
        val left = loadRef {
            loadTlb(ShardStateUnsplit)
        }
        val right = loadRef {
            loadTlb(ShardStateUnsplit)
        }
        SplitState(left, right)
    }
}
