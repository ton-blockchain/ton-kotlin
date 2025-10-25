package org.ton.sdk.crypto

public expect class Sha512 : Digest {
    public constructor()

    override val digestSize: Int
    override val blockSize: Int

    public override fun update(source: ByteArray, startIndex: Int, endIndex: Int): Sha512

    public override fun digest(destination: ByteArray, destinationOffset: Int)

    public override fun reset()

    override fun close()
}

public fun sha512(input: ByteArray): ByteArray {
    return Sha512().use { sha512 ->
        sha512.update(input)
        sha512.digest()
    }
}
