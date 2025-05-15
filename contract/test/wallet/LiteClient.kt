package org.ton.kotlin.contract.wallet

import kotlinx.coroutines.Dispatchers
import org.ton.kotlin.lite.client.LiteClient

fun liteClientTestnet() = LiteClient(
    liteClientConfigGlobal = TESTNET_GLOBAL_CONFIG,
    coroutineContext = Dispatchers.Default
)
