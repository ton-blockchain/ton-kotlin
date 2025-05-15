package org.ton.kotlin.tlb

import org.ton.kotlin.bitstring.BitString

public class TlbPrettyPrinter(
    private val stringBuilder: StringBuilder = StringBuilder(),
    private var indent: Int = 2,
    private val dummy: Boolean = false
) {
    public constructor(stringBuilder: StringBuilder, indent: Int) : this(stringBuilder, indent, false)
    public constructor(stringBuilder: StringBuilder) : this(stringBuilder, 2)
    public constructor(indent: Int) : this(StringBuilder(), indent)
    public constructor() : this(StringBuilder())

    private var level: Int = 0

    public fun open(msg: String = ""): TlbPrettyPrinter = apply {
        append('(').append(msg)
        level++
    }

    public fun close(msg: String = ""): TlbPrettyPrinter = apply {
        check(level > 0) { "TlbPrettyPrinter is already closed" }
        level--
        append(msg).append(')')
    }

    public fun newLine() {
        if (indent > 0 && !dummy) {
            if (level > 0) append("\n")
            append(" ".repeat(level * indent))
        }
    }

    public fun field(type: Any?): TlbPrettyPrinter = apply {
        if (type == null) return@apply
        level++
        if (type is TlbObject) {
            newLine()
        }
        append(" ")
        type(type)
        level--
    }

    public fun field(name: String, type: Any?): TlbPrettyPrinter = apply {
        if (type == null) return@apply
        if (type is TlbObject) {
            newLine()
        }
        append(' ').append(name).append(':')
        type(type)
    }

    public fun type(type: Any?): TlbPrettyPrinter = apply {
        try {
            when (type) {
                null -> return@apply
                is TlbObject -> type.print(this)
                else -> {
                    if (!dummy) {
                        when (type) {
                            is Boolean -> append(if (type) 1 else 0)
                            is BitString -> append("x{$type}")
                            else -> append(type)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Can't print TL-B:\n${stringBuilder}<-- HERE", e)
//            throw e
        }
    }

    private fun append(string: Any) = apply {
        if (!dummy) {
            stringBuilder.append(string.toString())
        }
    }

    public inline fun type(name: String = "", block: TlbPrettyPrinter.() -> Unit): TlbPrettyPrinter = apply {
        open(name).apply(block).close()
    }

    public inline operator fun invoke(block: TlbPrettyPrinter.() -> Unit): TlbPrettyPrinter = apply {
        apply(block)
    }

    override fun toString(): String = stringBuilder.toString()
}

public inline fun tlbPrettyPrinter(block: TlbPrettyPrinter.() -> Unit): String =
    TlbPrettyPrinter().apply(block).toString()

public inline fun tlbPrettyPrinter(name: String, noinline block: TlbPrettyPrinter.() -> Unit): String =
    TlbPrettyPrinter().type(name, block).toString()
