package org.ton.kotlin.tlb

import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.exception.UnknownTlbConstructorException
import kotlin.reflect.KClass

public abstract class TlbNegatedCombinator<T : Any>(
    baseClass: KClass<T>,
    vararg subClasses: Pair<KClass<out T>, TlbNegatedConstructor<out T>>
) : TlbCombinator<T>(
    baseClass,
    *subClasses
), TlbNegatedCodec<T> {
    override fun storeTlb(cellBuilder: CellBuilder, value: T, context: CellContext) {
        storeNegatedTlb(cellBuilder, value)
    }

    override fun loadTlb(cellSlice: CellSlice, context: CellContext): T = loadNegatedTlb(cellSlice).value

    override fun storeNegatedTlb(cellBuilder: CellBuilder, value: T): Int {
        val constructor = findTlbStorerOrNull(value) as? TlbNegatedConstructor<T>
            ?: throw UnknownTlbConstructorException()
        cellBuilder.storeBitString(constructor.id)
        return constructor.storeNegatedTlb(cellBuilder, value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun loadNegatedTlb(cellSlice: CellSlice): TlbNegatedResult<T> {
        val constructor = findTlbLoaderOrNull(cellSlice) as? TlbNegatedConstructor<out T>
            ?: throw UnknownTlbConstructorException()
        cellSlice.skipBits(constructor.id.size)
        return constructor.loadNegatedTlb(cellSlice) as TlbNegatedResult<T>
    }
}
