package org.ton.kotlin.crypto.curve25519.constants

import org.ton.kotlin.crypto.curve25519.edwards.CompressedEdwardsY
import org.ton.kotlin.crypto.curve25519.field.FieldElement
import org.ton.kotlin.crypto.curve25519.scalar.UnpackedScalar

internal val LOW_51_BIT_NASK = (1uL shl 51) - 1uL
internal val LOW_52_BIT_NASK = (1uL shl 52) - 1uL

// Edwards `d` value, equal to `-121665/121666 mod p`.
internal val EDWARDS_D = FieldElement(
    929955233495203u,
    466365720129213u,
    1662059464998953u,
    2033849074728123u,
    1442794654840575u,
)

// Edwards `2*d` value, equal to `2*(-121665/121666) mod p`.
internal val EDWARDS_D2 = FieldElement(
    1859910466990425u,
    932731440258426u,
    1072319116312658u,
    1815898335770999u,
    633789495995903u,
)

// Precomputed value of one of the square roots of -1 (mod p).
internal val SQRT_M1 = FieldElement(
    1718705420411056u,
    234908883556509u,
    2233514472574048u,
    2117202627021982u,
    765476049583133u,
)

// `L` is the order of base point, i.e. 2^252 + 27742317777372353535851937790883648493.
internal val L = UnpackedScalar(
    0x0002631a5cf5d3edu,
    0x000dea2f79cd6581u,
    0x000000000014def9u,
    0x0000000000000000u,
    0x0000100000000000u,
)

// `R` = R % L where R = 2^260.
internal val R = UnpackedScalar(
    0x000f48bd6721e6edu,
    0x0003bab5ac67e45au,
    0x000fffffeb35e51bu,
    0x000fffffffffffffu,
    0x00000fffffffffffu,
)

// `RR` = (R^2) % L where R = 2^260.
internal val RR = UnpackedScalar(
    0x0009d265e952d13bu,
    0x000d63c715bea69fu,
    0x0005be65cb687604u,
    0x0003dceec73d217fu,
    0x000009411b7c309au,
)

@Suppress("SpellCheckingInspection")
internal val NON_CANONICAL_SIGN_BITS =
    arrayOf(
        CompressedEdwardsY("0100000000000000000000000000000000000000000000000000000000000080".hexToByteArray()),
        CompressedEdwardsY("ecffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff".hexToByteArray())
    )

internal const val LFACTOR = 0x51da312547e1bu
