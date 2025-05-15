package org.ton.kotlin.examples.faucet

import org.ton.bigint.BigInt
import org.ton.contract.wallet.MessageData
import org.ton.contract.wallet.WalletTransfer
import org.ton.kotlin.account.balance
import org.ton.kotlin.adnl.pk.PrivateKeyEd25519
import org.ton.kotlin.block.Coins
import org.ton.kotlin.block.MsgAddressInt
import org.ton.kotlin.examples.contract.WalletV1R3Contract
import org.ton.kotlin.examples.provider.Provider

class TestnetFaucet(
    val provider: Provider
) {
    @OptIn(ExperimentalStdlibApi::class)
    private val SECRET =
        PrivateKeyEd25519("46aab91daaaa375d40588384fdf7e36c62d0c0f38c46adfea7f9c904c5973d97c02ece00eceb299066597ccc7a8ac0b2d08f0ad425f28c0ea92e74e2064f41f0".hexToByteArray())
    val wallet = WalletV1R3Contract(0, SECRET.publicKey(), provider)

    suspend fun topUpContract(destination: MsgAddressInt, amount: BigInt) =
        topUpContract(destination, Coins(amount))

    suspend fun topUpContract(destination: MsgAddressInt, amount: Long) =
        topUpContract(destination, Coins(amount))

    suspend fun topUpContract(destination: MsgAddressInt, amount: Coins) {
        var account = provider.getAccount(wallet.address)?.loadAccount()
        val seqno = account?.let { wallet.getSeqno(it) } ?: 0
        println("faucet balance: ${account?.balance}")
        wallet.transfer(SECRET, seqno, WalletTransfer {
            this.destination = destination
            this.coins = amount
            this.bounceable = false
            this.messageData = MessageData.text("github.com/ton-community/ton-kotlin")
        })
        println("Waiting for faucet new balance...")
        account = wallet.waitForAccountChange(account)
        println("New faucet balance: ${account.balance}")
    }
}
