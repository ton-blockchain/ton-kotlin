package org.ton.kotlin.block

import org.ton.kotlin.cell.CellBuilder
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.*
import org.ton.kotlin.tlb.TlbConstructor
import org.ton.kotlin.tlb.providers.TlbCombinatorProvider
import org.ton.kotlin.tlb.providers.TlbConstructorProvider

public sealed interface SmcCapability {
    public object MethodSeqno : SmcCapability, TlbConstructorProvider<MethodSeqno> by methodSeqno
    public object MethodPubKey : SmcCapability, TlbConstructorProvider<MethodPubKey> by methodPubKey
    public object IsWallet : SmcCapability, TlbConstructorProvider<IsWallet> by isWallet
    public data class Name(
        val name: Text
    ) : SmcCapability {
        public companion object : TlbConstructorProvider<Name> by capName
    }

    public companion object : TlbCombinatorProvider<SmcCapability> by SmcCapabilityTlbCombinator
}

private val methodSeqno = ObjectTlbConstructor(
    SmcCapability.MethodSeqno,
    schema = "cap_method_seqno#5371 = SmcCapability;",
)
private val methodPubKey = ObjectTlbConstructor(
    SmcCapability.MethodPubKey,
    schema = "cap_method_pubkey#71f4 = SmcCapability;",
)
private val isWallet = ObjectTlbConstructor(
    SmcCapability.IsWallet,
    schema = "cap_is_wallet#2177 = SmcCapability;",
)
private val capName = object : TlbConstructor<SmcCapability.Name>(
    schema = "cap_name#ff name:Text = SmcCapability;"
) {
    override fun loadTlb(cellSlice: CellSlice): SmcCapability.Name {
        val name = cellSlice.loadTlb(Text)
        return SmcCapability.Name(name)
    }

    override fun storeTlb(cellBuilder: CellBuilder, value: SmcCapability.Name) {
        cellBuilder.storeTlb(Text, value.name)
    }
}

private object SmcCapabilityTlbCombinator : TlbCombinator<SmcCapability>(
    SmcCapability::class,
    SmcCapability.MethodSeqno::class to methodSeqno,
    SmcCapability.MethodPubKey::class to methodPubKey,
    SmcCapability.IsWallet::class to isWallet,
    SmcCapability.Name::class to capName,
)
