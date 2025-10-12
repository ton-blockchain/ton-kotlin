package org.ton.contract.jetton

import kotlinx.io.bytestring.encodeToByteString
import org.ton.block.AddrStd
import org.ton.block.Coins
import org.ton.cell.CellBuilder
import org.ton.tlb.storeTlb
import kotlin.UInt
import kotlin.test.Test
import kotlin.test.assertEquals

class JettonTransferTest {
    val transferJettonData = JettonTransfer(543u, Coins.ofNano(63546), AddrStd.parse("0QCLoGOnQ7fegGD5yLDk77QrH-I005hl_JlqMnXAyRyEJ8h0"), AddrStd.parse("0QBqtS9bao0LH0DxJYIPAGuwx8aRXuMmTxigj43E-Ef2Bl0o"),
        Coins.ofNano(789), CellBuilder().storeUInt32(0u).storeByteString("Hello, this is a comment".encodeToByteString()).endCell(), null)

    @Test
    fun testJettonTransferDataEncodeAndDecode() {
        val cell = CellBuilder().storeTlb(JettonTransfer, transferJettonData).endCell()

        val decodedJttonTransferData = JettonTransfer.loadTlb(cell)

        assertEquals(transferJettonData.queryId, decodedJttonTransferData.queryId)
        assertEquals(transferJettonData.amount, decodedJttonTransferData.amount)
        assertEquals(transferJettonData.toAddress, decodedJttonTransferData.toAddress)
        assertEquals(transferJettonData.responseAddress, decodedJttonTransferData.responseAddress)
        assertEquals(transferJettonData.forwardAmount, decodedJttonTransferData.forwardAmount)
        assertEquals(transferJettonData.forwardPayload, decodedJttonTransferData.forwardPayload)
    }

    @Test
    fun testJettonTransferTataEncode() {
        val cell = CellBuilder().storeTlb(JettonTransfer, transferJettonData)
        assertEquals(cell.toString(), "x{0F8A7EA5000000000000021F2F83A8011740C74E876FBD00C1F39161C9DF68563FC469A730CBF932D464EB819239084F001AAD4BD6DAA342C7D03C496083C01AEC31F1A457B8C993C62823E3713E11FD8184062B}\n  x{0000000048656C6C6F2C2074686973206973206120636F6D6D656E74}")
    }
}
