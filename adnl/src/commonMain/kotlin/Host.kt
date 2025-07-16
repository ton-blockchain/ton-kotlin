package org.ton.kotlin.adnl

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import org.ton.kotlin.crypto.PrivateKey
import kotlin.coroutines.CoroutineContext

class Host(
    val localKey: PrivateKey,
    val network: Network,
) : CoroutineScope {
    override val coroutineContext: CoroutineContext = network.coroutineContext + CoroutineName("Host")
    val localId: AdnlIdShort get() = AdnlIdShort(localKey.publicKey())


}
