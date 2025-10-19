package org.ton.kotlin.adnl.internal

import org.ton.kotlin.adnl.AdnlAddress
import org.ton.kotlin.adnl.AdnlIdShort

internal class AdnlDatagram(
    val dest: AdnlIdShort,
    val address: AdnlAddress,
    val packet: ByteArray,
)
