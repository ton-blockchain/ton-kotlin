package org.ton.api.http.functions

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.http.HttpPayloadPart
import org.ton.tl.*

@SerialName("http.getNextPayloadPart")
@Serializable
public data class HttpGetNextPayloadPart(
    @Serializable(ByteStringBase64Serializer::class)
    val id: ByteString,
    val seqno: Int,
    @SerialName("max_chunk_size")
    val maxChunkSize: Int
) : TLFunction<HttpGetNextPayloadPart, HttpPayloadPart> {
    override fun tlCodec(): TlCodec<HttpGetNextPayloadPart> = Companion
    override fun resultTlCodec(): TlCodec<HttpPayloadPart> = HttpPayloadPart

    public companion object : TlCodec<HttpGetNextPayloadPart> by HttpGetNextPayloadPartTlConstructor
}

private object HttpGetNextPayloadPartTlConstructor : TlConstructor<HttpGetNextPayloadPart>(
    schema = "http.getNextPayloadPart id:int256 seqno:int max_chunk_size:int = http.PayloadPart"
) {
    override fun decode(input: TlReader): HttpGetNextPayloadPart {
        val id = input.readByteString(32)
        val seqno = input.readInt()
        val max_chunk_size = input.readInt()
        return HttpGetNextPayloadPart(id, seqno, max_chunk_size)
    }

    override fun encode(output: TlWriter, value: HttpGetNextPayloadPart) {
        output.writeRaw(value.id)
        output.writeInt(value.seqno)
        output.writeInt(value.maxChunkSize)
    }
}
