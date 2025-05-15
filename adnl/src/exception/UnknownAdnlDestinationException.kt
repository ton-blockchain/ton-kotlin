package org.ton.kotlin.adnl.exception

import org.ton.kotlin.adnl.adnl.AdnlIdShort

public class UnknownAdnlDestinationException(
    public val destination: AdnlIdShort
) : RuntimeException("Unknown ADNL destination: $destination")
