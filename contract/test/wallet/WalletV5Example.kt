package org.ton.contract.wallet

import io.github.andreypfau.kotlinx.crypto.sha2.sha256
import io.ktor.util.hex
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.block.AddrStd
import org.ton.block.Coins
import org.ton.kotlin.account.Account
import kotlin.test.Test

class WalletV5Example {
    @Test
    fun walletV5Example(): Unit = runBlocking {
        val liteClient = liteClientTestnet()

        val pk = PrivateKeyEd25519(sha256("example-key".encodeToByteArray()))

        val walletID = WalletId(0, 0, -3, 0)
        val contract = WalletV5R1Contract(
            liteClient,
            WalletV5R1Contract.address(pk, walletID),
            walletID,
        )
        val testnetNonBounceAddr =
            contract.address.toString(userFriendly = true, testOnly = true, bounceable = false)
        println("Wallet Address: $testnetNonBounceAddr")

        var accountState = liteClient.getAccountState(contract.address)
        val account = accountState.account.value as? Account
        if (account == null) {
            println("Account $testnetNonBounceAddr not initialized")
            return@runBlocking
        }

        val balance = account.storage.balance.coins
        println("Account balance: $balance toncoins")

        contract.transfer(pk) {
            coins = Coins.Companion.ofNano(100) // 100 nanoton
            destination = AddrStd("kf8ZzXwnCm23GeqkK8ekU0Dxzu_fiXqIYO48FElkd7rVnoix")
            messageData = MessageData.text("Hello, World!")
        }

        while (true) {
            println("Wait for transaction to be processed...")
            delay(6000)
            val newAccountState = liteClient.getAccountState(contract.address)
            if (newAccountState != accountState) {
                accountState = newAccountState
                println("Got new account state with last transaction: ${accountState.lastTransactionId}")
                break
            }
        }

        val lastTransactionId = accountState.lastTransactionId
        if (lastTransactionId == null) {
            println("No transactions found")
            return@runBlocking
        }

        val transaction = liteClient.getTransactions(accountState.address, lastTransactionId, 1)
            .first().transaction.value
        println("Transaction: $lastTransactionId")
    }
}