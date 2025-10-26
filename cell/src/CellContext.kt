package org.ton.sdk.cell

import org.ton.sdk.cell.internal.EmptyCellContext
import kotlin.jvm.JvmStatic

/**
 * Defines the operational context for working with TVM Cells.
 *
 * A `CellContext` provides the necessary environment for performing
 * operations such as loading and finalizing cells. It is typically used
 * during cell serialization/deserialization processes, or by the
 * TON Virtual Machine (TVM) for gas accounting and cell I/O operations.
 */
public interface CellContext {

    /**
     * Loads the specified [cell] and returns it as a [LoadedCell] instance.
     *
     * This function is used in various contexts such as:
     * - within the TVM for cell-based gas computations,
     * - when deserializing cells from external I/O sources (e.g., libraries or
     *   large BagOfCells instances containing blockchain state data).
     *
     * @param cell The [Cell] instance to be loaded.
     * @return A [LoadedCell] representing the fully loaded cell.
     */
    public suspend fun loadCell(cell: Cell): LoadedCell

    /**
     * Finalizes the construction of a [Cell] from the provided [CellBuilder].
     *
     * This is a convenience method equivalent to calling
     * [finalizeCell] with `isExotic = false`.
     *
     * @param builder The [CellBuilder] used to construct the cell.
     * @return The finalized [Cell] instance.
     */
    public fun finalizeCell(builder: CellBuilder): Cell =
        finalizeCell(builder, isExotic = false)

    /**
     * Completes the construction of a [Cell] using the given [CellBuilder],
     * optionally marking the resulting cell as *exotic*.
     *
     * Exotic cells are used to represent special cell types such as
     * reference or library cells (see TON Blockchain whitepaper §1.1.5).
     *
     * @param builder The [CellBuilder] instance used to build the cell.
     * @param isExotic Whether the resulting cell should be marked as exotic.
     *                 Defaults to `false`.
     * @return The constructed [Cell] instance.
     */
    public fun finalizeCell(builder: CellBuilder, isExotic: Boolean = false): Cell =
        builder.build(isExotic = isExotic)

    /**
     * Companion object providing predefined [CellContext] instances.
     */
    public companion object {
        /**
         * A singleton empty [CellContext] implementation.
         *
         * Used as a default or placeholder where no specific
         * context behavior is required.
         */
        @JvmStatic
        public val EMPTY: CellContext = EmptyCellContext
    }
}
