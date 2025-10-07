@file:Suppress("OPT_IN_USAGE", "PropertyName")

package org.ton.api.validator.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.api.tonnode.TonNodeBlockIdExt
import org.ton.tl.*

@Serializable
@SerialName("validator.config.global")
@JsonClassDiscriminator("@type")
public data class ValidatorConfigGlobal(
    @SerialName("zero_state")
    val zeroState: TonNodeBlockIdExt = TonNodeBlockIdExt(),
    @SerialName("init_block")
    val initBlock: TonNodeBlockIdExt = TonNodeBlockIdExt(),
    @SerialName("hardforks")
    val hardforks: Collection<TonNodeBlockIdExt> = listOf()
) {
    public companion object : TlCodec<ValidatorConfigGlobal> by ValidatorConfigGlobalTlConstructor
}

private object ValidatorConfigGlobalTlConstructor : TlConstructor<ValidatorConfigGlobal>(
    schema = "validator.config.global zero_state:tonNode.blockIdExt init_block:tonNode.blockIdExt hardforks:(vector tonNode.blockIdExt) = validator.config.Global"
) {
    override fun encode(writer: TlWriter, value: ValidatorConfigGlobal) = writer {
        writer.write(TonNodeBlockIdExt, value.zeroState)
        writer.write(TonNodeBlockIdExt, value.initBlock)
        writer.writeVector(value.hardforks) {
            writer.write(TonNodeBlockIdExt, it)
        }
    }

    override fun decode(reader: TlReader): ValidatorConfigGlobal = reader {
        val zeroState = read(TonNodeBlockIdExt)
        val initBlock = read(TonNodeBlockIdExt)
        val hardforks = readVector {
            read(TonNodeBlockIdExt)
        }
        ValidatorConfigGlobal(zeroState, initBlock, hardforks)
    }
}
