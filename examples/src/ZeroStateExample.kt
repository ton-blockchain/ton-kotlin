package org.ton.kotlin.examples

import io.github.andreypfau.kotlinx.crypto.sha2.sha256
import io.ktor.util.*
import org.ton.boc.BagOfCells
import org.ton.kotlin.shard.ShardStateUnsplit
import java.io.File

@OptIn(ExperimentalStdlibApi::class)
suspend fun main() {
    val zeroStateFile =
        File("/Users/andreypfau/.ton/states/zerostate_-1_8D2BF688DCB46FE65518FF49E40FA19B5BAFA1C3E5A493EF74A6560F11122CAB").readBytes()
    println("zero state file hash: ${sha256(zeroStateFile).encodeBase64()}")
    val cell = BagOfCells(zeroStateFile).first()
    val shardState = ShardStateUnsplit.loadTlb(cell)
    shardState.accounts.load().forEach { (_, v) ->
        val (_, shardAccount) = v
        val account = shardAccount.loadAccount() ?: return@forEach
        println("${account.address} - ${account.balance}")
    }
    val outMsgQueueInfo = shardState.outMsgQueueInfo.load()
//    outMsgQueueInfo.outQueue.forEach { (key, value) ->
//        val (lt, msg) = value
//        println("out: $key - $lt - $msg")
//    }

    println(outMsgQueueInfo)
}
