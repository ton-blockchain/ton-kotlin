package org.ton.kotlin.tl.constructors

internal enum class Bool(val value: Boolean) {
    TRUE(true),
    FALSE(false);

    companion object {
        operator fun get(value: Boolean) = if (value) TRUE else FALSE
    }
}

internal object BoolTlCombinator : EnumTlCombinator<Bool>(
    Bool::class,
    Bool.TRUE to "boolTrue = Bool",
    Bool.FALSE to "boolFalse = Bool"
)
