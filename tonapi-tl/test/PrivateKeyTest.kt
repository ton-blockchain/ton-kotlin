package org.ton.api.pk

import io.ktor.util.*
import org.ton.kotlin.crypto.PrivateKeyEd25519
import kotlin.test.Test
import kotlin.test.assertContentEquals

class PrivateKeyTest {
    @Test
    fun `test creation PublicKey`() {
        val privateKeyEd25519 = PrivateKeyEd25519("d53mOPj3+xx69TYJZ2LvzhxrNn32WBvt/ioV4Ha4gz8=".decodeBase64Bytes())
        val publicKeyEd25519 = privateKeyEd25519.publicKey()
        assertContentEquals(
            "4745ede03eb4ef607843359c1f206d061a5632f68caa6f63021aa23b400950fd".hexToByteArray(),
            publicKeyEd25519.key.toByteArray()
        )
    }
}
