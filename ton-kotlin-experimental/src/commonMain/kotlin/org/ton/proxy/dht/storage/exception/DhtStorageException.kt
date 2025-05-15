package org.ton.proxy.dht.storage.exception

import org.ton.kotlin.adnl.dht.DhtValue

sealed class DhtStorageException(
    val value: DhtValue,
    override val message: String
) : RuntimeException() {
    class UnsupportedUpdateRule(
        value: DhtValue,
    ) : DhtStorageException(value, "Unsupported update rule")

    class InvalidSignatureValue(
        value: DhtValue,
    ) : DhtStorageException(value, "Invalid signature value")

    class InvalidKeyDescription(
        value: DhtValue,
    ) : DhtStorageException(value, "Invalid key description")

    class InvalidDhtKey(
        value: DhtValue,
    ) : DhtStorageException(value, "Invalid dht key")

    class EmptyOverlayNodes(
        value: DhtValue,
    ) : DhtStorageException(value, "Empty overlay nodes")

    class ValueExpired(
        value: DhtValue,
    ) : DhtStorageException(value, "Value expired")

    class InvalidKey(
        value: DhtValue,
    ) : DhtStorageException(value, "Invalid key")
}
