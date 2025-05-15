package org.ton.dht

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import org.ton.kotlin.adnl.adnl.AdnlIdShort
import org.ton.kotlin.adnl.dht.config.DhtConfigGlobal
import org.ton.dht.storage.DhtStorage
import kotlin.coroutines.CoroutineContext

class DhtMember(
    val id: AdnlIdShort,
    val storage: DhtStorage,
    val config: DhtConfigGlobal,
    coroutineContext: CoroutineContext
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = coroutineContext + CoroutineName(toString())

    override fun toString(): String = "[dhtnode $id]"

    companion object {
        const val DEFAULT_K = 10
        const val DEFAULT_A = 3
        const val MAX_K = 10
        const val MAX_A = 10
    }
}
