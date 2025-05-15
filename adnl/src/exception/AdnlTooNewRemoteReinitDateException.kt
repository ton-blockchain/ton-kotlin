package org.ton.kotlin.adnl.exception

import kotlinx.datetime.Instant

public class AdnlTooNewRemoteReinitDateException(
    public val reinitDate: Instant
) : RuntimeException("Too new remote peer reinit date: $reinitDate (${reinitDate.epochSeconds})")
