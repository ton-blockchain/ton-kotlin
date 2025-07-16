package org.ton.kotlin.adnl

interface PeerTable {
    suspend fun getInfo(localId: AdnlIdShort, remoteId: AdnlIdShort): AdnlNode?

    suspend fun setInfo(localId: AdnlIdShort, remoteId: AdnlIdShort, info: AdnlNode)
}

class PeerTableMemory(
    val table: MutableMap<Pair<AdnlIdShort, AdnlIdShort>, AdnlNode> = mutableMapOf()
) : PeerTable {
    override suspend fun getInfo(localId: AdnlIdShort, remoteId: AdnlIdShort): AdnlNode? {
        return table[localId to remoteId]
    }

    override suspend fun setInfo(localId: AdnlIdShort, remoteId: AdnlIdShort, info: AdnlNode) {
        table[localId to remoteId] = info
    }
}
