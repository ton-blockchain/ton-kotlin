package org.ton.contract.wallet

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.ton.block.AddrStd
import org.ton.block.Coins
import org.ton.kotlin.account.Account
import org.ton.sdk.crypto.PrivateKeyEd25519
import org.ton.sdk.crypto.sha256
import kotlin.test.Ignore
import kotlin.test.Test

@Ignore
class WalletV4Example {
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
        val account = accountState.account.load() as? Account
        if (account == null) {
            println("Account $testnetNonBounceAddr not initialized")
            return@runBlocking
        }

        val balance = account.balance.coins
        println("Account balance: $balance toncoins")

        contract.transfer(pk) {
            coins = Coins.ofNano(100) // 100 nanoton
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
            } catch (e: Exception) {
                null
            }

            println("Message text: $rawMessageText")
        }
    }
}
