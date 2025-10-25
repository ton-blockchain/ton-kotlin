@file:UseSerializers(
    ByteStringBase64Serializer::class,
    BigIntAsHexStringSerializer::class
)

package org.ton.sdk.toncenter.model

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.bigint.BigInt
import org.ton.sdk.tl.serializers.ByteStringBase64Serializer
import org.ton.sdk.toncenter.internal.serializers.BigIntAsHexStringSerializer

@Serializable
public sealed class TonCenterStackEntry<T> {
    public abstract val value: T

    @Serializable
    @SerialName("num")
    public class Num(
        public override val value: BigInt
    ) : TonCenterStackEntry<BigInt>()

    @Serializable
    @SerialName("cell")
    public class Cell(
        public override val value: ByteString
    ) : TonCenterStackEntry<ByteString>()

    @Serializable
    @SerialName("slice")
    public class Slice(
        public override val value: ByteString
    ) : TonCenterStackEntry<ByteString>()
}
