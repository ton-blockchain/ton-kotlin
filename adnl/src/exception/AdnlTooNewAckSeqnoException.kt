package org.ton.kotlin.adnl.exception

public class AdnlTooNewAckSeqnoException(
    public val ackSeqno: Long,
    public val localSeqno: Long
) : RuntimeException("Too new ack seqno: $ackSeqno (current max sent $localSeqno)")
