@file:Suppress("OPT_IN_USAGE")

package org.ton.api.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator
import org.ton.api.adnl.config.AdnlConfigGlobal
import org.ton.api.dht.config.DhtConfigGlobal
import org.ton.api.validator.config.ValidatorConfigGlobal
import org.ton.tl.*
import kotlin.jvm.JvmName

@Serializable
@SerialName("config.global")
@JsonClassDiscriminator("@type")
public data class ConfigGlobal(
    @get:JvmName("adnl")
    val adnl: AdnlConfigGlobal = AdnlConfigGlobal(),

    @get:JvmName("dht")
    val dht: DhtConfigGlobal = DhtConfigGlobal(),

    @get:JvmName("validator")
    val validator: ValidatorConfigGlobal
) : TlObject<ConfigGlobal> {
    override fun tlCodec(): TlCodec<ConfigGlobal> = Companion

    public companion object : TlCodec<ConfigGlobal> by ConfigGlobalTlConstructor
}

private object ConfigGlobalTlConstructor : TlConstructor<ConfigGlobal>(
    schema = "config.global adnl:adnl.config.global dht:dht.config.global validator:validator.config.global = config.Global"
) {
    override fun encode(output: TlWriter, value: ConfigGlobal) {
        output.write(AdnlConfigGlobal, value.adnl)
        output.write(DhtConfigGlobal, value.dht)
        output.write(ValidatorConfigGlobal, value.validator)
    }

    override fun decode(input: TlReader): ConfigGlobal {
        val adnl = input.read(AdnlConfigGlobal)
        val dht = input.read(DhtConfigGlobal)
        val validator = input.read(ValidatorConfigGlobal)
        return ConfigGlobal(adnl, dht, validator)
    }
}
