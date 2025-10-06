package org.ton.kotlin.crypto

public sealed interface PrivateKey : Decryptor, Signer {
    public val publicKey: PublicKey
}
