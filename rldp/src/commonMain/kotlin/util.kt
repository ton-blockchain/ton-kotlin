package org.ton.kotlin.rldp

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.toHexString

internal fun Int.toBitString(): String {
    return this.toUInt().toString(2).padStart(32, '0')
}

internal fun ByteString.debugString(): String {
    return if (size > 16) {
        toHexString().let { it.replace(it.substring(8, it.length - 8), "..") }
    } else {
        toHexString()
    }
}
