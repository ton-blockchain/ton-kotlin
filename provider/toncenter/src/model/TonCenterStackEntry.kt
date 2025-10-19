@file:UseSerializers(
    ByteStringBase64Serializer::class,
    BigIntAsHexStringSerializer::class
)

package org.ton.kotlin.provider.toncenter.model

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.bigint.BigInt
import org.ton.kotlin.provider.toncenter.internal.BigIntAsHexStringSerializer
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
public sealed class TonCenterStackEntry<T> {
    public abstract val value: T

    @Serializable
    @SerialName("num")
    public data class Num(
        public override val value: BigInt
    ) : TonCenterStackEntry<BigInt>()

    @Serializable
    @SerialName("cell")
    public data class Cell(
        public override val value: ByteString
    ) : TonCenterStackEntry<ByteString>()

    @Serializable
    @SerialName("slice")
    public data class Slice(
        public override val value: ByteString
    ) : TonCenterStackEntry<ByteString>()
}
