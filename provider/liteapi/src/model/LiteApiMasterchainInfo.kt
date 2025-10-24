package org.ton.kotlin.provider.liteapi.model

import kotlinx.io.bytestring.ByteString
import kotlinx.serialization.Serializable
import org.ton.kotlin.tl.Bits256
import org.ton.kotlin.tl.TlConstructorId
import org.ton.kotlin.tl.serializers.ByteStringBase64Serializer

@Serializable
@TlConstructorId(0x85832881)
public data class LiteApiMasterchainInfo(
    val last: LiteApiBlockIdExt,
    @Serializable(ByteStringBase64Serializer::class)
    @Bits256
    val stateRootHash: ByteString,
    val init: LiteApiZeroStateIdExt,
)
