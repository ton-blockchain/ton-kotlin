package org.ton.contract.wallet

import io.github.andreypfau.kotlinx.crypto.sha2.sha256
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.ton.api.pk.PrivateKeyEd25519
import org.ton.block.AddrStd
import org.ton.block.Coins
import org.ton.block.MsgAddressInt
import org.ton.cell.CellBuilder
import org.ton.contract.jetton.JettonTransfer
import org.ton.kotlin.account.Account
import org.ton.kotlin.message.MessageLayout
import org.ton.tlb.storeTlb
import kotlin.test.Test

class JettonTransferExample {
    @Test
    fun walletV4Example(): Unit = runBlocking {
        val liteClient = liteClientTestnet()

        val pk = PrivateKeyEd25519(sha256("example-key".encodeToByteArray()))

        val contract = WalletV4R2Contract(
            liteClient,
            WalletV4R2Contract.address(pk)
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

        val toAddress = AddrStd("0QBFbLhcjyVqeLgYgNxroeXr5eaNXe4l4l3ekU4xLS57-cER");

        val jettonData = JettonTransfer(
            queryId = ULongRange(ULong.MIN_VALUE, ULong.MAX_VALUE).random(),
            amount = Coins.ofNano(1),
            toAddress = toAddress,
            responseAddress = contract.address,
            forwardAmount = Coins.ZERO,
            forwardPayload = null,
            customPayload = null
        )

        val transferDataCell = CellBuilder().storeTlb(JettonTransfer, jettonData).endCell()

        contract.transfer(pk) {
            destination = MsgAddressInt.parse("kQDdAW8SkFA9Zplv-v6ysn_n4TboKLMSohBi7iipZJ3flHff")   // Jetton wallet address
            coins = Coins.ofNano(40000000)                                                          // Fee
            messageData = MessageData.Raw(transferDataCell, null, MessageLayout.PLAIN)
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
