package org.ton.kotlin.rldp

fun Int.toBitString(): String {
    return this.toUInt().toString(2).padStart(32, '0')
}
