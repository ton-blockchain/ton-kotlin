package org.ton.contract

import org.ton.lite.client.LiteClient

public interface SmartContract {
    public val liteClient: LiteClient
    public val address: org.ton.kotlin.message.address.AddrStd
}
