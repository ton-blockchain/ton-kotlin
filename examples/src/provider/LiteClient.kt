package org.ton.kotlin.examples.provider

import kotlinx.coroutines.Dispatchers
import org.ton.kotlin.lite.client.LiteClient

fun liteClientTestnet() = LiteClient(
    liteClientConfigGlobal = TESTNET_GLOBAL_CONFIG,
    coroutineContext = Dispatchers.Default
)

fun liteClientMainnet() = LiteClient(
    liteClientConfigGlobal = MAINNET_GLOBAL_CONFIG,
    coroutineContext = Dispatchers.Default
)
