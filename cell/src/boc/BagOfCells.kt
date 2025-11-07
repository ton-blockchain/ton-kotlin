package org.ton.sdk.cell.boc

import org.ton.sdk.cell.Cell

public abstract class BagOfCells {
    public abstract fun getRootCell(index: Int): Cell
}
