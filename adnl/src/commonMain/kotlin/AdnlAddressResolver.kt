package org.ton.kotlin.adnl

fun interface AdnlAddressResolver {
    suspend fun resolveAddress(adnlIdShort: AdnlIdShort): AdnlNode?
}
