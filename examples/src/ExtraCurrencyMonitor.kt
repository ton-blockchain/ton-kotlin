package org.ton.kotlin.examples

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.io.bytestring.ByteString
import org.ton.block.*
import org.ton.contract.wallet.MessageData
import org.ton.contract.wallet.WalletTransfer
import org.ton.kotlin.account.Account
import org.ton.kotlin.account.balance
import org.ton.sdk.crypto.PrivateKeyEd25519
import org.ton.kotlin.currency.VarUInt248
import org.ton.kotlin.examples.contract.WalletV1R3Contract
import org.ton.kotlin.examples.faucet.TestnetFaucet
import org.ton.kotlin.examples.provider.LiteClientProvider
import org.ton.kotlin.examples.provider.liteClientTestnet
import org.ton.kotlin.transaction.Transaction
import org.ton.kotlin.transaction.TransactionInfo
import org.ton.kotlin.transaction.phase.BouncePhase

private val provider = LiteClientProvider(liteClientTestnet())
private val swapAddress = AddrStd("kQC_rkxBuZDwS81yvMSLzeXBNLCGFNofm0avwlMfNXCwoOgr")
private val faucet = TestnetFaucet(provider)

suspend fun main(): Unit = coroutineScope {
    val key1 = PrivateKeyEd25519.random()
    val key2 = PrivateKeyEd25519.random()
    var (wallet1, state1) = deployWallet(key1)
    val (wallet2, _) = deployWallet(key2)
    println("requesting from $swapAddress ECHIDNA...")
    wallet1.transfer(
        key1,
        wallet1.getSeqno(state1),
        WalletTransfer {
            destination = swapAddress
            coins = Coins(3700000000)
        }
    )
    println("sent transfer message, waiting for extra currency...")
    var tx = wallet1.waitForExtraCurrency(100)
    println("wallet1 ${wallet1.address.toString(userFriendly = false)} received ${tx.inMsg?.load()?.info.value}")

    println("sending EC wallet1->wallet2...")
    state1 = requireNotNull(wallet1.getState()?.loadAccount())
    wallet1.transfer(key1, wallet1.getSeqno(state1), WalletTransfer {
        destination = wallet2.address
        currencyCollection = CurrencyCollection(mapOf(100 to VarUInt248(300000000)))
        messageData = MessageData.text("send EC wallet1->wallet2")
    })
    println("transfer sent, waiting for extra currency on wallet2...")
    tx = wallet2.waitForExtraCurrency(100)
    println("wallet2 received ${tx.inMsg?.load()?.info.value}")
}


suspend fun WalletV1R3Contract.waitForExtraCurrency(
    id: Int,
    fromTxLt: Long? = null,
    fromTxHash: ByteString? = null
): Transaction {
    while (true) {
        for (cellTx in provider.getTransactions(address, fromTxLt, fromTxHash)) {
            val tx = cellTx.load()
            (tx.inMsg?.load()?.info as? IntMsgInfo)?.value?.get(id) ?: continue
            val info = tx.loadInfo() as? TransactionInfo.Ordinary ?: continue
            if (info.bouncePhase !is BouncePhase.Executed) {
                return tx
            }
            throw IllegalStateException(
                "Extra currency transaction ${tx.lt}:${cellTx.hash().toHexString()} bounced: ${info.bouncePhase}"
            )
        }
        delay(2000)
    }
}

suspend fun deployWallet(key: PrivateKeyEd25519): Pair<WalletV1R3Contract, Account> {
    val wallet = WalletV1R3Contract(0, key.publicKey(), provider)
    println("Deploying wallet...")
    println("         raw address: ${wallet.address.toString(userFriendly = false)}")
    println("nonBounceableAddress: ${wallet.address.toString(userFriendly = true, bounceable = false)}")
    println("   bounceableAddress: ${wallet.address.toString(userFriendly = true, bounceable = true)}")
    var account = wallet.getState()?.loadAccount()
    println("top up from faucet...")
    val faucetRequest = Coins.of(4)
    faucet.topUpContract(wallet.address, faucetRequest)
    account = wallet.waitForAccountChange(account) { it.balance.coins == faucetRequest }
    println("topped up: ${account.balance}")
    println("sending state-init to self...")
    wallet.transfer(key, wallet.getSeqno(account), WalletTransfer {
        destination = wallet.address
    })
    account = requireNotNull(wallet.waitForAccountChange(account) { it?.isActive == true })
    println("wallet ${wallet.address.toString(userFriendly = false)} deployed!")
    return wallet to account
}
