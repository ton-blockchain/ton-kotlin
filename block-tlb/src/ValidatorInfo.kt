package org.ton.kotlin.block

import kotlinx.serialization.SerialName
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.cell.invoke
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.TlbObject
import org.ton.kotlin.tlb.TlbPrettyPrinter
import org.ton.kotlin.tlb.providers.TlbConstructorProvider


@SerialName("validator_info")
public data class ValidatorInfo(
    @SerialName("validator_list_hash_short") val validatorListHashShort: UInt,
    @SerialName("catchain_seqno") val catchainSeqno: UInt,
    @SerialName("nx_cc_updated") val nxCcUpdated: Boolean
) : TlbObject {
    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer.type("validator_info") {
        field("validator_list_hash_short", validatorListHashShort)
        field("catchain_seqno", catchainSeqno)
        field("nx_cc_updated", nxCcUpdated)
    }

    override fun toString(): String = print().toString()

    public companion object : TlbConstructorProvider<ValidatorInfo> by ValidatorInfoTlbConstructor
}

private object ValidatorInfoTlbConstructor : TlbConstructor<ValidatorInfo>(
    schema = "validator_info\$_ " +
            "  validator_list_hash_short:uint32 " +
            "  catchain_seqno:uint32 " +
            "  nx_cc_updated:Bool " +
            "= ValidatorInfo;"
) {
    override fun storeTlb(
        cellBuilder: CellBuilder,
        value: ValidatorInfo
    ) = cellBuilder {
        storeUInt32(value.validatorListHashShort)
        storeUInt32(value.catchainSeqno)
        storeBit(value.nxCcUpdated)
    }

    override fun loadTlb(
        cellSlice: CellSlice
    ): ValidatorInfo = cellSlice {
        val validatorListHashShort = loadUInt32()
        val catchainSeqno = loadUInt32()
        val nxCcUpdated = loadBit()
        ValidatorInfo(validatorListHashShort, catchainSeqno, nxCcUpdated)
    }
}
