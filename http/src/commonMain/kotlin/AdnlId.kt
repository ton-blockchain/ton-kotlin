package org.ton.kotlin.http

import com.osmerion.kotlin.io.encoding.Base32
import com.osmerion.kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.adnl.AdnlIdShort

@OptIn(ExperimentalEncodingApi::class)
internal fun String.decodeAdnlId(): AdnlIdShort {
    require(length == 55) { "ADNL ID must be 55 characters long" }
    val decodedStr = Base32.decode("F${this.uppercase()}")
    require(decodedStr[0].toInt() == 0x2d) { "Invalid first byte" }
    (decodedStr[33].toInt() shl 8) or (decodedStr[34].toInt())
    return AdnlIdShort(ByteString(*decodedStr.copyOfRange(1, 33)))
}
