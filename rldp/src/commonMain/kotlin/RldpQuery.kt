package org.ton.kotlin.rldp

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString

class RldpQuery(
    val connection: RldpConnection,
    val transferId: ByteString,
    val query: RldpMessage.Query
) {
    val data = query.data

    suspend fun response(data: ByteString) {
        connection.sendAnswer(transferId, RldpMessage.Answer(query.queryId, data))
    }

    override fun toString(): String {
        return "RldpQuery(remoteId=${connection.adnl.remoteNode.id}, id=${transferId.toHexString()}, query=$query)"
    }
}
