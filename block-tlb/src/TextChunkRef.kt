package org.ton.kotlin.block

import org.ton.kotlin.tlb.TlbCombinator

public sealed interface TextChunkRef {
    public companion object {
        public fun tlbCombinator(n: Int): TlbCombinator<TextChunkRef> = if (n == 0) {
            ChunkRefEmpty.tlbConstructor()
        } else {
            ChunkRef.tlbConstructor(n)
        } as TlbCombinator<TextChunkRef>
    }
}
