package org.ton.contract.wallet

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.decodeToString
import org.ton.api.pk.PrivateKey
import org.ton.api.pub.PublicKey
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.contract.CellStringTlbConstructor
import org.ton.tlb.TlbCombinator
import org.ton.tlb.TlbConstructor
import org.ton.tlb.loadTlb
import org.ton.tlb.providers.TlbCombinatorProvider
import org.ton.tlb.providers.TlbConstructorProvider
import org.ton.tlb.storeTlb

public sealed interface MessageText {
    public data class Raw(
        public val text: String
    ) : MessageText {
        public fun encrypt(publicKey: PublicKey): Encrypted {
            val encrypted = publicKey.encryptToByteArray(text.encodeToByteArray())
            return Encrypted(ByteString(*encrypted))
        }

        public companion object : TlbConstructorProvider<Raw> by TextTlbConstructor
    }

    public data class Encrypted(
        public val text: ByteString
    ) : MessageText {
        public fun decrypt(privateKey: PrivateKey): Raw {
            val decrypted = privateKey.decryptToByteArray(text.toByteArray())
            return Raw(decrypted.decodeToString())
        }

        public companion object : TlbConstructorProvider<Encrypted> by EncryptedTextTlbConstructor
    }

    public companion object : TlbCombinatorProvider<MessageText> by MessageTextTlbCombinator
}

private object MessageTextTlbCombinator : TlbCombinator<MessageText>(
    MessageText::class,
    MessageText.Raw::class to TextTlbConstructor,
    MessageText.Encrypted::class to EncryptedTextTlbConstructor
)

private object TextTlbConstructor : TlbConstructor<MessageText.Raw>(
    "raw#00000000 text:BitString = MessageText"
) {
    override fun loadTlb(cellSlice: CellSlice): MessageText.Raw {
        val text = cellSlice.loadTlb(CellStringTlbConstructor)
        return MessageText.Raw(text.decodeToString())
    }

    override fun storeTlb(cellBuilder: CellBuilder, value: MessageText.Raw) {
        cellBuilder.storeTlb(CellStringTlbConstructor, ByteString(*value.text.encodeToByteArray()))
    }
}

private object EncryptedTextTlbConstructor : TlbConstructor<MessageText.Encrypted>(
    "encrypted#00000001 text:BitString = MessageText"
) {
    override fun loadTlb(cellSlice: CellSlice): MessageText.Encrypted {
        val text = cellSlice.loadTlb(CellStringTlbConstructor)
        return MessageText.Encrypted(text)
    }

    override fun storeTlb(cellBuilder: CellBuilder, value: MessageText.Encrypted) {
        cellBuilder.storeTlb(CellStringTlbConstructor, value.text)
    }
}
