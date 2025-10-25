package org.ton.sdk.cell

import org.ton.sdk.cell.internal.EmptyCellContext
import kotlin.jvm.JvmStatic

public interface CellContext {
    public fun loadCell(cell: Cell): LoadedCell

    public fun finalizeCell(builder: CellBuilder): Cell

    public companion object {
        @JvmStatic
        public val EMPTY: CellContext = EmptyCellContext
    }
}
