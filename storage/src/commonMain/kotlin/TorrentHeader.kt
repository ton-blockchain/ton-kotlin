package org.ton.kotlin.storage

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.KeepGeneratedSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.kotlin.tl.TlConstructorId
import org.ton.kotlin.tl.TlDecoder
import org.ton.kotlin.tl.TlEncoder

@Serializable
sealed interface FecInfo {
    @Serializable
    @TlConstructorId(0xc82a1964)
    data object None : FecInfo
}

@OptIn(ExperimentalSerializationApi::class)
@Serializable(TorrentHeaderSerializer::class)
@KeepGeneratedSerializer
@TlConstructorId(0x9128aab7)
data class TorrentHeader(
    val filesCount: Int,
    val totalNamesSize: Long,
    val totalDataSize: Long,
    val fecInfo: FecInfo = FecInfo.None,
    val dirName: String,
    private val dataIndex: List<Long>,
    private val names: List<String>
) {
    fun getName(index: Int): String {
        return names[index]
    }
}

private object TorrentHeaderSerializer : KSerializer<TorrentHeader> {
    override val descriptor: SerialDescriptor = TorrentHeader.generatedSerializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: TorrentHeader
    ) {
        if (encoder !is TlEncoder) {
            return TorrentHeader.generatedSerializer().serialize(encoder, value)
        }
        TODO()
    }

    override fun deserialize(decoder: Decoder): TorrentHeader {
        if (decoder !is TlDecoder) {
            return TorrentHeader.generatedSerializer().deserialize(decoder)
        }
        val filesCount = decoder.decodeInt()
        val totalNamesSize = decoder.decodeLong()
        val totalDataSize = decoder.decodeLong()
        val fecType = decoder.decodeSerializableValue(FecInfo.serializer())
        val dirNameSize = decoder.decodeInt()
        val dirName = decoder.decodeByteArray(dirNameSize).decodeToString()
        val nameIndex = LongArray(filesCount) {
            decoder.decodeLong()
        }
        val dataIndex = List(filesCount) {
            decoder.decodeLong()
        }
        val names = List(filesCount) {
            val size = (nameIndex[it] - if (it == 0) 0 else nameIndex[it - 1]).toInt()
            decoder.decodeByteArray(size).decodeToString()
        }
        return TorrentHeader(
            filesCount, totalNamesSize, totalDataSize, fecType, dirName, dataIndex, names
        )
    }
}
