package org.ton.kotlin.adnl

public fun interface AdnlNodeResolver {
    public suspend fun resolveAdnlNode(adnlIdFull: AdnlIdFull): AdnlNode? = resolveAdnlNode(adnlIdFull.shortId)

    public suspend fun resolveAdnlNode(adnlIdShort: AdnlIdShort): AdnlNode?
}
