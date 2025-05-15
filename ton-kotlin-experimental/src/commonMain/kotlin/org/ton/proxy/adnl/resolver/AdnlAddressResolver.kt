package org.ton.proxy.adnl.resolver

import org.ton.kotlin.adnl.adnl.AdnlAddress
import org.ton.kotlin.adnl.adnl.AdnlAddressList
import org.ton.kotlin.adnl.adnl.AdnlIdShort
import org.ton.kotlin.adnl.pub.PublicKey

fun interface AdnlAddressResolver {
    suspend fun resolve(publicKey: PublicKey): List<AdnlAddress>? = resolve(publicKey.toAdnlIdShort())?.second
    suspend fun resolve(address: AdnlIdShort): Pair<PublicKey, List<AdnlAddress>>?
}
