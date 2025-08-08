package org.ton.kotlin.adnl

interface AdnlAddressResolver {
    suspend fun resolveAddress(adnlIdShort: AdnlIdShort): AdnlNode?
}
