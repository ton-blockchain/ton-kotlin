package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.bitstring.BitString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbConstructor
import org.ton.tlb.providers.TlbConstructorProvider


@SerialName("ed25519_pubkey")
public data class SigPubKey(
    val pubkey: BitString
) {
    init {
        require(pubkey.size == 256) { "required: pubkey.size == 256, actual: ${pubkey.size}" }
    }

    public companion object : TlbConstructorProvider<SigPubKey> by SigPubKeyTlbConstructor
}

private object SigPubKeyTlbConstructor : TlbConstructor<SigPubKey>(
    schema = "ed25519_pubkey#8e81278a pubkey:bits256 = SigPubKey;"
) {
    override fun storeTlb(
        builder: CellBuilder,
        value: SigPubKey
    ) = builder {
        storeBitString(value.pubkey)
    }

    override fun loadTlb(
        slice: CellSlice
    ): SigPubKey = slice {
        val pubkey = loadBitString(256)
        SigPubKey(pubkey)
    }
}
