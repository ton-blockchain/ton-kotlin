package org.ton.kotlin.contract.wallet

import kotlinx.io.bytestring.ByteString
import kotlinx.io.bytestring.decodeToString
import org.ton.kotlin.adnl.pk.PrivateKey
import org.ton.kotlin.adnl.pub.PublicKey
import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.contract.CellStringTlbConstructor
import org.ton.kotlin.tlb.TlbCombinator
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.loadTlb
import org.ton.kotlin.tlb.providers.TlbCombinatorProvider
import org.ton.kotlin.tlb.providers.TlbConstructorProvider
import org.ton.kotlin.tlb.storeTlb

public sealed interface MessageText {
    public data class Raw(
        public val text: String
    ) : MessageText {
        public fun encrypt(publicKey: PublicKey): Encrypted {
            val encrypted = publicKey.encrypt(text.encodeToByteArray())
            return Encrypted(ByteString(*encrypted))
        }

        public companion object : TlbConstructorProvider<Raw> by TextTlbConstructor
    }

    public data class Encrypted(
        public val text: ByteString
    ) : MessageText {
        public fun decrypt(privateKey: PrivateKey): Raw {
            val decrypted = privateKey.decrypt(text.toByteArray())
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
