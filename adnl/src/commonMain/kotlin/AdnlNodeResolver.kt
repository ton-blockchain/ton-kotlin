package org.ton.kotlin.adnl

fun interface AdnlNodeResolver {
    suspend fun resolveAdnlNode(adnlIdFull: AdnlIdFull): AdnlNode? = resolveAdnlNode(adnlIdFull.shortId)
    suspend fun resolveAdnlNode(adnlIdShort: AdnlIdShort): AdnlNode?
}
