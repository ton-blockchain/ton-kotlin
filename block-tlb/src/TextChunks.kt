package org.ton.kotlin.block

import org.ton.kotlin.tlb.TlbCodec

public sealed interface TextChunks {
    public companion object {
        public fun tlbCodec(n: Int): TlbCodec<TextChunks> = if (n == 0) {
            TextChunkEmpty
        } else {
            TextChunk.tlbConstructor(n)
        } as TlbCodec<TextChunks>
    }
}
