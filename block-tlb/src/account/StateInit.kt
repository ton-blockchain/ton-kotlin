package org.ton.kotlin.account

import kotlinx.io.bytestring.ByteString
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.block.AddrStd
import org.ton.kotlin.cell.*
import org.ton.kotlin.dict.Dictionary
import org.ton.kotlin.dict.DictionaryKeyCodec
import org.ton.kotlin.tlb.TlbCodec

/**
 * Deployed account state.
 *
 * @see [AccountState]
 */
public data class StateInit(
    /**
     * Optional split depth for large smart contracts.
     */
    val splitDepth: Int? = null,

    /**
     * Optional special contract flags.
     */
    val special: TickTockFlags? = null,

    /**
     * Optional contract code.
     */
    val code: Cell? = null,

    /**
     * Optional contract data.
     */
    val data: Cell? = null,

    /**
     * Libraries used in smart-contract.
     */
    val library: Dictionary<ByteString, SimpleLib> = Dictionary(null, DictionaryKeyCodec.BYTE_STRING_32, SimpleLib)
) : CellSizeable {
    public constructor(
        code: Cell? = null,
        data: Cell? = null,
        library: Dictionary<ByteString, SimpleLib> = Dictionary(null, DictionaryKeyCodec.BYTE_STRING_32, SimpleLib),
        splitDepth: Int? = null,
        special: TickTockFlags? = null
    ) : this(
        splitDepth,
        special,
        code,
        data,
        library
    )

    override val cellSize: CellSize
        get() = CellSize(
            bits = (1 + if (splitDepth != null) 5 else 0) + (1 + if (special != null) 2 else 0) + 3,
            refs = (if (code != null) 1 else 0) + (if (data != null) 1 else 0) + (if (library.isEmpty()) 0 else 1)
        )

    private var hash: BitString = BitString.empty()

    public fun address(workchain: Int = 0): AddrStd {
        var hash = hash
        if (hash.isEmpty()) {
            hash = toCell().hash()
            this.hash = hash
        }
        return AddrStd(workchain, hash)
    }

    public fun toCell(context: CellContext = CellContext.EMPTY): Cell = buildCell(context) {
        StateInitTlbCodec.storeTlb(this, this@StateInit, CellContext.EMPTY)
    }

    public companion object : TlbCodec<StateInit> by StateInitTlbCodec
}

private object StateInitTlbCodec : TlbCodec<StateInit> {

    override fun storeTlb(
        builder: CellBuilder,
        value: StateInit,
        context: CellContext
    ) {
        if (value.splitDepth == null && value.special == null) { // fast path
            var flags = 0
            if (value.code != null) {
                flags = flags or 0b00100
                builder.storeRef(value.code)
            }
            if (value.data != null) {
                flags = flags or 0b00010
                builder.storeRef(value.data)
            }
            val libraryCell = value.library.cell
            if (libraryCell != null) {
                flags = flags or 0b00001
                builder.storeRef(libraryCell)
            }
            builder.storeUInt(flags, 5)
        }

        if (value.splitDepth != null) {
            builder.storeUInt(0b1_00000 or value.splitDepth, 6)
        } else {
            builder.storeBoolean(false)
        }
        if (value.special != null) {
            builder.storeBoolean(true)
            TickTockFlags.storeTlb(builder, value.special, context)
        } else {
            builder.storeBoolean(false)
        }
        builder.storeNullableRef(value.code)
        builder.storeNullableRef(value.data)
        builder.storeNullableRef(value.library.cell)
    }

    override fun loadTlb(
        slice: CellSlice,
        context: CellContext
    ): StateInit {
        if (slice.remainingBits == 5) { // fast path
            val flags = slice.loadUInt(5).toInt()
            require(flags and 0b11000 == 0) { "Invalid StateInit" }
            val code = if (flags and 0b00100 != 0) slice.loadRef() else null
            val data = if (flags and 0b00010 != 0) slice.loadRef() else null
            val library = if (flags and 0b00001 != 0) slice.loadRef() else null
            return StateInit(code, data, Dictionary(library, DictionaryKeyCodec.BYTE_STRING_32, SimpleLib))
        }

        val splitDepth = if (slice.loadBoolean()) {
            slice.loadUInt(5).toInt()
        } else {
            null
        }
        val special = if (slice.loadBoolean()) {
            TickTockFlags.loadTlb(slice, context)
        } else {
            null
        }
        val code = slice.loadNullableRef()
        val data = slice.loadNullableRef()
        val library = Dictionary(slice.loadNullableRef(), DictionaryKeyCodec.BYTE_STRING_32, SimpleLib)
        return StateInit(splitDepth, special, code, data, library)
    }
}
