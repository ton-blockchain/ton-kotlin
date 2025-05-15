package org.ton.kotlin.hashmap

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.tlb.TlbPrettyPrinter

@Serializable
@SerialName("hme_empty")
public class HmeEmpty<T> : HashMapE<T> {
    override fun iterator(): Iterator<Pair<BitString, T>> = EmptyIterator()

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter =
        printer.type("hme_empty")

    override fun toString(): String = print().toString()
}

private class EmptyIterator<T> : Iterator<Pair<BitString, T>> {
    override fun hasNext(): Boolean = false
    override fun next(): Pair<BitString, T> = throw NoSuchElementException()
}
