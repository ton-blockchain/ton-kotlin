package org.ton.kotlin.examples

import org.ton.block.Block
import org.ton.kotlin.examples.provider.LiteClientProvider
import org.ton.kotlin.examples.provider.liteClientTestnet
import org.ton.tlb.CellRef
import kotlin.time.measureTimedValue

private val provider = LiteClientProvider(liteClientTestnet())

suspend fun main() {
    val blockId = provider.getLastBlockId()
    println("last block id: $blockId")
    val blockRef = provider.getBlock(blockId) ?: error("Block with id $blockId not found")
    processBlock(blockRef)
}

fun processBlock(blockRef: CellRef<Block>) {
    repeat(100000) {
        val block = measureTimedValue { blockRef.load() }.let {
//            println("block header loaded for: ${it.duration}")
            it.value
        }
        val blockExtra = measureTimedValue { block.loadExtra() }.let {
//            println("block extra loaded for: ${it.duration}")
            it.value
        }
        measureTimedValue { blockExtra.loadAccountBlocks() }.let {
//            println("account blocks loaded for: ${it.duration}")
            it.value
        }
//        accountBlocks.forEach { hash, value ->
//            val (cc, accountBlock) = value
//            println("${accountBlock.address(blockId.workchain).toString(userFriendly = false)} $cc")
//            accountBlock.transactions.forEach { (lt, value) ->
//                val (txcc, tx) = value
//                println("  $lt. ${tx.cell.hash()} $txcc")
//            }
//        }
    }
}