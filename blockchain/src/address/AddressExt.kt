package org.ton.sdk.blockchain.address

import org.ton.bitstring.BitString

/**
 * External message address (MsgAddressExt) container.
 *
 * This is a thin wrapper around a BitString that holds the serialized external address bits.
 * Its exact structure is defined in TL‑B and may vary; higher-level code is expected to interpret it.
 *
 * @property bits Serialized address bits.
 */
public class AddressExt(
    public val bits: BitString
)
