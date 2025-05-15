package org.ton.kotlin.examples

import org.ton.kotlin.block.AccountActive
import org.ton.kotlin.examples.contract.config.ConfigContract
import org.ton.kotlin.examples.contract.config.ConfigData
import org.ton.kotlin.examples.provider.LiteClientProvider
import org.ton.kotlin.examples.provider.liteClientMainnet

private val provider = LiteClientProvider(liteClientMainnet())

suspend fun main() {
    val configContract = ConfigContract(provider)

    val account = configContract.getState()?.loadAccount() ?: error("cant load account not found")
    val data = (account.state as AccountActive).value.data.value?.cell ?: error("cant load data")
    val configData = ConfigData.loadTlb(data.beginParse())
    println("seqno: ${configData.seqno}")
    println("public key: ${configData.publicKey}")
    println("voteDict: ${configData.voteDict}")
    for (entry in configData.voteDict) {
        println("proposal: ${entry.key}")
        println(entry.value)
    }
}
