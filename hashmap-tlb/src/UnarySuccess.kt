package org.ton.kotlin.hashmap

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.kotlin.tlb.TlbPrettyPrinter

@Serializable
@SerialName("unary_succ")
public data class UnarySuccess(
    val x: Unary
) : Unary() {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("unary_succ") {
        field("x", x)
    }

    override fun toString(): String = print().toString()
}
