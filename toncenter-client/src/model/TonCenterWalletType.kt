package org.ton.sdk.toncenter.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.jvm.JvmStatic

@Serializable(with = TonCenterWalletType.Serializer::class)
public class TonCenterWalletType private constructor(
    public val name: String
) {
    public companion object {
        @JvmStatic
        public val WALLET_V1_R1: TonCenterWalletType = TonCenterWalletType("wallet v1 r1")

        @JvmStatic
        public val WALLET_V1_R2: TonCenterWalletType = TonCenterWalletType("wallet v1 r2")

        @JvmStatic
        public val WALLET_V1_R3: TonCenterWalletType = TonCenterWalletType("wallet v1 r3")

        @JvmStatic
        public val WALLET_V2_R1: TonCenterWalletType = TonCenterWalletType("wallet v2 r1")

        @JvmStatic
        public val WALLET_V2_R2: TonCenterWalletType = TonCenterWalletType("wallet v2 r2")

        @JvmStatic
        public val WALLET_V3_R1: TonCenterWalletType = TonCenterWalletType("wallet v3 r1")

        @JvmStatic
        public val WALLET_V3_R2: TonCenterWalletType = TonCenterWalletType("wallet v3 r2")

        @JvmStatic
        public val WALLET_V4_R1: TonCenterWalletType = TonCenterWalletType("wallet v4 r1")

        @JvmStatic
        public val WALLET_V4_R2: TonCenterWalletType = TonCenterWalletType("wallet v4 r2")

        @JvmStatic
        public val WALLET_V5_BETA: TonCenterWalletType = TonCenterWalletType("wallet v5 beta")

        @JvmStatic
        public val WALLET_V5_R1: TonCenterWalletType = TonCenterWalletType("wallet v5 r1")

        @JvmStatic
        public fun byName(name: String): TonCenterWalletType {
            return when (name) {
                "wallet v1 r1" -> WALLET_V1_R1
                "wallet v1 r2" -> WALLET_V1_R2
                "wallet v1 r3" -> WALLET_V1_R3
                "wallet v2 r1" -> WALLET_V2_R1
                "wallet v2 r2" -> WALLET_V2_R2
                "wallet v3 r1" -> WALLET_V3_R1
                "wallet v3 r2" -> WALLET_V3_R2
                "wallet v4 r1" -> WALLET_V4_R1
                "wallet v4 r2" -> WALLET_V4_R2
                "wallet v5 beta" -> WALLET_V5_BETA
                "wallet v5 r1" -> WALLET_V5_R1
                else -> TonCenterWalletType(name)
            }
        }
    }

    private object Serializer : KSerializer<TonCenterWalletType> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("TonCenterWalletType", PrimitiveKind.STRING)

        override fun serialize(
            encoder: Encoder,
            value: TonCenterWalletType
        ) {
            encoder.encodeString(value.name)
        }

        override fun deserialize(decoder: Decoder): TonCenterWalletType {
            return byName(decoder.decodeString())
        }
    }
}
