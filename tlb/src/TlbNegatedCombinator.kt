package org.ton.tlb

import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.kotlin.cell.CellContext
import org.ton.tlb.exception.UnknownTlbConstructorException
import kotlin.reflect.KClass

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

    override fun storeNegatedTlb(builder: CellBuilder, value: T): Int {
        val constructor = findTlbStorerOrNull(value) as? TlbNegatedConstructor<T>
            ?: throw UnknownTlbConstructorException()
        builder.storeBitString(constructor.id)
        return constructor.storeNegatedTlb(builder, value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun loadNegatedTlb(slice: CellSlice): TlbNegatedResult<T> {
        val constructor = findTlbLoaderOrNull(slice) as? TlbNegatedConstructor<out T>
            ?: throw UnknownTlbConstructorException()
        slice.skipBits(constructor.id.size)
        return constructor.loadNegatedTlb(slice) as TlbNegatedResult<T>
    }
}
