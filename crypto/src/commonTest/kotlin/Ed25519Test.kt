import org.ton.kotlin.crypto.PrivateKeyEd25519
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
}
