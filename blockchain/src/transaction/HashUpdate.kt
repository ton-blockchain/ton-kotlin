package org.ton.sdk.blockchain.transaction

import org.ton.sdk.crypto.HashBytes

public class HashUpdate(
    public val old: HashBytes,
    public val new: HashBytes
)
