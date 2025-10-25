package org.ton.sdk.crypto

public expect class Sha256 : Digest {
    public constructor()

    override val digestSize: Int
    override val blockSize: Int

    public override fun update(source: ByteArray, startIndex: Int, endIndex: Int): Sha256

    public override fun digest(destination: ByteArray, destinationOffset: Int)

    public override fun reset()

    override fun close()
}

public fun sha256(input: ByteArray): ByteArray {
    return Sha256().use { sha256 ->
        sha256.update(input)
        sha256.digest()
    }
}
