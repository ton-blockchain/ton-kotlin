package org.ton.kotlin.block

import org.ton.kotlin.cell.Cell
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbCodec
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.TlbObject
import org.ton.kotlin.tlb.TlbPrettyPrinter
import kotlin.jvm.JvmStatic


public data class SimpleLib(
    val public: Boolean,
    val root: Cell
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return printer {
            type("simple_lib") {
                field("public", public)
                field("root", root)
            }
        }
    }

    public companion object : TlbCodec<SimpleLib> by SimpleLibTlbConstructor {
        @JvmStatic
        public fun tlbCodec(): TlbConstructor<SimpleLib> = SimpleLibTlbConstructor
    }
}

private object SimpleLibTlbConstructor : TlbConstructor<SimpleLib>(
    schema = "simple_lib\$_ public:Bool root:^Cell = SimpleLib;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder, value: SimpleLib
    ) = cellBuilder {
        storeBit(value.public)
        storeRef(value.root)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): SimpleLib = cellSlice {
        val public = loadBit()
        val root = loadRef()
        SimpleLib(public, root)
    }
}
