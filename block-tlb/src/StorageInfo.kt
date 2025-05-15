package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import kotlin.jvm.JvmName


@SerialName("storage_info")
public data class StorageInfo(
    @get:JvmName("used")
    val used: StorageUsed,

    @get:JvmName("lastPaid")
    @SerialName("last_paid") val lastPaid: UInt,

    @get:JvmName("duePayment")
    @SerialName("due_payment") val duePayment: Maybe<Coins>
) : TlbObject {
    public constructor(used: StorageUsed, lastPaid: UInt, duePayment: Coins? = null) : this(
        used,
        lastPaid,
        duePayment.toMaybe()
    )

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter {
        return printer.type("storage_info") {
            field("used", used)
            field("last_paid", lastPaid)
            field("due_payment", duePayment)
        }
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<StorageInfo> by StorageInfoTlbConstructor
}

private object StorageInfoTlbConstructor : TlbConstructor<StorageInfo>(
    schema = "storage_info\$_ used:StorageUsed last_paid:uint32 due_payment:(Maybe Coins) = StorageInfo;"
) {
    private val maybeCoins = Maybe(Coins)

    override fun storeTlb(
        cellBuilder: CellBuilder, value: StorageInfo
    ) = cellBuilder {
        storeTlb(StorageUsed, value.used)
        storeUInt32(value.lastPaid)
        storeTlb(maybeCoins, value.duePayment)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): StorageInfo = cellSlice {
        val used = loadTlb(StorageUsed)
        val lastPaid = loadUInt32()
        val duePayment = loadTlb(maybeCoins)
        StorageInfo(used, lastPaid, duePayment)
    }
}
