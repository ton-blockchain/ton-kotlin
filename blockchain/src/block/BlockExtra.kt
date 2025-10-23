package org.ton.kotlin.blockchain.block

import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.blockchain.currency.CurrencyCollection
import org.ton.kotlin.blockchain.message.ImportFees
import org.ton.kotlin.blockchain.message.InMsg
import org.ton.kotlin.blockchain.message.OutMsg
import org.ton.kotlin.cell.CellRef
import org.ton.kotlin.dict.AugmentedDictionary

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
