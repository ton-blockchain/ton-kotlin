package org.ton.contract.wallet

import org.ton.api.pk.PrivateKeyEd25519
import org.ton.contract.SmartContract
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

public interface WalletContract : SmartContract {
    public companion object {
        public const val DEFAULT_WALLET_ID: Int =
            698983191 // First 4 bytes of zero state root hash of mainnet (as int32 in Little Endian)
    }

    public suspend fun transfer(privateKey: PrivateKeyEd25519, transfer: WalletTransfer)
}

public suspend inline fun WalletContract.transfer(
    privateKey: PrivateKeyEd25519,
    transfer: WalletTransferBuilder.() -> Unit
) {
    contract { callsInPlace(transfer, InvocationKind.EXACTLY_ONCE) }
    transfer(privateKey, WalletTransferBuilder().apply(transfer).build())
}
