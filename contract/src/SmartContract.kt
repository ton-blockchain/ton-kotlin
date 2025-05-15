package org.ton.kotlin.contract

import org.ton.kotlin.block.AddrStd
import org.ton.kotlin.lite.client.LiteClient

public interface SmartContract {
    public val liteClient: LiteClient
    public val address: AddrStd
}
