package org.ton.kotlin.block

import org.ton.hashmap.HashMapE
import org.ton.kotlin.cell.Cell


public data class VmLibraries(
    val libraries: HashMapE<Cell>
)
