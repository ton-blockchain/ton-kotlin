package org.ton.tl

public interface TlCodec<T> : TlDecoder<T>, TlEncoder<T>

public interface TlObject<T> where T : TlObject<T> {
    public fun tlCodec(): TlCodec<out T>

    @Suppress("UNCHECKED_CAST")
    public fun hash(): ByteArray = (tlCodec() as TlCodec<T>).hash(this as T)

    @Suppress("UNCHECKED_CAST")
    public fun toByteArray(): ByteArray {
        val codec = tlCodec() as TlCodec<T>
        return codec.encodeToByteArray(this as T)
    }
}

public interface TLFunction<Q, A> {
    public fun tlCodec(): TlCodec<Q>
    public fun resultTlCodec(): TlCodec<A>
}
