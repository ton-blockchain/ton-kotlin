package org.ton.kotlin.dict

public class AugmentedDictionary<K, A, V>(
    public val dictionary: Dictionary<K, Pair<A, V>>,
    public val extra: A
)
