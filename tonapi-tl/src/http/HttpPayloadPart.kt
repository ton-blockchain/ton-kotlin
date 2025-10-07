package org.ton.api.http

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.tl.*

@SerialName("http.payloadPart")
@Serializable
public data class HttpPayloadPart(
    @Serializable(ByteStringBase64Serializer::class)
    val data: ByteString,
    val trailer: Collection<HttpHeader>,
    val last: Boolean
) : TlObject<HttpPayloadPart> {
    override fun tlCodec(): TlCodec<HttpPayloadPart> = Companion

    public companion object : TlCodec<HttpPayloadPart> by HttpPayloadPartTlConstructor
}

private object HttpPayloadPartTlConstructor : TlConstructor<HttpPayloadPart>(
    schema = "http.payloadPart data:bytes trailer:(vector http.header) last:Bool = http.PayloadPart"
) {
    override fun decode(reader: TlReader): HttpPayloadPart {
        val data = reader.readByteString()
        val trailer = reader.readVector {
            read(HttpHeader)
        }
        val last = reader.readBoolean()
        return HttpPayloadPart(data, trailer, last)
    }

    override fun encode(writer: TlWriter, value: HttpPayloadPart) {
        writer.writeBytes(value.data)
        writer.writeVector(value.trailer) {
            write(HttpHeader, it)
        }
        writer.writeBoolean(value.last)
    }
}
