package org.ton.kotlin.tlb.providers

import org.ton.kotlin.tlb.TlbCodec

public sealed interface TlbProvider<T> : TlbCodec<T>
