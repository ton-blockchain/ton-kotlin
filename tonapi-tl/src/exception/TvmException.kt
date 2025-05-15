package org.ton.kotlin.api.exception

public class TvmException(
    public val code: Int,
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(if (message != null) code.toString() else "[$code] $message", cause)
