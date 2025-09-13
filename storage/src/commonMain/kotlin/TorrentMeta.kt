package org.ton.kotlin.storage

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.kotlin.cell.BagOfCells
import org.ton.kotlin.tl.TlDecoder
import org.ton.kotlin.tl.TlEncoder

@OptIn(ExperimentalSerializationApi::class)
@Serializable(TorrentMetaSerializer::class)
@KeepGeneratedSerializer
data class TorrentMeta(
    val info: BagOfCells,
    val rootProof: BagOfCells? = null,
    val header: TorrentHeader? = null
)

private object TorrentMetaSerializer : KSerializer<TorrentMeta> {
    override val descriptor: SerialDescriptor get() = TorrentMeta.generatedSerializer().descriptor

    override fun serialize(encoder: Encoder, value: TorrentMeta) {
        if (encoder !is TlEncoder) return TorrentMeta.generatedSerializer().serialize(encoder, value)
        TODO()
    }

    override fun deserialize(decoder: Decoder): TorrentMeta {
        if (decoder !is TlDecoder) return TorrentMeta.generatedSerializer().deserialize(decoder)
        val flags = decoder.decodeInt()
        val infoBocSize = decoder.decodeInt()
        val rootProofBocSize = if (flags and 1 != 0) decoder.decodeInt() else -1
        val infoBoc = BagOfCells(decoder.decodeByteArray(infoBocSize))
        val rootProofBoc =
            if (flags and 1 != 0) BagOfCells(decoder.decodeByteArray(rootProofBocSize)) else null
        val header = if (flags and 2 != 0) decoder.decodeSerializableValue(TorrentHeader.serializer()) else null
        return TorrentMeta(infoBoc, rootProofBoc, header)
    }
}
