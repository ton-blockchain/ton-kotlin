package org.ton.kotlin.cell

import kotlin.jvm.JvmStatic

public enum class CellType(
    public val value: Int
) {
    /**
     * Contains up to 1023 bits of data and up to four cell references.
     */
    ORDINARY(-1),

    /**
     * May have any level `1 ≤ level ≤ 3`. It
     * contains exactly `8 + 256 * level` data bits:
     *
     * first an 8-bit integer equal to `1` (representing the cell’s type),
     * then its `level` higher hashes:
     *
     * `(Hash₁(cell), ... ,  Hash level (cell))`
     *
     * The `level` of a pruned branch cell may be called its de
     * _Brujn index_, because it determines the outer Merkle proof or Merkle
     * update during the construction of which the branch has been pruned.
     * An attempt to load a pruned branch cell usually leads to an exception.
     */
    PRUNED_BRANCH(1),

    /**
     * Always has level `0`, and contains `8+256` data bits,
     * including its 8-bit type integer `2` and the representation hash:
     *
     * `Hash(cell)`
     *
     * of the library cell being referred to.
     * When loaded, a library reference cell may be transparently replaced by the cell it refers to,
     * if found in the current library context.
     */
    LIBRARY_REFERENCE(2),

    /**
     * Has exactly one reference `c1` and level `0 ≤ level ≤ 3`,
     * which must be one less than the level of its only child `cell₁`:
     *
     * `Lvl(cell) = Max(Lvl(cell₁) - 1,0)`
     *
     * The 8 + 256 data bits of a Merkle proof cell contain its 8-bit type
     * integer `3`, followed by `Hash₁(cell₁)`
     * (assumed to be equal to `Hash(cell₁)` if `Lvl(cell₁) = 0`).
     *
     * The higher hashes `Hashᵢ(cell)` of `cell` are computed similarly
     * to the higher hashes of an ordinary cell, but with `Hashᵢ₊₁(cell₁)` used
     * instead of `Hashᵢ(cell₁)`. When loaded, a Merkle proof cell is replaced by `cell₁`.
     */
    MERKLE_PROOF(3),

    /**
     * Has two children `Cell₁` and `Cell₂`. Its level `0 ≤ level ≤ 3` is given by
     *
     * `Lvl(Cell) = max(Lvl(Cell₁) − 1, Lvl(Cell₂) − 1, 0)`
     *
     * A Merkle update behaves like a Merkle proof for both `cell₁` and `cell₂`,
     * and contains 8+256+256 data bits with `Hash₁(cell₁)` and `Hash₁(cell₂)`.
     * However, an extra requirement is that _all pruned branch cells `cell'`
     * that are descendants of `cell₂` and are bound by c must also be descendants of `cell₁`
     * When a Merkle update cell is loaded, it is replaced by `cell₂`
     */
    MERKLE_UPDATE(4);

    public val isExotic: Boolean get() = this != ORDINARY
    public val isMerkle: Boolean get() = this == MERKLE_PROOF || this == MERKLE_UPDATE
    public val isPruned: Boolean get() = this == PRUNED_BRANCH

    public companion object {
        private val values by lazy(LazyThreadSafetyMode.PUBLICATION) {
            values()
        }

        @JvmStatic
        public operator fun get(index: Int): CellType = values[index]
    }
}
