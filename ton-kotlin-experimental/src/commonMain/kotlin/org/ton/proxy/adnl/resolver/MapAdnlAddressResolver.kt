package org.ton.proxy.adnl.resolver

import org.ton.kotlin.adnl.adnl.AdnlAddress
import org.ton.kotlin.adnl.adnl.AdnlIdShort
import org.ton.kotlin.adnl.pub.PublicKey

class MapAdnlAddressResolver(
    private val map: Map<AdnlIdShort, Pair<PublicKey, List<AdnlAddress>>>
) : AdnlAddressResolver {
    override suspend fun resolve(address: AdnlIdShort): Pair<PublicKey, List<AdnlAddress>>? = map[address]
}
