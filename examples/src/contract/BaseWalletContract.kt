package contract

import kotlinx.coroutines.delay
import org.ton.kotlin.account.Account
import org.ton.kotlin.account.ShardAccount
import org.ton.kotlin.adnl.pub.PublicKeyEd25519
import org.ton.kotlin.adnl.tonnode.TonNodeBlockIdExt
import org.ton.kotlin.block.AddrStd
import org.ton.kotlin.block.StateInit
import org.ton.kotlin.cell.Cell
import org.ton.kotlin.examples.provider.Provider

abstract class BaseWalletContract(
    val workchain: Int = DEFAULT_WORKCHAIN,
    val publicKey: PublicKeyEd25519,
    val provider: Provider
) {
    companion object {
        const val DEFAULT_WORKCHAIN = 0
        const val DEFAULT_WALLET_ID: Int = 698983191
    }

    val walletId = DEFAULT_WALLET_ID + workchain

    val stateInit: StateInit by lazy {
        StateInit(initCodeCell(), initDataCell())
    }

    val address: AddrStd get() = stateInit.address(workchain)

    abstract val maxMessages: Int

    abstract fun initDataCell(): Cell

    abstract fun initCodeCell(): Cell

    suspend fun getState(blockId: TonNodeBlockIdExt? = null): ShardAccount? = provider.getAccount(address, blockId)

    abstract fun getSeqno(accountInfo: Account?): Int

    suspend fun waitForShardAccountChange(shardAccount: ShardAccount?): ShardAccount? {
        while (true) {
            val newShardAccount = provider.getAccount(address)
            if (newShardAccount != shardAccount) {
                return newShardAccount
            }
            delay(2000)
        }
    }

    suspend fun waitForAccountChange(account: Account?, condition: (Account?) -> Boolean = { true }): Account? {
        while (true) {
            val newAccount = provider.getAccount(address)?.loadAccount()
            if (newAccount != account && condition(newAccount)) {
                return newAccount
            }
            delay(2000)
        }
    }
}
