package org.ton.kotlin.tlb

import kotlin.reflect.KClass

public abstract class AbstractTlbCombinator<T : Any> : TlbCodec<T> {
    public abstract val baseClass: KClass<T>
}
