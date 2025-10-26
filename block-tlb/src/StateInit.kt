package org.ton.block

import kotlinx.serialization.SerialName
import org.ton.bitstring.BitString
import org.ton.cell.*
import org.ton.hashmap.HashMapE
import org.ton.hashmap.HmeEmpty
import org.ton.kotlin.cell.CellSize
import org.ton.kotlin.cell.CellSizeable
import org.ton.tlb.*
import org.ton.tlb.TlbConstructor
import org.ton.tlb.constructor.AnyTlbConstructor
import org.ton.tlb.constructor.UIntTlbConstructor
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic


public data class StateInit(
    @SerialName("split_depth")
    @get:JvmName("splitDepth")
    val splitDepth: Maybe<UInt>,

    @get:JvmName("special")
    val special: Maybe<TickTock>,

    @get:JvmName("code")
    val code: Maybe<CellRef<Cell>>,

    @get:JvmName("data")
    val data: Maybe<CellRef<Cell>>,

    @get:JvmName("library")
    val library: HashMapE<SimpleLib>
) : TlbObject, CellSizeable {
    public constructor(
        code: Cell? = null,
        data: Cell? = null,
        library: HashMapE<SimpleLib> = HashMapE.of(),
        splitDepth: UInt? = null,
        special: TickTock? = null
    ) : this(
        splitDepth.toMaybe(),
        special.toMaybe(),
        code?.let { CellRef(cell = it, AnyTlbConstructor) }.toMaybe(),
        data?.let { CellRef(cell = it, AnyTlbConstructor) }.toMaybe(),
        library
    )

    override val cellSize: CellSize
        get() = CellSize(
            bits = (1 + if (splitDepth.value != null) 5 else 0) + (1 + if (special.value != null) 2 else 0) + 3,
            refs = (if (code.value != null) 1 else 0) + (if (data.value != null) 1 else 0) + (if (library is HmeEmpty) 0 else 1)
        )

    private var hash: BitString = BitString.empty()

    public fun address(workchain: Int = 0): AddrStd {
        var hash = hash
        if (hash == BitString.empty()) {
            hash = buildCell {
                storeTlb(StateInitTlbConstructor, this@StateInit)
            }.hash()
            this.hash = hash
        }
        return AddrStd(workchain, hash)
    }

    public fun toCell(): Cell = buildCell {
        storeTlb(StateInitTlbConstructor, this@StateInit)
    }

    override fun toString(): String = print().toString()

    override fun print(printer: TlbPrettyPrinter): TlbPrettyPrinter = printer {
        type {
            field("split_depth", splitDepth)
            field("special", special)
            field("code", code)
            field("data", data)
            field("library", library)
        }
    }

    public companion object : TlbCodec<StateInit> by StateInitTlbConstructor {
        @JvmStatic
        public fun tlbCodec(): TlbConstructor<StateInit> = StateInitTlbConstructor
    }
}

private object StateInitTlbConstructor : TlbConstructor<StateInit>(
    schema = "_ split_depth:(Maybe (## 5)) special:(Maybe TickTock) code:(Maybe ^Cell) data:(Maybe ^Cell) library:(HashmapE 256 SimpleLib) = StateInit;"
) {
    private val Maybe5 = Maybe(UIntTlbConstructor.int(5))
    private val MaybeTickTock = Maybe(TickTock)
    private val MaybeCell = Maybe(CellRef.tlbCodec(AnyTlbConstructor))
    private val Library = HashMapE.tlbCodec(256, SimpleLib)

    override fun storeTlb(
        builder: CellBuilder, value: StateInit
    ) = builder {
        storeTlb(Maybe5, value.splitDepth)
        storeTlb(MaybeTickTock, value.special)
        storeTlb(MaybeCell, value.code)
        storeTlb(MaybeCell, value.data)
        storeTlb(Library, value.library)
    }

    override fun loadTlb(
        slice: CellSlice
    ): StateInit = slice {
        val splitDepth = loadTlb(Maybe5)
        val special = loadTlb(MaybeTickTock)
        val code = loadTlb(MaybeCell)
        val data = loadTlb(MaybeCell)
        val library = loadTlb(Library)
        StateInit(splitDepth, special, code, data, library)
    }
}
