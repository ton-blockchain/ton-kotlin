package org.ton.block

import org.ton.bigint.toBigInt
import org.ton.bitstring.BitString
import org.ton.boc.BagOfCells
import org.ton.cell.CellBuilder
import org.ton.hashmap.HmeEmpty
import org.ton.tlb.storeTlb
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

@Ignore
class StateInitTest {
    private val SIMPLE_WALLET_R3_CODE =
        BagOfCells("b5ee9c7241010101005f0000baff0020dd2082014c97ba218201339cbab19c71b0ed44d0d31fd70bffe304e0a4f260810200d71820d70b1fed44d0d31fd3ffd15112baf2a122f901541044f910f2a2f80001d31f3120d74a96d307d402fb00ded1a4c8cb1fcbffc9ed54b5b86e42".hexToByteArray()).roots.first()
    private val stateInitCodec by lazy { StateInit.tlbCodec() }

    @Test
    fun `serialization of an example wallet StateInit`() {

        val publicKey = "4745ede03eb4ef607843359c1f206d061a5632f68caa6f63021aa23b400950fd"
        val hash = "2c49a26a126a6e3a7b1a99c5041698504cbef27fca60a230f2f475954deb07cf"

        val stateInit = StateInit(
            splitDepth = null,
            special = null,
            code = SIMPLE_WALLET_R3_CODE,
            data = CellBuilder.createCell {
                storeUInt(0, 32)
                storeBits(BitString(publicKey))
            },
            library = HmeEmpty()
        )

        val stateInitCell = CellBuilder.createCell { storeTlb(stateInitCodec, stateInit) }

        assertEquals(BitString("34_"), stateInitCell.bits)
        assertEquals(SIMPLE_WALLET_R3_CODE.bits, stateInitCell.refs[0].bits)

        stateInitCell.refs[1].parse {
            assertEquals(0.toBigInt(), loadUInt(32))
            assertEquals(BitString(publicKey), loadBits(256))
        }

        assertEquals(hash, stateInitCell.hash().toHexString().lowercase())
    }
}
