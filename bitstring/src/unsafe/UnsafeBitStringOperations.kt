package org.ton.sdk.bitstring.unsafe

import org.ton.sdk.bitstring.BitString
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract

@UnsafeBitStringApi
public object UnsafeBitStringOperations {
    public fun wrapUnsafe(array: ByteArray, bitLength: Int): BitString = BitString(array, bitLength, 0)

    /**
     * Applies [block] to a reference to the underlying array.
     *
     * This method invokes [block] on a reference to the underlying array, not to its copy.
     * Consider using [BitString.toByteArray] if it's impossible to guarantee that the array won't be modified.
     */
    public fun withByteArrayUnsafe(bitString: BitString, block: (ByteArray) -> Unit) {
        contract {
            callsInPlace(block, EXACTLY_ONCE)
        }
        block(bitString.getBackingArrayReference())
    }
}
