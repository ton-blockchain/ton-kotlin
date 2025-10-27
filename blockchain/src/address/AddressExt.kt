package org.ton.sdk.blockchain.address

import org.ton.sdk.bitstring.BitString
import kotlin.jvm.JvmName

/**
 * External message address (MsgAddressExt) container.
 *
 * This is a thin wrapper around a BitString that holds the serialized external address bits.
 * Its exact structure is defined in TL‑B and may vary; higher-level code is expected to interpret it.
 *
 * @property bits Serialized address bits.
 */
public class AddressExt(
    @get:JvmName("bits")
    public val bits: BitString
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AddressExt

        return bits == other.bits
    }

    override fun hashCode(): Int {
        return bits.hashCode()
    }

    override fun toString(): String = "AddressExt($bits)"
}
