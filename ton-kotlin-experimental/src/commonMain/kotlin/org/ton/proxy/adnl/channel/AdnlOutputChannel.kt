@file:Suppress("NOTHING_TO_INLINE")

package org.ton.proxy.adnl.channel

import org.ton.kotlin.adnl.adnl.AdnlIdShort
import org.ton.kotlin.adnl.pub.PublicKeyAes
import org.ton.kotlin.bitstring.BitString
import kotlin.jvm.JvmStatic

inline fun AdnlOutputChannel(key: PublicKeyAes): AdnlOutputChannel = AdnlOutputChannel.of(key)

interface AdnlOutputChannel {
    val id: AdnlIdShort
    val key: PublicKeyAes

    companion object {
        @JvmStatic
        fun of(key: PublicKeyAes): AdnlOutputChannel = AdnlOutputChannelImpl(key)
    }
}

private data class AdnlOutputChannelImpl(
    override val key: PublicKeyAes,
) : AdnlOutputChannel {
    override val id get() = key.toAdnlIdShort()
}
