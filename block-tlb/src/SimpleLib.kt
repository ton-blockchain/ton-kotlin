package org.ton.block

import org.ton.cell.Cell
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbCodec
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbObject
import org.ton.tlb.TlbPrettyPrinter
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
        builder: CellBuilder, value: SimpleLib
    ) = builder {
        storeBit(value.public)
        storeRef(value.root)
    }

    override fun loadTlb(
        slice: CellSlice
    ): SimpleLib = slice {
        val public = loadBoolean()
        val root = loadRef()
        SimpleLib(public, root)
    }
}
