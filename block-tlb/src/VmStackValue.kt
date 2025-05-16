@file:Suppress("OPT_IN_USAGE", "NOTHING_TO_INLINE")

package org.ton.kotlin.block

import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.kotlin.bigint.BigInt
import org.ton.kotlin.cell.Cell
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.TlbCombinator
import org.ton.kotlin.tlb.providers.TlbCombinatorProvider
import kotlin.jvm.JvmStatic

public inline fun VmStackValue(): VmStackValue = VmStackValue.of()
public inline fun VmStackValue(int: Int): VmStackTinyInt = VmStackValue.of(int)
public inline fun VmStackValue(long: Long): VmStackTinyInt = VmStackValue.of(long)
public inline fun VmStackValue(boolean: Boolean): VmStackTinyInt = VmStackValue.of(boolean)
public inline fun VmStackValue(bigInt: BigInt): VmStackInt = VmStackValue.of(bigInt)
public inline fun VmStackValue(cell: Cell): VmStackCell = VmStackValue.of(cell)
public inline fun VmStackValue(cellSlice: CellSlice): VmCellSlice = VmStackValue.of(cellSlice)
public inline fun VmStackValue(cellBuilder: CellBuilder): VmStackBuilder = VmStackValue.of(cellBuilder)
public inline fun VmStackValue(cont: VmCont): VmStackCont = VmStackValue.of(cont)
public inline fun VmStackValue(tuple: VmTuple): VmStackTuple = VmStackValue.of(tuple)

@JsonClassDiscriminator("@type")

public sealed interface VmStackValue {
    public companion object : TlbCombinatorProvider<VmStackValue> by VmStackValueTlbCombinator {
        @JvmStatic
        public fun of(): VmStackNull = VmStackNull

        @JvmStatic
        public fun of(int: Int): VmStackTinyInt = VmStackTinyInt(int)

        @JvmStatic
        public fun of(boolean: Boolean): VmStackTinyInt = VmStackTinyInt(boolean)

        @JvmStatic
        public fun of(long: Long): VmStackTinyInt = VmStackTinyInt(long)

        @JvmStatic
        public fun of(bigInt: BigInt): VmStackInt = VmStackInt(bigInt)

        @JvmStatic
        public fun of(cell: Cell): VmStackCell = VmStackCell(cell)

        @JvmStatic
        public fun of(cellSlice: CellSlice): VmCellSlice = VmCellSlice(cellSlice)

        @JvmStatic
        public fun of(cellBuilder: CellBuilder): VmStackBuilder = VmStackBuilder(cellBuilder)

        @JvmStatic
        public fun of(cont: VmCont): VmStackCont = VmStackCont(cont)

        @JvmStatic
        public fun of(tuple: VmTuple): VmStackTuple = VmStackTuple(tuple)
    }
}

private object VmStackValueTlbCombinator : TlbCombinator<VmStackValue>(
    VmStackValue::class,
    VmStackNull::class to VmStackNull.tlbConstructor(),
    VmStackTinyInt::class to VmStackTinyInt.tlbConstructor(),
    VmStackInt::class to VmStackInt.tlbConstructor(),
    VmStackNan::class to VmStackNan.tlbConstructor(),
    VmStackCell::class to VmStackCell.tlbConstructor(),
    VmCellSlice::class to VmCellSlice.tlbConstructor(),
    VmStackBuilder::class to VmStackBuilder.tlbConstructor(),
    VmStackCont::class to VmStackCont.tlbConstructor(),
    VmStackTuple::class to VmStackTuple.tlbConstructor()
)
