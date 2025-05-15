package org.ton.kotlin.examples

import org.ton.contract.wallet.WalletTransferBuilder
import org.ton.contract.wallet.WalletV4R2Contract
import org.ton.kotlin.account.balance
import org.ton.kotlin.adnl.pk.PrivateKeyEd25519
import org.ton.kotlin.block.AddrStd
import org.ton.kotlin.block.Coins
import org.ton.kotlin.block.CurrencyCollection
import org.ton.kotlin.examples.faucet.TestnetFaucet
import org.ton.kotlin.examples.provider.LiteClientProvider
import org.ton.kotlin.examples.provider.liteClientTestnet
import kotlin.random.Random

private val provider = LiteClientProvider(liteClientTestnet())

suspend fun main() {
    val key = PrivateKeyEd25519(Random(42))
    val wallet = WalletV4R2Contract(provider.liteClient, WalletV4R2Contract.address(key))
    if (provider.getAccount(wallet.address)?.loadAccount().balance == CurrencyCollection.ZERO) {
        TestnetFaucet(provider).topUpContract(wallet.address, Coins.of(1))
    }
    val transfer = WalletTransferBuilder().apply {
        destination = AddrStd("0QCaLIqf03-qGREcf-Novb6H3UOXmIB1cLXxAJDxqrwe3rbP")
        currencyCollection = CurrencyCollection(Coins.ofNano(1))
        bounceable = false
    }

    val res = wallet.transfer(key, transfer.build())
    println(res)
}
