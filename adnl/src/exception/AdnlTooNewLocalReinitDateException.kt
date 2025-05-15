package org.ton.kotlin.adnl.exception

import kotlinx.datetime.Instant

public class AdnlTooNewLocalReinitDateException(
    public val reinitDate: Instant
) : RuntimeException("Too new local peer reinit date: $reinitDate (${reinitDate.epochSeconds})")
