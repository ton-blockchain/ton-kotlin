package org.ton.proxy.dht.storage

import kotlinx.datetime.Clock
import org.ton.kotlin.adnl.dht.DhtKey
import org.ton.kotlin.adnl.dht.DhtUpdateRule
import org.ton.kotlin.adnl.dht.DhtValue
import org.ton.kotlin.adnl.overlay.OverlayNode
import org.ton.kotlin.adnl.overlay.OverlayNodes
import org.ton.kotlin.adnl.pub.PublicKeyOverlay
import org.ton.kotlin.bitstring.BitString
import org.ton.proxy.dht.storage.exception.DhtStorageException

abstract class AbstractDhtStorage(
    override val config: DhtStorageConfig
) : DhtStorage {
    override fun put(value: DhtValue): Boolean {
        val now = Clock.System.now()
        if (value.ttl() <= now) {
            throw DhtStorageException.ValueExpired(value)
        }
        if (value.key.key.name.length > config.maxKeyNameLength) {
            throw DhtStorageException.InvalidKey(value)
        }
        if (value.key.key.idx > config.maxKeyIndex) {
            throw DhtStorageException.InvalidKey(value)
        }
        if (!value.key.key.id.contentEquals(value.key.id.hash())) {
            throw DhtStorageException.InvalidKey(value)
        }
        return when (value.key.update_rule) {
            DhtUpdateRule.SIGNATURE -> putSigned(value)
            DhtUpdateRule.OVERLAY_NODES -> putOverlayNodes(value)
            DhtUpdateRule.ANYBODY -> if (config.allowUnsignedValues) {
                putUnsigned(value)
            } else {
                throw DhtStorageException.UnsupportedUpdateRule(value)
            }
        }
    }

    override fun get(bitString: BitString): DhtValue? {
        val value = getUnchecked(bitString) ?: return null
        return if (value.ttl() > Clock.System.now()) value else null
    }

    fun putUnsigned(value: DhtValue): Boolean {
        val key = BitString(value.key.key.hash())
        val currentValue = getUnchecked(key)
        if (currentValue != null && currentValue.ttl() > value.ttl()) {
            return false
        }
        putUnchecked(key, value)
        return true
    }

    fun putSigned(value: DhtValue): Boolean {
        if (!value.verify(value.key.id)) {
            throw DhtStorageException.InvalidSignatureValue(value)
        }
        return putUnsigned(value)
    }

    fun putOverlayNodes(value: DhtValue): Boolean {
        if (value.signature.isNotEmpty() || value.key.signature.isNotEmpty()) {
            throw DhtStorageException.InvalidSignatureValue(value)
        }
        val overlayKey = value.key.id as? PublicKeyOverlay
        val overlayId = overlayKey?.toAdnlIdShort() ?: throw DhtStorageException.InvalidKeyDescription(value)
        val requiredKey = DhtKey.nodes(overlayId)
        if (value.key.key != requiredKey) {
            throw DhtStorageException.InvalidDhtKey(value)
        }

        val newNodes = OverlayNodes.decodeBoxed(value.value).filter {
            overlayId.verify(it)
        }
        if (newNodes.isEmpty()) {
            throw DhtStorageException.EmptyOverlayNodes(value)
        }

        val key = BitString(value.key.key.hash())
        val currentValue = getUnchecked(key)
        val oldNodes = if (currentValue != null) {
            val oldTtl = currentValue.ttl()
            when {
                oldTtl < Clock.System.now() -> null
                oldTtl > value.ttl() -> return false
                else -> OverlayNodes.decodeBoxed(currentValue.value).nodes
            }
        } else null
        val nodes = mergeOverlayNodes(newNodes, oldNodes)
        put(
            value.copy(
                value = OverlayNodes(nodes).toByteArray(),
            )
        )
        return true
    }

    protected abstract fun putUnchecked(key: BitString, value: DhtValue)

    protected abstract fun getUnchecked(key: BitString): DhtValue?

    private fun mergeOverlayNodes(newNodes: List<OverlayNode>, oldNodes: List<OverlayNode>?): List<OverlayNode> {
        val result = oldNodes?.associateByTo(HashMap()) { it.id } ?: HashMap()
        for (node in newNodes) {
            val currentNode = result[node.id]
            if (currentNode != null) {
                if (currentNode.version < node.version) {
                    result[node.id] = node
                }
            } else {
                result[node.id] = node
            }
        }
        return result.values.toList()
    }
}
