package org.ton.kotlin.tlb.exception

import org.ton.kotlin.bitstring.BitString

public class UnknownTlbConstructorException(
    public val id: BitString? = null
) : IllegalArgumentException(if (id != null) "Unknown constructor: $id (${id.toBinary()})" else "Unknown constructor")
