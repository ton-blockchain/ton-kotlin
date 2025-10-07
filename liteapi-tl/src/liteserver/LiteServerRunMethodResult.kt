package org.ton.lite.api.liteserver

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.tl.*
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic

@Serializable
@SerialName("liteServer.runMethodResult")
public data class LiteServerRunMethodResult internal constructor(
    @get:JvmName("mode")
    val mode: Int,

    @get:JvmName("id")
    val id: TonNodeBlockIdExt,

    @SerialName("shardblk")
    @get:JvmName("shardBlock")
    val shardBlock: TonNodeBlockIdExt,

    @SerialName("shard_proof")
    @get:JvmName("shardProof")
    @Serializable(ByteStringBase64Serializer::class)
    val shardProof: ByteString?,

    @get:JvmName("proof")
    @Serializable(ByteStringBase64Serializer::class)
    val proof: ByteString?,

    @SerialName("state_proof")
    @get:JvmName("stateProof")
    @Serializable(ByteStringBase64Serializer::class)
    val stateProof: ByteString?,

    @SerialName("init_c7")
    @get:JvmName("initC7")
    @Serializable(ByteStringBase64Serializer::class)
    val initC7: ByteString?,

    @SerialName("lib_extras")
    @get:JvmName("libExtras")
    @Serializable(ByteStringBase64Serializer::class)
    val libExtras: ByteString?,

    @SerialName("exit_code")
    @get:JvmName("exitCode")
    val exitCode: Int,

    @get:JvmName("result")
    @Serializable(ByteStringBase64Serializer::class)
    val result: ByteString?
) {
    public companion object : TlCodec<LiteServerRunMethodResult> by LiteServerRunMethodResultTlConstructor {
        @JvmStatic
        public fun mode(
            hasProof: Boolean = false,
            hasStateProof: Boolean = false,
            hasResult: Boolean = false,
            hasInitC7: Boolean = false,
            hasLibExtras: Boolean = false,
        ): Int {
            var mode = 0
            if (hasProof) mode = mode or 1
            if (hasStateProof) mode = mode or 2
            if (hasResult) mode = mode or 4
            if (hasInitC7) mode = mode or 8
            if (hasLibExtras) mode = mode or 16
            return mode
        }
    }
}

private object LiteServerRunMethodResultTlConstructor : TlConstructor<LiteServerRunMethodResult>(
    schema = "liteServer.runMethodResult mode:# id:tonNode.blockIdExt shardblk:tonNode.blockIdExt shard_proof:mode.0?bytes proof:mode.0?bytes state_proof:mode.1?bytes init_c7:mode.3?bytes lib_extras:mode.4?bytes exit_code:int result:mode.2?bytes = liteServer.RunMethodResult"
) {
    override fun encode(writer: TlWriter, value: LiteServerRunMethodResult) {
        writer.writeInt(value.mode)
        val mode = value.mode
        writer.write(TonNodeBlockIdExt, value.id)
        writer.write(TonNodeBlockIdExt, value.shardBlock)
        writer.writeNullable(mode, 0, value.shardProof) { writeBytes(it) }
        writer.writeNullable(mode, 0, value.proof) { writeBytes(it) }
        writer.writeNullable(mode, 1, value.stateProof) { writeBytes(it) }
        writer.writeNullable(mode, 3, value.initC7) { writeBytes(it) }
        writer.writeNullable(mode, 4, value.libExtras) { writeBytes(it) }
        writer.writeInt(value.exitCode)
        writer.writeNullable(mode, 2, value.result) { writeBytes(it) }
    }

    override fun decode(reader: TlReader): LiteServerRunMethodResult {
        val mode = reader.readInt()
        val id = reader.read(TonNodeBlockIdExt)
        val shardblk = reader.read(TonNodeBlockIdExt)
        val shardProof = reader.readNullable(mode, 0) { readByteString() }
        val proof = reader.readNullable(mode, 0) { readByteString() }
        val stateProof = reader.readNullable(mode, 1) { readByteString() }
        val initC7 = reader.readNullable(mode, 3) { readByteString() }
        val libExtras = reader.readNullable(mode, 4) { readByteString() }
        val exitCode = reader.readInt()
        val result = reader.readNullable(mode, 2) { readByteString() }
        return LiteServerRunMethodResult(
            mode,
            id,
            shardblk,
            shardProof,
            proof,
            stateProof,
            initC7,
            libExtras,
            exitCode,
            result
        )
    }
}
