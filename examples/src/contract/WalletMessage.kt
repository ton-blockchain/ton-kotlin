package org.ton.kotlin.examples.contract

import org.ton.bitstring.BitString
import org.ton.contract.wallet.WalletTransfer
import org.ton.kotlin.crypto.Signer

interface WalletMessage {
    val seqno: Int
    val transfers: List<WalletTransfer>

    fun sign(signer: Signer): SignedWalletMessage
}

interface SignedWalletMessage : WalletMessage {
    val signature: BitString
}
