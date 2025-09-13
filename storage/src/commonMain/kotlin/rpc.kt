@file:UseSerializers(ByteStringBase64Serializer::class)

package org.ton.kotlin.storage

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.ton.kotlin.tl.TlConstructorId
import org.ton.kotlin.tl.TlDecoder
import org.ton.kotlin.tl.TlEncoder
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@OptIn(ExperimentalSerializationApi::class)
@Serializable(with = StorageState.Serializer::class)
@KeepGeneratedSerializer
@TlConstructorId(0x3313708a)
data class StorageState(
    val willUpload: Boolean,
    val wantDownload: Boolean
) {
    internal object Serializer : KSerializer<StorageState> {
        override val descriptor: SerialDescriptor =
            SerialDescriptor($$"storage.state$boxed", generatedSerializer().descriptor)

        override fun serialize(
            encoder: Encoder,
            value: StorageState
        ) {
            if (encoder is TlEncoder) {
                encoder.encodeInt(0x3313708a) // boxed constructor id, due 'storage.State' polymorphic type, not storage.state
            }
            generatedSerializer().serialize(encoder, value)
        }

        override fun deserialize(decoder: Decoder): StorageState {
            if (decoder is TlDecoder) {
                check(decoder.decodeInt() == 0x3313708a) // boxed constructor id, due 'storage.State' polymorphic type, not storage.state
            }
            return generatedSerializer().deserialize(decoder)
        }
    }
}

@Serializable
@TlConstructorId(0x80b4fa0d)
data class StoragePiece(
    val proof: ByteString,
    val data: ByteString
)

@Serializable
@TlConstructorId(0x14ced0ee)
data class StorageTorrentInfo(
    val data: ByteString,
)

@Serializable
sealed interface StorageUpdate {
    @Serializable
    @TlConstructorId(0xce33e0b6)
    data class Init(
        val havePieces: ByteString,
        val havePiecesOffset: Int,
        val state: StorageState
    ) : StorageUpdate

    @Serializable
    @TlConstructorId(0x3bf82049)
    data class HavePieces(
        val pieceId: List<Int>
    ) : StorageUpdate

    @Serializable
    @TlConstructorId(0x05b034b5)
    data class UpdateState(
        val state: StorageState
    ) : StorageUpdate
}

@Serializable
sealed interface StorageFunction {
    @Serializable
    @TlConstructorId(0x6cf5c6a5)
    data object Pong

    @Serializable
    @TlConstructorId(0xc32b1c05)
    data object Ok

    @Serializable
    @TlConstructorId(0x44f3f211)
    data class Ping(
        val sessionId: Long
    ) : StorageFunction

    @Serializable
    @TlConstructorId(0x4d3135d2)
    data class AddUpdate(
        val sessionId: Long,
        val seqno: Int,
        val update: StorageUpdate
    ) : StorageFunction

    @Serializable
    @TlConstructorId(0x91c4962a)
    data object GetTorrentInfo : StorageFunction

    @Serializable
    @TlConstructorId(0x807ae660)
    data class GetPiece(
        val pieceId: Int
    ) : StorageFunction
}
