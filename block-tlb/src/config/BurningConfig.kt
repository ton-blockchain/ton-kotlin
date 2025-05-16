@file:Suppress("PackageDirectoryMismatch")

package org.ton.kotlin.config

import org.ton.kotlin.bigint.div
import org.ton.kotlin.bigint.times
import org.ton.kotlin.bigint.toBigInt
import org.ton.kotlin.block.AddrStd
import org.ton.kotlin.block.Coins
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbCodec

public data class BurningConfig(
    val blackholeAddress: AddrStd?,
    val feeBurnNum: Int,
    val feeBurnDenom: Int
) {
    init {
        require(feeBurnDenom >= 1) { "feeBurnDenom must be at least 1, actual: $feeBurnDenom" }
        require(feeBurnNum in 0..feeBurnDenom) { "feeBurnNum must be in 0..$feeBurnDenom, actual: $feeBurnNum" }
    }

    public fun calculateBurnedFees(value: Coins): Coins {
        if (value == Coins.ZERO) return value
        return Coins(value.amount.value.times(feeBurnNum.toBigInt()).div(feeBurnDenom.toBigInt()))
    }

    public companion object : TlbCodec<BurningConfig> by BurningConfigTlbCodec
}

private object BurningConfigTlbCodec : TlbCodec<BurningConfig> {
    override fun loadTlb(slice: CellSlice, context: CellContext): BurningConfig {
        val tag = slice.loadUInt(8).toInt()
        require(tag == 0x01) { "Invalid BurningConfig tag: ${tag.toHexString()}" }
        val blackholeAddress = if (slice.loadBoolean()) AddrStd(-1, slice.loadByteArray(32)) else null
        val feeBurnNum = slice.loadUInt(32).toInt()
        val feeBurnDenom = slice.loadUInt(32).toInt()
        return BurningConfig(blackholeAddress, feeBurnNum, feeBurnDenom)
    }

    override fun storeTlb(builder: CellBuilder, value: BurningConfig) {
        builder.storeUInt(0x01, 8)
        if (value.blackholeAddress != null) {
            builder.storeBoolean(true)
            builder.storeBitString(value.blackholeAddress.address)
        } else {
            builder.storeBoolean(false)
        }
        builder.storeUInt(value.feeBurnNum, 32)
        builder.storeUInt(value.feeBurnDenom, 32)
    }
}
