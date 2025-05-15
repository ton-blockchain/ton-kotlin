package org.ton.kotlin.tlb

public interface TlbObject {
    public fun print(printer: TlbPrettyPrinter = TlbPrettyPrinter()): TlbPrettyPrinter
}
