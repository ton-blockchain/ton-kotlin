package org.ton.adnl.resolver

import org.ton.api.adnl.AdnlAddress
import org.ton.api.adnl.AdnlIdShort
import org.ton.api.pub.PublicKey

public class MapAdnlAddressResolver(
    private val map: Map<AdnlIdShort, Pair<PublicKey, List<AdnlAddress>>>
) : AdnlAddressResolver {
    override suspend fun resolve(address: AdnlIdShort): Pair<PublicKey, List<AdnlAddress>>? = map[address]
}
