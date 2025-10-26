package org.ton.block

import org.ton.tlb.TlbCodec

public sealed interface TextChunks {
    public companion object {
        @Suppress("UNCHECKED_CAST")
        public fun tlbCodec(n: Int): TlbCodec<TextChunks> = if (n == 0) {
            TextChunkEmpty
        } else {
            TextChunk.tlbConstructor(n)
        } as TlbCodec<TextChunks>
    }
}
