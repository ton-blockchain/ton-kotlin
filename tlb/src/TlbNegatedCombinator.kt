package org.ton.tlb

import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.kotlin.cell.CellContext
import org.ton.tlb.exception.UnknownTlbConstructorException
import kotlin.reflect.KClass

@Deprecated("Scheduled to remove")
@Suppress("DEPRECATION")
public abstract class TlbNegatedCombinator<T : Any>(
    baseClass: KClass<T>,
    vararg subClasses: Pair<KClass<out T>, TlbNegatedConstructor<out T>>
) : TlbCombinator<T>(
    baseClass,
    *subClasses
), TlbNegatedCodec<T> {
    override fun storeTlb(builder: CellBuilder, value: T, context: CellContext) {
        storeNegatedTlb(builder, value)
    }

    override fun loadTlb(slice: CellSlice, context: CellContext): T = loadNegatedTlb(slice).value

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
