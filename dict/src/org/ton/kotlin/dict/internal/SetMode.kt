package org.ton.kotlin.dict.internal

internal enum class SetMode(
    val mask: Int
) {
    /**
     * Sets the value associated with the key in the dictionary.
     */
    Set(0b11),

    /**
     * Sets the value associated with the key in the dictionary only if the key was already present in it.
     */
    Replace(0b01),

    /**
     * Sets the value associated with key in dictionary, but only if it is not already present.
     */
    Add(0b10);

    fun canReplace(): Boolean = mask and 0b01 != 0

    fun canAdd(): Boolean = mask and 0b10 != 0
}
