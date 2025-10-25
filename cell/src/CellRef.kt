package org.ton.sdk.cell

public interface CellRef<T> {
    public fun toCell(): Cell

    public fun load(): T

    public companion object {
        public fun <T> valueOf(value: T): CellRef<T> {
            TODO()
        }
    }
}
