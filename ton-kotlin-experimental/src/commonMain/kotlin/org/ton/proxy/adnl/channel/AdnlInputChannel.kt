@file:Suppress("NOTHING_TO_INLINE")

package org.ton.proxy.adnl.channel

import org.ton.kotlin.adnl.adnl.AdnlIdShort
import org.ton.kotlin.adnl.pk.PrivateKeyAes
import org.ton.kotlin.bitstring.BitString
import kotlin.jvm.JvmStatic

inline fun AdnlInputChannel(key: PrivateKeyAes): AdnlInputChannel = AdnlInputChannel.of(key)

interface AdnlInputChannel {
    val id: AdnlIdShort
    val key: PrivateKeyAes

    companion object {
        @JvmStatic
        fun of(key: PrivateKeyAes): AdnlInputChannel = AdnlInputChannelImpl(key)
    }
}

private data class AdnlInputChannelImpl(
    override val key: PrivateKeyAes,
) : AdnlInputChannel {
    override val id: AdnlIdShort get() = key.toAdnlIdShort()
}
