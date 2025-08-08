@file:Suppress("OPT_IN_USAGE")

package org.ton.kotlin.tl

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.internal.AbstractPolymorphicSerializer

@SerialInfo
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class TlConstructorId(
    val id: Long
)

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
annotation class TlConditional(
    val field: String,
    val mask: Int
)

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
annotation class Bits128

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
annotation class Bits256

@SerialInfo
@Target(AnnotationTarget.PROPERTY)
annotation class TlFixedSize(
    val value: Int = -1,
)


internal fun SerialDescriptor.getTlConstructorId(): Int {
    for (i in annotations.indices) {
        val annotation = annotations[i]
        if (annotation is TlConstructorId) {
            return annotation.id.toInt()
        }
    }
    error("No TLConstructorId annotation found for $serialName")
}


// TODO: use caching inside TL
@OptIn(InternalSerializationApi::class)
internal fun AbstractPolymorphicSerializer<*>.constructorIdToSerialName(tl: TL): Map<Int, String> {
    val variantsDescriptor = descriptor.getElementDescriptor(1)
    val variantsCount = variantsDescriptor.elementsCount
    val result = HashMap<Int, String>(variantsCount, 1.0f)
    for (variantIndex in 0 until variantsCount) {
        val variantDescriptor = variantsDescriptor.getElementDescriptor(variantIndex)
        val constructorId = variantDescriptor.getTlConstructorId()
        result[constructorId] = variantDescriptor.serialName
    }
    return result
}
