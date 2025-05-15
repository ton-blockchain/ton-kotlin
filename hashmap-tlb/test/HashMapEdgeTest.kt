package org.ton.kotlin.hashmap

import io.ktor.util.*
import org.ton.boc.BagOfCells
import org.ton.kotlin.bitstring.BitString
import org.ton.kotlin.cell.Cell
import org.ton.kotlin.cell.CellSlice
import org.ton.kotlin.tlb.constructor.UIntTlbConstructor
import kotlin.test.Test
import kotlin.test.assertEquals

class HashMapEdgeTest {
    @Test
    fun `1 - keys are correctly determined when iterating over nodes`() {
        val codec = HmEdge.tlbCodec(32, UIntTlbConstructor.int(1))
        val cellSlice =
            BagOfCells(("te6cckEBEwEAVwACASABAgIC2QMEAgm3///wYBESAgEgBQYCAWIODwIBIAcIAgHODQ0CAdQNDQIBIAkKAgEgCxACASAQDAABWAIBIA0NAAEgAgEgEBAAAdQAAUgAAfwAAdwXk+eF").decodeBase64Bytes())
                .first()
                .beginParse()
        val hashMapEdge = codec.loadTlb(cellSlice)

        val keys = hashMapEdge.map { CellSlice(it.first).loadInt(32).toInt() }.toList()
        assertEquals(
            listOf(0, 1, 9, 10, 12, 14, 15, 16, 17, 32, 34, 36, -1001, -1000),
            keys
        )
    }

    @Test
    fun `2 -keys are correctly determined when iterating over nodes`() {
        val e = HmEdge(
            label = HmlShort(UnaryZero, BitString()),
            node = HmnFork(
                left = HmEdge(
                    label = HmlLong(
                        255,
                        BitString("C20BAD98ED5E80064BD29AB119CA237CB7FB76E7686FB8A3D948722FAF487C7B_")
                    ),
                    node = HmnLeaf(Cell.of("69696969"))
                ),
                right = HmEdge(
                    label = HmlShort(UnaryZero, BitString()),
                    node = HmnFork(
                        left = HmEdge(
                            label = HmlShort(UnaryZero, BitString()),
                            node = HmnFork(
                                left = HmEdge(
                                    label = HmlLong(
                                        253,
                                        BitString("151A9BFF86DE73F761AEB4F6E1D0C4F7378BEC179A9D2A9FCD54B6585F1E744C_")
                                    ),
                                    node = HmnLeaf(Cell.of("42424242"))
                                ),
                                right = HmEdge(
                                    label = HmlLong(
                                        253,
                                        BitString("BB53E50A9E12338B2C19ADDE844A31A87FE310FD0E28B7389184AEA7FEAE2C0C_")
                                    ),
                                    node = HmnLeaf(Cell.of("69426942"))
                                )
                            )
                        ),
                        right = HmEdge(
                            label = HmlLong(
                                254,
                                BitString("2411BDE8DEB43A9F3B9CCD56613E950A260BE2CDF23DEF3B247DEB1C69F34412_")
                            ),
                            node = HmnLeaf(Cell.of("DEADBEEF"))
                        )
                    )
                )
            )
        )

        val nodes = e.toMap()

        assertEquals(
            Cell.of("42424242"),
            nodes[BitString("82A3537FF0DBCE7EEC35D69EDC3A189EE6F17D82F353A553F9AA96CB0BE3CE89")]
        )
        assertEquals(
            Cell.of("DEADBEEF"),
            nodes[BitString("C9046F7A37AD0EA7CEE73355984FA5428982F8B37C8F7BCEC91F7AC71A7CD104")]
        )
        assertEquals(
            Cell.of("69696969"),
            nodes[BitString("6105D6CC76AF400325E94D588CE511BE5BFDBB73B437DC51ECA43917D7A43E3D")]
        )
        assertEquals(
            Cell.of("69426942"),
            nodes[BitString("B76A7CA153C24671658335BBD08946350FFC621FA1C516E7123095D4FFD5C581")]
        )
    }
}
