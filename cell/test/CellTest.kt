import org.ton.sdk.cell.CellDescriptor
import org.ton.sdk.cell.LevelMask
import kotlin.test.Test
import kotlin.test.assertEquals

class CellTest {
//    @Test
//    fun correctLevelTest() {
//        val levels = intArrayOf(0, 1, 1, 2, 1, 2, 2, 3)
//        for (mask in 0b000..0b111) {
//            println("---")
//            println("test mask $mask - ${mask.toString(2)}")
//            println(LevelMask(mask))
//            println(levels[mask])
//            assertEquals(levels[mask], LevelMask(mask).level)
//        }
//    }

    @Test
    fun virtualizeDescriptor() {
        val levelMask = LevelMask(0b111)
        val desc = CellDescriptor(
            levelMask, false, 3, 123
        )
        assertEquals(levelMask, desc.levelMask)

        for (i in 0..3) {
            val vDesc = desc.virtualize(i)

            assertEquals(desc.cellType, vDesc.cellType)
            assertEquals(desc.referenceCount, vDesc.referenceCount)
            assertEquals(desc.isExotic, vDesc.isExotic)
            assertEquals(desc.hasHashes, vDesc.hasHashes)
            assertEquals(desc.isAligned, vDesc.isAligned)
            assertEquals(desc.byteLength, vDesc.byteLength)

            assertEquals(levelMask.virtualize(i), vDesc.levelMask)
        }
    }

    @Test
    fun correctHashIndex() {
        val table = arrayOf(
            // index                // mask
            intArrayOf(0, 0, 0, 0), // 000
            intArrayOf(0, 1, 1, 1), // 001
            intArrayOf(0, 0, 1, 1), // 010
            intArrayOf(0, 1, 2, 2), // 011
            intArrayOf(0, 0, 0, 1), // 100
            intArrayOf(0, 1, 1, 2), // 101
            intArrayOf(0, 0, 1, 2), // 110
            intArrayOf(0, 1, 2, 3), // 111
        )
        for (mask in 0b000..0b111) {
            val levelMask = LevelMask(mask)
            for (level in 0..3) {
                assertEquals(
                    table[mask][level],
                    levelMask.apply(level).hashIndex,
                    "mask=${mask.toString(2).padStart(3, '0')} level=$level"
                )
            }
        }
    }
}
