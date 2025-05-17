package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.bitstring.BitString


@SerialName("libref_hash")
public data class LibRefHash(
    val lib_hash: BitString
) : LibRef {
    init {
        require(lib_hash.size == 256) { "required: lib_hash.size == 256, actual: ${lib_hash.size}" }
    }
}
