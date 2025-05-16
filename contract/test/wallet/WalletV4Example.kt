package org.ton.kotlin.contract.wallet

import io.github.andreypfau.kotlinx.crypto.sha256
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.ton.kotlin.api.pk.PrivateKeyEd25519
import org.ton.kotlin.block.AddrStd
import org.ton.kotlin.block.Coins
import kotlin.test.Test

class WalletV4Example {
    //    @Ignore
    @Test
    fun walletExample(): Unit = runBlocking {
        val liteClient = liteClientTestnet()

        val pk = PrivateKeyEd25519(sha256("example-key".encodeToByteArray()))
        val contract = WalletV4R2Contract(
            liteClient,
            WalletV4R2Contract.address(pk)
        )
        val testnetNonBounceAddr = contract.address.toString(userFriendly = true, testOnly = true, bounceable = false)
        println("Wallet Address: $testnetNonBounceAddr")

        var accountState = liteClient.getAccountState(contract.address)
        val account = accountState.account.load()
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
            .first().transaction.load()
        println("Transaction: $lastTransactionId")

        transaction.outMsgs.forEach { (hash, outMsgCell) ->
            val outMsgBody = outMsgCell.load().body.let {
                requireNotNull(it.x ?: it.y?.load()) { "Body for message $hash is empty!" }
            }

            val rawMessageText = try {
                MessageText.loadTlb(outMsgBody)
            } catch (_: Exception) {
                null
            }

            println("Message text: $rawMessageText")
        }
    }
}
