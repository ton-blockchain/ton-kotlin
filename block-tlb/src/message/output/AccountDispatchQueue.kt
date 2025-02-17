package org.ton.block.message.output

import org.ton.block.EnqueuedMsg
import org.ton.kotlin.dict.Dictionary
import org.ton.tlb.TlbCodec

public class AccountDispatchQueue(
    public val messages: Dictionary<Long, EnqueuedMsg>,
    public val count: Long
) {
    public companion object : TlbCodec<AccountDispatchQueue>
}