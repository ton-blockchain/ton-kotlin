package org.ton.tl.constructors

import org.ton.tl.DEPRECATION_MESSAGE

@Deprecated(DEPRECATION_MESSAGE)
internal enum class Bool(val value: Boolean) {
    TRUE(true),
    FALSE(false);

    companion object {
        operator fun get(value: Boolean) = if (value) TRUE else FALSE
    }
}

@Deprecated(DEPRECATION_MESSAGE)
internal object BoolTlCombinator : EnumTlCombinator<Bool>(
    Bool::class,
    Bool.TRUE to "boolTrue = Bool",
    Bool.FALSE to "boolFalse = Bool"
)
