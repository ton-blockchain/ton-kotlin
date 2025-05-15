package org.ton.kotlin.tlb

import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.exception.ParseTlbException
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import kotlin.jvm.JvmStatic
import kotlin.reflect.KClass

public abstract class AbstractTlbConstructor<T>(
    public val schema: String,
    id: BitString? = null,
) {
    public val id: BitString by lazy(LazyThreadSafetyMode.PUBLICATION) {
        id ?: calculateId(formatSchema(schema))
    }

    override fun toString(): String = schema

    public companion object {
        @JvmStatic
        public fun calculateId(schema: String): BitString {
            if (schema.isEmpty()) return BitString(0)
            try {
                val prefix = schema.split(" ").first()
                if (prefix.contains('$')) {
                    val (_, id) = prefix.split('$')
                    if (id != "_") {
                        return BitString.binary(id)
                    }
                } else if (prefix.contains('#')) {
                    val (_, id) = prefix.split('#')
                    if (id != "_") {
                        return BitString(id)
                    }
                }
                return BitString(0)
            } catch (e: Exception) {
                throw ParseTlbException("Failed to calculate id for schema: `$schema`", e)
            }
        }

        public fun formatSchema(schema: String): String {
            return schema.replace(Regex("\\s+"), " ").trim()
                .replace("(", "")
                .replace(")", "")
                .replace(";", "")
        }
    }
}

public abstract class TlbConstructor<T : Any>(
    schema: String,
    id: BitString? = null,
) : AbstractTlbConstructor<T>(schema, id), TlbCodec<T>, TlbConstructorProvider<T> {
    override fun tlbConstructor(): TlbConstructor<T> = this
}

public class ObjectTlbConstructor<T : Any>(
    public val instance: T,
    schema: String,
    id: BitString? = null,
) : TlbConstructor<T>(schema, id) {
    override fun storeTlb(cellBuilder: CellBuilder, value: T) {
    }

    override fun loadTlb(cellSlice: CellSlice): T = instance
}

public abstract class TlbNegatedConstructor<T : Any>(
    schema: String,
    id: BitString? = null
) : TlbConstructor<T>(schema, id), TlbNegatedCodec<T> {

    override fun storeTlb(builder: CellBuilder, value: T) {
        storeNegatedTlb(builder, value)
    }

    override fun storeTlb(cellBuilder: CellBuilder, value: T, context: CellContext) {
        storeNegatedTlb(cellBuilder, value)
    }

    override fun loadTlb(slice: CellSlice, context: CellContext): T = loadNegatedTlb(slice).value

    override fun loadTlb(slice: CellSlice): T {
        return loadNegatedTlb(slice).value
    }
}

public inline fun <reified T : Any> TlbConstructor<T>.asTlbCombinator(): TlbCombinator<T> = asTlbCombinator(T::class)

public fun <T : Any> TlbConstructor<T>.asTlbCombinator(clazz: KClass<T>): TlbCombinator<T> = object : TlbCombinator<T>(
    clazz,
    clazz to this@asTlbCombinator
) {
}
