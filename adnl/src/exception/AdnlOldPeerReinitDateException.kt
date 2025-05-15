package org.ton.kotlin.adnl.exception

import kotlinx.datetime.Instant

public class AdnlOldPeerReinitDateException(
    public val reinitDate: Instant
) : RuntimeException("Too old remote peer reinit date: $reinitDate (${reinitDate.epochSeconds})")
