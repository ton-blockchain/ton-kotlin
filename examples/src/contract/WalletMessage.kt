package org.ton.kotlin.examples.contract

import org.ton.contract.wallet.WalletTransfer
import org.ton.kotlin.adnl.pk.PrivateKey
import org.ton.kotlin.bitstring.BitString

interface WalletMessage {
    val seqno: Int
    val transfers: List<WalletTransfer>

    fun sign(privateKey: PrivateKey): SignedWalletMessage
}

interface SignedWalletMessage : WalletMessage {
    val signature: BitString
}
