package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.Cell


@SerialName("libref_ref")
public data class LibRefRef(
    val library: Cell
) : LibRef
