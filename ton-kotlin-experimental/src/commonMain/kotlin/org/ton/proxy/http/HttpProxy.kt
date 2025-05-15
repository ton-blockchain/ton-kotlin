package org.ton.proxy.http

import org.ton.kotlin.adnl.pk.PrivateKey
import org.ton.kotlin.adnl.pk.PrivateKeyEd25519
import org.ton.kotlin.adnl.pub.PublicKey
import org.ton.kotlin.adnl.pub.PublicKeyEd25519
import org.ton.proxy.adnl.engine.AdnlNetworkEngine
import org.ton.proxy.adnl.resolver.AdnlAddressResolver
import org.ton.proxy.rldp.AbstractRldpPeerSession
import org.ton.proxy.rldp.Rldp
import org.ton.proxy.rldp.RldpPeerSession

class HttpProxy(
    networkEngine: AdnlNetworkEngine,
    addressResolver: AdnlAddressResolver
) : Rldp(
    networkEngine,
    addressResolver
) {
    override fun createPeer(remoteKey: PublicKey, localKey: PrivateKey): RldpPeerSession {
        require(remoteKey is PublicKeyEd25519)
        require(localKey is PrivateKeyEd25519)
        return object : AbstractRldpPeerSession(this@HttpProxy, localKey, remoteKey) {}
    }
}
