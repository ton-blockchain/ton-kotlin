package org.ton.proxy.adnl.resolver

import io.github.reactivecircus.cache4k.Cache
import org.ton.kotlin.adnl.adnl.AdnlAddress
import org.ton.kotlin.adnl.adnl.AdnlIdShort
import org.ton.kotlin.adnl.pub.PublicKey
import kotlin.time.Duration

class CachedAdnlAddressResolver(
    private val delegate: AdnlAddressResolver,
    ttl: Duration
) : AdnlAddressResolver {
    val cache = Cache.Builder()
        .expireAfterWrite(ttl)
        .build<AdnlIdShort, Pair<PublicKey, List<AdnlAddress>>>()

    override suspend fun resolve(address: AdnlIdShort): Pair<PublicKey, List<AdnlAddress>>? {
        val cached = cache.get(address)
        if (cached != null) return cached
        val resolved = delegate.resolve(address)
        if (resolved != null) {
            cache.put(address, resolved)
        }
        return resolved
    }
}
