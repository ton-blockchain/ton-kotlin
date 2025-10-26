@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.cell

import org.ton.cell.Cell

public interface LoadedCell : Cell {
    public val references: List<Cell>
}
