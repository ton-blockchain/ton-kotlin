package org.ton.kotlin.tl.internal

import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import org.ton.kotlin.tl.Bits128
import org.ton.kotlin.tl.Bits256
import org.ton.kotlin.tl.TlConditional

internal data class TlMeta(
    val index: Int,
    val name: String,
    val isBits128: Boolean = false,
    val isBits256: Boolean = false,
    val conditional: TlConditional? = null,
)

internal fun SerialDescriptor.getTlMeta(index: Int): TlMeta {
    var isBits128 = false
    var isBits256 = false
    var conditional: TlConditional? = null
    for (annotation in getElementAnnotations(index)) {
        when (annotation) {
            is TlConditional -> {
                if (conditional != null) {
                    multipleAnnotationsError(index, serialName, "TlConditional")
                }
                conditional = annotation
            }

            is Bits128 -> {
                if (isBits128) {
                    multipleAnnotationsError(index, serialName, "Bits128")
                }
                isBits128 = true
            }

            is Bits256 -> {
                if (isBits256) {
                    multipleAnnotationsError(index, serialName, "Bits267")
                }
                isBits256 = true
            }
        }
    }
    return TlMeta(
        index = index,
        name = getElementName(index),
        isBits128 = isBits128,
        isBits256 = isBits256,
        conditional = conditional
    )
}

private fun multipleAnnotationsError(index: Int, serialName: String, annotationType: String): Nothing {
    throw SerializationException("Multiple $annotationType annotations found for element $index in $serialName")
}
