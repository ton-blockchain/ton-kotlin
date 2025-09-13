package org.ton.kotlin.http

import com.osmerion.kotlin.io.encoding.Base32
import com.osmerion.kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.decodeToByteString
import kotlin.io.encoding.Base64

@OptIn(ExperimentalEncodingApi::class)
internal fun String.decodeHash(): ByteString {
    if (length == 64) {
        return Base64.decodeToByteString(this)
    }
    require(length == 55) { "ADNL ID must be 55 characters long" }
    val decodedStr = Base32.decode("F${this.uppercase()}")
    require(decodedStr[0].toInt() == 0x2d) { "Invalid first byte" }
    (decodedStr[33].toInt() shl 8) or (decodedStr[34].toInt())
    return ByteString(*decodedStr.copyOfRange(1, 33))
}
