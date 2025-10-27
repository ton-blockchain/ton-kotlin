@file:Suppress("OPT_IN_USAGE")

package org.ton.sdk.example

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.ton.sdk.bigint.toBigInt
import org.ton.sdk.blockchain.address.AddressStd
import org.ton.sdk.toncenter.client.TonCenterV3Client
import org.ton.sdk.toncenter.client.transactions
import org.ton.sdk.toncenter.model.TonCenterTransactionsResponse

object GetTransactionExample {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        // create TON Center v3 http client
        val client: TonCenterV3Client = TonCenterV3Client.create()

        // Get 10 last transactions for account
        val response: Deferred<TonCenterTransactionsResponse> = GlobalScope.async {
            client.transactions {
                account += AddressStd.parse("UQAKtVj024T9MfYaJzU1xnDAkf_GGbHNu-V2mgvyjTuP6uYH")
                limit = 10
            }
        }

        // Print transactions info and balance after transaction
        response.await().transactions.forEach { transaction ->
            val balance = transaction.accountStateAfter.balance
            val value = balance?.value ?: 0.toBigInt()
            println("hash=${transaction.hash} lt=${transaction.lt} balance=$value")
        }
    }
}
