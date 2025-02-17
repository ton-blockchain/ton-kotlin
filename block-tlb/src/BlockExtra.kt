package org.ton.block

import kotlinx.io.bytestring.ByteString
import org.ton.cell.CellBuilder
import org.ton.cell.CellSlice
import org.ton.kotlin.account.AccountBlock
import org.ton.kotlin.account.AccountBlocks
import org.ton.kotlin.cell.CellContext
import org.ton.kotlin.dict.AugmentedDictionary
import org.ton.kotlin.dict.Dictionary
import org.ton.kotlin.dict.DictionaryKeyCodec
import org.ton.tlb.CellRef
import org.ton.tlb.TlbCodec
import org.ton.tlb.asRef

/**
 *  A list of inbound messages.
 */
public typealias InMsgDescr = AugmentedDictionary<ByteString, ImportFees, InMsg>

/**
 * A list of outbound messages.
 */
public typealias OutMsgDescr = AugmentedDictionary<ByteString, CurrencyCollection, OutMsg>

/**
 * Block extra info
 *
 * @see [Block]
 */
public data class BlockExtra(
    /**
     * Inbound message description.
     */
    val inMsgDescription: CellRef<InMsgDescr> = EMPTY_IN_MSG_DESCR_REF,

    /**
     * Outbound message description.
     */
    val outMsgDescription: CellRef<OutMsgDescr> = EMPTY_OUT_MSG_DESCR_REF,

    /**
     * Block transactions info.
     */
    val accountBlocks: CellRef<AccountBlocks> = EMPTY_ACCOUNT_BLOCKS_REF,

    /**
     * Random generator seed.
     */
    val randSeed: ByteString = ByteString(*ByteArray(32)),

    /**
     * Public key of the collator who produced this block.
     */
    val createdBy: ByteString = ByteString(*ByteArray(32)),

    /**
     * Additional block content.
     */
    val custom: CellRef<McBlockExtra>? = null
) {
    public fun loadCustom(context: CellContext = CellContext.EMPTY): McBlockExtra? =
        custom?.load(context)

    public fun loadInMsgDescription(context: CellContext = CellContext.EMPTY): InMsgDescr =
        inMsgDescription.load(context)

    public fun loadOutMsgDescription(context: CellContext = CellContext.EMPTY): OutMsgDescr =
        outMsgDescription.load(context)

    public fun loadAccountBlocks(context: CellContext = CellContext.EMPTY): AccountBlocks =
        accountBlocks.load(context)

    public companion object : TlbCodec<BlockExtra> by BlockExtraTlbCodec {
        public val EMPTY_IN_MSG_DESCR_REF: CellRef<InMsgDescr> by lazy {
            CellRef(EMPTY_IN_MSG_DESCR, IN_MSG_DESCR_CODEC)
        }

        public val EMPTY_OUT_MSG_DESCR_REF: CellRef<OutMsgDescr> by lazy {
            CellRef(EMPTY_OUT_MSG_DESCR, OUT_MSG_DESCR_CODEC)
        }

        public val EMPTY_ACCOUNT_BLOCKS_REF: CellRef<AccountBlocks> by lazy {
            CellRef(EMPTY_ACCOUNT_BLOCKS, ACCOUNT_BLOCKS_CODEC)
        }
    }
}

private val IMPORT_FEES_IN_MSG_CODEC = TlbCodec.pair(ImportFees, InMsg)
private val CURRENCY_COLLECTION_OUT_MSG_CODEC = TlbCodec.pair(CurrencyCollection, OutMsg)
private val CURRENCY_COLLECTION_ACCOUNT_BLOCK_CODEC = TlbCodec.pair(CurrencyCollection, AccountBlock)

private val EMPTY_IN_MSG_DESCR = InMsgDescr(
    Dictionary(null, DictionaryKeyCodec.BYTE_STRING_32, IMPORT_FEES_IN_MSG_CODEC),
    ImportFees.ZERO
)

private val EMPTY_OUT_MSG_DESCR = OutMsgDescr(
    Dictionary(null, DictionaryKeyCodec.BYTE_STRING_32, CURRENCY_COLLECTION_OUT_MSG_CODEC),
    CurrencyCollection.ZERO
)

private val EMPTY_ACCOUNT_BLOCKS = AccountBlocks(
    Dictionary(null, DictionaryKeyCodec.BYTE_STRING_32, CURRENCY_COLLECTION_ACCOUNT_BLOCK_CODEC),
    CurrencyCollection.ZERO
)

private val IN_MSG_DESCR_CODEC = AugmentedDictionary.tlbCodec(
    DictionaryKeyCodec.BYTE_STRING_32, ImportFees, IMPORT_FEES_IN_MSG_CODEC
)
private val OUT_MSG_DESCR_CODEC = AugmentedDictionary.tlbCodec(
    DictionaryKeyCodec.BYTE_STRING_32, CurrencyCollection, CURRENCY_COLLECTION_OUT_MSG_CODEC
)
private val ACCOUNT_BLOCKS_CODEC = AugmentedDictionary.tlbCodec(
    DictionaryKeyCodec.BYTE_STRING_32, CurrencyCollection, CURRENCY_COLLECTION_ACCOUNT_BLOCK_CODEC
)

private object BlockExtraTlbCodec : TlbCodec<BlockExtra> {
    const val TAG = 0x4a33f6fd

    override fun storeTlb(builder: CellBuilder, value: BlockExtra, context: CellContext) {
        builder.storeUInt(TAG, 32)
        builder.storeRef(value.inMsgDescription.cell)
        builder.storeRef(value.outMsgDescription.cell)
        builder.storeRef(value.accountBlocks.cell)
        builder.storeByteString(value.randSeed)
        builder.storeByteString(value.createdBy)
        builder.storeNullableRef(value.custom?.cell)
    }

    override fun loadTlb(
        slice: CellSlice,
        context: CellContext,
    ): BlockExtra {
        val tag = slice.loadUInt(32).toInt()
        require(tag == TAG) { "Invalid block_extra tag: ${tag.toHexString()}, expected: ${TAG.toHexString()}" }

        val inMsgDescr = slice.loadRef().asRef(IN_MSG_DESCR_CODEC)
        val outMsgDescr = slice.loadRef().asRef(OUT_MSG_DESCR_CODEC)
        val accountBlocks = slice.loadRef().asRef(ACCOUNT_BLOCKS_CODEC)
        val randSeed = slice.loadByteString(32)
        val createdBy = slice.loadByteString(32)
        val custom = slice.loadNullableRef()?.asRef(McBlockExtra)

        return BlockExtra(inMsgDescr, outMsgDescr, accountBlocks, randSeed, createdBy, custom)
    }
}
