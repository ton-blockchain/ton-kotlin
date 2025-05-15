@file:Suppress("NOTHING_TO_INLINE")

package org.ton.kotlin.api.exception

public inline fun TonException(code: Int, message: String, cause: Throwable? = null): TonException = when (code) {
    TonFailureException.CODE -> TonFailureException(message, cause)
    TonErrorException.CODE -> TonErrorException(message, cause)
    TonWarningException.CODE -> TonWarningException(message, cause)
    TonProtoviolationException.CODE -> TonProtoviolationException(message, cause)
    TonNotReadyException.CODE -> TonNotReadyException(message, cause)
    TonTimeoutException.CODE -> TonTimeoutException(message, cause)
    TonCancelledException.CODE -> TonCancelledException(message, cause)
    else -> object : TonException(message, cause) {
        override val code: Int = code
    }
}

public abstract class TonException(
    override val message: String,
    override val cause: Throwable? = null
) : RuntimeException(message, cause) {
    public abstract val code: Int
}

public open class TonFailureException(
    override val message: String,
    override val cause: Throwable? = null
) : TonException(message, cause) {
    override val code: Int = CODE

    public companion object {
        public const val CODE: Int = 601
    }
}

public open class TonErrorException(
    override val message: String,
    override val cause: Throwable? = null
) : TonException(message, cause) {
    override val code: Int = CODE

    public companion object {
        public const val CODE: Int = 602
    }
}

public open class TonWarningException(
    override val message: String,
    override val cause: Throwable? = null
) : TonException(message, cause) {
    override val code: Int = CODE

    public companion object {
        public const val CODE: Int = 603
    }
}

public open class TonProtoviolationException(
    override val message: String,
    override val cause: Throwable? = null
) : TonException(message, cause) {
    override val code: Int = CODE

    public companion object {
        public const val CODE: Int = 621
    }
}

public open class TonNotReadyException(
    override val message: String,
    override val cause: Throwable? = null
) : TonException(message, cause) {
    override val code: Int = CODE

    public companion object {
        public const val CODE: Int = 651
    }
}

public open class TonTimeoutException(
    override val message: String,
    override val cause: Throwable? = null
) : TonException(message, cause) {
    override val code: Int = CODE

    public companion object {
        public const val CODE: Int = 652
    }
}

public open class TonCancelledException(
    override val message: String,
    override val cause: Throwable? = null
) : TonException(message, cause) {
    override val code: Int = CODE

    public companion object {
        public const val CODE: Int = 653
    }
}
