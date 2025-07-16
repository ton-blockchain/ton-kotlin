import org.ton.kotlin.crypto.PrivateKeyEd25519
import kotlin.test.Test
import kotlin.test.assertContentEquals

class AesTest {
    @Test
    fun testEncrypt() {
        val key = PrivateKeyEd25519.random()
        val message = "Hello, World!".encodeToByteArray()
        val encrypted = key.publicKey().createEncryptor().encryptToByteArray(message)
        val decrypted = key.createDecryptor().decryptToByteArray(encrypted)
        assertContentEquals(message, decrypted)
    }
}
