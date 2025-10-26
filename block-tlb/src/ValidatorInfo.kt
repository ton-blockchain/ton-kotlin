package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.cell.invoke
import org.ton.tlb.TlbConstructor
import org.ton.tlb.TlbObject
import org.ton.tlb.TlbPrettyPrinter
import org.ton.tlb.providers.TlbConstructorProvider


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
        builder: CellBuilder,
        value: ValidatorInfo
    ) = builder {
        storeUInt32(value.validatorListHashShort)
        storeUInt32(value.catchainSeqno)
        storeBit(value.nxCcUpdated)
    }

    override fun loadTlb(
        slice: CellSlice
    ): ValidatorInfo = slice {
        val validatorListHashShort = loadUInt32()
        val catchainSeqno = loadUInt32()
        val nxCcUpdated = loadBoolean()
        ValidatorInfo(validatorListHashShort, catchainSeqno, nxCcUpdated)
    }
}
