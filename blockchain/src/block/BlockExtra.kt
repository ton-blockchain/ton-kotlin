package org.ton.sdk.blockchain.block

import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.dict.AugmentedDictionary
import org.ton.sdk.blockchain.currency.CurrencyCollection
import org.ton.sdk.blockchain.message.ImportFees
import org.ton.sdk.blockchain.message.InMsg
import org.ton.sdk.blockchain.message.OutMsg
import org.ton.tlb.CellRef

public typealias InMsgDescr = AugmentedDictionary<ByteString, ImportFees, InMsg>
public typealias OutMsgDescr = AugmentedDictionary<ByteString, CurrencyCollection, OutMsg>
public typealias AccountBlocks = AugmentedDictionary<ByteString, CurrencyCollection, AccountBlock>

public class BlockExtra(
    public val inMsgDescription: CellRef<InMsgDescr>,
    public val outMsgDescription: CellRef<OutMsgDescr>,
    public val accountBlocks: CellRef<AccountBlocks>,
    public val randSeed: ByteString,
    public val createdBy: ByteString,
    public val custom: CellRef<McBlockExtra>?
)
