package org.ton.kotlin.block

import org.ton.kotlin.bitstring.BitString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class MsgAddressIntTest {
    @Test
    fun `parse raw addresses`() {
        val addr1 =
            AddrStd.parseRaw("-1:3333333333333333333333333333333333333333333333333333333333333333")
        assertEquals(-1, addr1.workchainId)
        assertEquals(BitString("3333333333333333333333333333333333333333333333333333333333333333"), addr1.address)

        val addr2 =
            AddrStd.parseRaw("0:83dfd552e63729b472fcbcc8c45ebcc6691702558b68ec7527e1ba403a0f31a8")
        assertEquals(0, addr2.workchainId)
        assertEquals(BitString("83dfd552e63729b472fcbcc8c45ebcc6691702558b68ec7527e1ba403a0f31a8"), addr2.address)

        val addr3 =
            AddrStd.parseRaw("-1:dd24c4a1f2b88f8b7053513b5cc6c5a31bc44b2a72dcb4d8c0338af0f0d37ec5")
        assertEquals(-1, addr3.workchainId)
        assertEquals(BitString("dd24c4a1f2b88f8b7053513b5cc6c5a31bc44b2a72dcb4d8c0338af0f0d37ec5"), addr3.address)
    }

    @Test
    fun `parse user-friendly base64url addresses`() {
        val bounceableAddr1 =
            AddrStd.parseUserFriendly("Ef8zMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzM0vF")
        assertEquals(-1, bounceableAddr1.workchainId)
        assertEquals(
            BitString("3333333333333333333333333333333333333333333333333333333333333333"),
            bounceableAddr1.address
        )

        val nonBounceableAddr1 =
            AddrStd.parseUserFriendly("Uf8zMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMxYA")
        assertEquals(-1, nonBounceableAddr1.workchainId)
        assertEquals(
            BitString("3333333333333333333333333333333333333333333333333333333333333333"),
            nonBounceableAddr1.address
        )

        assertEquals(bounceableAddr1, nonBounceableAddr1)

        val bounceableAddr2 =
            AddrStd.parseUserFriendly("EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N")
        assertEquals(0, bounceableAddr2.workchainId)
        assertEquals(
            BitString("83dfd552e63729b472fcbcc8c45ebcc6691702558b68ec7527e1ba403a0f31a8"),
            bounceableAddr2.address
        )

        val nonBounceableAddr2 =
            AddrStd.parseUserFriendly("UQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqEBI")
        assertEquals(0, nonBounceableAddr2.workchainId)
        assertEquals(
            BitString("83dfd552e63729b472fcbcc8c45ebcc6691702558b68ec7527e1ba403a0f31a8"),
            nonBounceableAddr2.address
        )

        assertEquals(bounceableAddr2, nonBounceableAddr2)


        val bounceableAddr3 =
            AddrStd.parseUserFriendly("Ef_dJMSh8riPi3BTUTtcxsWjG8RLKnLctNjAM4rw8NN-xWdr")
        assertEquals(-1, bounceableAddr3.workchainId)
        assertEquals(
            BitString("dd24c4a1f2b88f8b7053513b5cc6c5a31bc44b2a72dcb4d8c0338af0f0d37ec5"),
            bounceableAddr3.address
        )

        val nonBounceableAddr3 =
            AddrStd.parseUserFriendly("Uf_dJMSh8riPi3BTUTtcxsWjG8RLKnLctNjAM4rw8NN-xTqu")
        assertEquals(-1, nonBounceableAddr3.workchainId)
        assertEquals(
            BitString("dd24c4a1f2b88f8b7053513b5cc6c5a31bc44b2a72dcb4d8c0338af0f0d37ec5"),
            nonBounceableAddr3.address
        )

        assertEquals(bounceableAddr3, nonBounceableAddr3)

        val bounceableAddr4 =
            AddrStd.parseUserFriendly("Ef/dJMSh8riPi3BTUTtcxsWjG8RLKnLctNjAM4rw8NN+xWdr")
        assertEquals(-1, bounceableAddr4.workchainId)
        assertEquals(
            BitString("dd24c4a1f2b88f8b7053513b5cc6c5a31bc44b2a72dcb4d8c0338af0f0d37ec5"),
            bounceableAddr4.address
        )

        val nonBounceableAddr4 =
            AddrStd.parseUserFriendly("Uf/dJMSh8riPi3BTUTtcxsWjG8RLKnLctNjAM4rw8NN+xTqu")
        assertEquals(-1, nonBounceableAddr4.workchainId)
        assertEquals(
            BitString("dd24c4a1f2b88f8b7053513b5cc6c5a31bc44b2a72dcb4d8c0338af0f0d37ec5"),
            nonBounceableAddr4.address
        )

        assertEquals(bounceableAddr4, nonBounceableAddr4)
    }

    @Test
    fun `address to raw string`() {
        val addr1 =
            AddrStd(null, -1, "3333333333333333333333333333333333333333333333333333333333333333".hexToByteArray())
        assertEquals(
            "-1:3333333333333333333333333333333333333333333333333333333333333333",
            addr1.toString(userFriendly = false)
        )
        assertEquals(
            "-1:3333333333333333333333333333333333333333333333333333333333333333",
            addr1.toString(userFriendly = false, testOnly = true)
        )
        assertEquals(
            "-1:3333333333333333333333333333333333333333333333333333333333333333",
            addr1.toString(userFriendly = false, testOnly = true, bounceable = true)
        )
        assertEquals(
            "-1:3333333333333333333333333333333333333333333333333333333333333333",
            addr1.toString(userFriendly = false, bounceable = true)
        )

        val addr2 =
            AddrStd(null, 0, "83dfd552e63729b472fcbcc8c45ebcc6691702558b68ec7527e1ba403a0f31a8".hexToByteArray())
        assertEquals(
            "0:83DFD552E63729B472FCBCC8C45EBCC6691702558B68EC7527E1BA403A0F31A8",
            addr2.toString(userFriendly = false)
        )

        val addr3 =
            AddrStd(null, -1, "dd24c4a1f2b88f8b7053513b5cc6c5a31bc44b2a72dcb4d8c0338af0f0d37ec5".hexToByteArray())
        assertEquals(
            "-1:DD24C4A1F2B88F8B7053513B5CC6C5A31BC44B2A72DCB4D8C0338AF0F0D37EC5",
            addr3.toString(userFriendly = false)
        )
    }

    @Test
    fun `address to user-friendly base64url string`() {
        val addr1 =
            AddrStd(null, -1, "3333333333333333333333333333333333333333333333333333333333333333".hexToByteArray())
        assertEquals(
            "Ef8zMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzM0vF",
            addr1.toString(userFriendly = true, urlSafe = true, testOnly = false, bounceable = true)
        )
        assertEquals(
            "Ef8zMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzM0vF",
            addr1.toString(userFriendly = true, urlSafe = false, testOnly = false, bounceable = true)
        )
        assertEquals(
            "Uf8zMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMxYA",
            addr1.toString(userFriendly = true, urlSafe = true, testOnly = false, bounceable = false)
        )
        assertEquals(
            "Uf8zMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMzMxYA",
            addr1.toString(userFriendly = true, urlSafe = false, testOnly = false, bounceable = false)
        )

        val addr2 =
            AddrStd(null, 0, "83dfd552e63729b472fcbcc8c45ebcc6691702558b68ec7527e1ba403a0f31a8".hexToByteArray())
        assertEquals(
            "EQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqB2N",
            addr2.toString(bounceable = true)
        )
        assertEquals(
            "UQCD39VS5jcptHL8vMjEXrzGaRcCVYto7HUn4bpAOg8xqEBI",
            addr2.toString(bounceable = false)
        )

        val addr3 =
            AddrStd(null, -1, "dd24c4a1f2b88f8b7053513b5cc6c5a31bc44b2a72dcb4d8c0338af0f0d37ec5".hexToByteArray())
        assertEquals(
            "Ef_dJMSh8riPi3BTUTtcxsWjG8RLKnLctNjAM4rw8NN-xWdr",
            addr3.toString(urlSafe = true, testOnly = false, bounceable = true)
        )
        assertEquals(
            "Ef/dJMSh8riPi3BTUTtcxsWjG8RLKnLctNjAM4rw8NN+xWdr",
            addr3.toString(urlSafe = false, testOnly = false, bounceable = true)
        )
        assertEquals(
            "Uf_dJMSh8riPi3BTUTtcxsWjG8RLKnLctNjAM4rw8NN-xTqu",
            addr3.toString(urlSafe = true, testOnly = false, bounceable = false)
        )
        assertEquals(
            "Uf/dJMSh8riPi3BTUTtcxsWjG8RLKnLctNjAM4rw8NN+xTqu",
            addr3.toString(urlSafe = false, testOnly = false, bounceable = false)
        )
        assertEquals(
            "kf_dJMSh8riPi3BTUTtcxsWjG8RLKnLctNjAM4rw8NN-xdzh",
            addr3.toString(urlSafe = true, testOnly = true, bounceable = true)
        )
    }

    @Test
    fun `user friendly address representation`() {
        val okAddr1 = AddrStd(0, "0F3DCC67E2C308314D56D3F0CA042A392CDD560F7B8514A8EA6348E9CADD1665".hexToByteArray())
        val okAddr2 = AddrStd(0, "AE94287A412BC6B3CC73FBB6C0D57EAEBF7C24E8D24AC092313A55007137A2F9".hexToByteArray())

        assertEquals("EQAPPcxn4sMIMU1W0_DKBCo5LN1WD3uFFKjqY0jpyt0WZf7D", okAddr1.toString(userFriendly = true))
        assertEquals("EQCulCh6QSvGs8xz-7bA1X6uv3wk6NJKwJIxOlUAcTei-cjj", okAddr2.toString(userFriendly = true))

        assertEquals(
            AddrStd("EQBLAcMnTcyx-_mWQtrVEC1eyDfK2nHI-A54P5eL7y-uE2Ht").toString(userFriendly = true),
            "EQBLAcMnTcyx-_mWQtrVEC1eyDfK2nHI-A54P5eL7y-uE2Ht"
        )

        val badAddr = AddrStd(0, "6C5FADFB25D8F6E55D26537BAC5B90E09ACEB0D447C6EE2DE2A94D93AB34B25D".hexToByteArray())

        assertEquals(AddrStd("EQBsX637Jdj25V0mU3usW5Dgms6w1EfG7i3iqU2TqzSyXf_s"), badAddr)
        // crc = 65516 , user friendly has wrong tail
        assertNotEquals("EQBsX637Jdj25V0mU3usW5Dgms6w1EfG7i3iqU2TqzSyXew=", badAddr.toString(userFriendly = true))
    }
}
