package org.ton.kotlin.cell

import kotlinx.serialization.Serializable

@Serializable
public class CellRef<T : Any>(
    public val cell: Cell
)

//private object CellRefSerializer : KSerializer<CellRef<*>> {
//    override val descriptor = SerialDescriptor("CellRef", ByteArrayBase64Serializer.descriptor)
//
//    override fun serialize(encoder: kotlinx.serialization.encoding.Encoder, value: CellRef<*>) {
//
//    }
//
//    override fun deserialize(decoder: kotlinx.serialization.encoding.Decoder): CellRef<*> {
//        if (decoder !is TlDecoder) {
//            return CellRef<Any>().generatedSerializer().deserialize(decoder)
//        }
//        TODO("Not yet implemented")
//    }
//}
