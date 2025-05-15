package org.ton.proxy.adnl

import org.ton.kotlin.adnl.adnl.message.*

interface AdnlMessageReceiver {
    fun receiveAdnlMessage(message: AdnlMessage) = when (message) {
        is AdnlMessageAnswer -> receiveAdnlAnswer(message)
        is AdnlMessageConfirmChannel -> receiveAdnlConfirmChannel(message)
        is AdnlMessageCreateChannel -> receiveAdnlCreateChannel(message)
        is AdnlMessageCustom -> receiveAdnlCustom(message)
        is AdnlMessageNop -> receiveNop(message)
        is AdnlMessagePart -> receivePart(message)
        is AdnlMessageQuery -> receiveAdnlQuery(message)
        is AdnlMessageReinit -> receiveReinit(message)
    }

    fun receiveAdnlConfirmChannel(message: AdnlMessageConfirmChannel) {

    }

    fun receiveAdnlCreateChannel(message: AdnlMessageCreateChannel) {

    }

    fun receiveAdnlCustom(message: AdnlMessageCustom) {

    }

    fun receiveAdnlAnswer(message: AdnlMessageAnswer) {

    }

    fun receiveAdnlQuery(message: AdnlMessageQuery) {

    }

    fun receiveNop(message: AdnlMessageNop) {

    }

    fun receivePart(message: AdnlMessagePart) {

    }

    fun receiveReinit(message: AdnlMessageReinit) {

    }
}
