package org.ton.kotlin.hashmap

import org.ton.kotlin.bitstring.BitString

public interface AugmentedDictionary<X, Y> : Iterable<Pair<BitString, AugmentedDictionary.Node<X, Y>>> {
    public operator fun get(key: BitString): Leaf<X, Y>?

    public interface Node<X, Y> {
        public val extra: Y
        public val value: X?
    }

    public interface Leaf<X, Y> : Node<X, Y> {
        override val extra: Y
        override val value: X
    }
}
