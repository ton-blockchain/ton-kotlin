package org.ton.kotlin.cell.exception

public class CellOverflowException : RuntimeException {
    public constructor(message: String) : super(message)
    public constructor(cause: Throwable) : super("Cell overflow", cause)
    public constructor(message: String, cause: Throwable) : super(message, cause)
}

public class CellUnderflowException : RuntimeException {
    public constructor(message: String) : super(message)
    public constructor(cause: Throwable) : super("Cell underflow", cause)
    public constructor(message: String, cause: Throwable) : super(message, cause)
}
