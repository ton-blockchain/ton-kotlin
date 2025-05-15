package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


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
        cellBuilder: CellBuilder,
        value: SigPubKey
    ) = cellBuilder {
        storeBits(value.pubkey)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): SigPubKey = cellSlice {
        val pubkey = loadBits(256)
        SigPubKey(pubkey)
    }
}
