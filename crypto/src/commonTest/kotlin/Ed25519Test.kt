import kotlinx.io.bytestring.toHexString
import org.ton.kotlin.crypto.PrivateKeyEd25519
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertContentEquals

class Ed25519Test {
    @Test
    fun test() {
        val alice = PrivateKeyEd25519.random()
        val bob = PrivateKeyEd25519.random()

        val shared1 = alice.computeSharedSecret(bob.publicKey())
        val shared2 = bob.computeSharedSecret(alice.publicKey())

        assertContentEquals(shared1, shared2)
    }

    @Test
    fun testShortId() {
        val pk = PrivateKeyEd25519(Base64.decode("C4ahDPYfqT76nBq/BC/NkLubVveN3hDhmBcCNWfmycs="))
        println("pk = ${pk.key.toHexString()} ${Base64.encode(pk.key)}")
        println("pub = ${pk.publicKey().key.toHexString()} ${Base64.encode(pk.publicKey().key)}")
        println("id = ${pk.computeShortId().toHexString()} ${Base64.encode(pk.computeShortId().toByteArray())}")
    }
}
