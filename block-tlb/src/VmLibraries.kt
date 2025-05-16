package org.ton.kotlin.block

import org.ton.kotlin.cell.Cell
import org.ton.kotlin.hashmap.HashMapE


public data class VmLibraries(
    val libraries: HashMapE<Cell>
)
