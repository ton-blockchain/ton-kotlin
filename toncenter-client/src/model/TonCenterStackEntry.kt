@file:Suppress("INAPPLICABLE_JVM_NAME") // https://youtrack.jetbrains.com/issue/KT-31420
@file:UseSerializers(
    ByteStringBase64Serializer::class,
    BigIntAsHexStringSerializer::class
)

package org.ton.sdk.toncenter.model

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.ton.sdk.bigint.BigInt
import org.ton.sdk.tl.serializers.ByteStringBase64Serializer
import org.ton.sdk.toncenter.internal.serializers.BigIntAsHexStringSerializer
import kotlin.jvm.JvmName

@Serializable
public sealed class TonCenterStackEntry<T> {
    @get:JvmName("value")
    public abstract val value: T

    @Serializable
    @SerialName("num")
    public class Num(
        @get:JvmName("value")
        public override val value: BigInt
    ) : TonCenterStackEntry<BigInt>()

    @Serializable
    @SerialName("cell")
    public class Cell(
        @get:JvmName("value")
        public override val value: ByteString
    ) : TonCenterStackEntry<ByteString>()

    @Serializable
    @SerialName("slice")
    public class Slice(
        @get:JvmName("value")
        public override val value: ByteString
    ) : TonCenterStackEntry<ByteString>()
}
