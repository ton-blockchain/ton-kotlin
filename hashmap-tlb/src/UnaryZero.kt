package org.ton.hashmap

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tlb.TlbPrettyPrinter

@Serializable
@SerialName("unary_zero")
public object UnaryZero : Unary() {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("unary_zero")

    override fun toString(): String = print().toString()
}
