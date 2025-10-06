package org.ton.kotlin.crypto

import kotlinx.io.bytestring.ByteString
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

open class Ed25519Test {
    @Test
    fun testSignVerify() {
        val privateKey = PrivateKeyEd25519.random()
        val message = "test message".encodeToByteArray()
        val signature = privateKey.signToByteArray(message)
        val publicKey = privateKey.publicKey
        assertTrue(publicKey.verifySignature(message, signature))
        val wrongMessage = "wrong message".encodeToByteArray()
        assertFalse(publicKey.verifySignature(wrongMessage, signature))
    }

    @Test
    fun sharedKey() {
        val alicePrivate = PrivateKeyEd25519.random()
        val alicePublic = alicePrivate.publicKey

        val bobPrivate = PrivateKeyEd25519.random()
        val bobPublic = bobPrivate.publicKey

        val aliceShared = alicePrivate.computeSharedSecret(bobPublic)
        val bobShared = bobPrivate.computeSharedSecret(alicePublic)

        assertContentEquals(aliceShared, bobShared)
    }

    @Test
    fun testGolden() {
        GOLDEN_ED25519.lines().forEach { line ->
            val goldenData = Ed25519GoldenData.parse(line)
            try {
                testGolden(goldenData)
            } catch (e: Throwable) {
                throw e
            }
        }
    }

    private fun testGolden(goldenData: Ed25519GoldenData) {
        val sig = goldenData.signatureBytes
        val priv = goldenData.privateKey
        val pubKey = goldenData.publicBytes
        val msg = goldenData.message

        val sig2 = priv.signToByteArray(msg)
        assertContentEquals(sig, sig2)

        val pubKey2 = priv.publicKey.key.toByteArray()
        assertContentEquals(pubKey, pubKey2)

        assertContentEquals(goldenData.privateBytes.copyOf(32), priv.key.toByteArray())
    }
}

private class Ed25519GoldenData(
    val privateBytes: ByteArray,
    val publicBytes: ByteArray,
    val message: ByteArray,
    val signatureBytes: ByteArray,
) {
    val privateKey = PrivateKeyEd25519(ByteString(*privateBytes.copyOf(32)))

    companion object {
        fun parse(line: String): Ed25519GoldenData {
            val (
                privateBytes,
                pubKey,
                msg,
                sig
            ) = line.split(":").map { it.hexToByteArray() }
            return Ed25519GoldenData(
                privateBytes,
                pubKey,
                msg,
                sig.copyOf(64)
            )
        }
    }
}
