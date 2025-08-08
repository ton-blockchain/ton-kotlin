import kotlinx.io.bytestring.ByteString
import kotlin.random.Random
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTime

class AdnlShortIdMapTest {
    @Test

    fun foo() {
        val random = Array(1000) {
            Random.nextBytes(32)
        }.let {
            it + it + it
        }.also {
            it.shuffle()
        }

        var check1global = 0.seconds
        var check2global = 0.seconds
        var check3global = 0.seconds

        repeat(10_000) {
            println("Iteration: $it")
            val a1 = random.random()
            val a2 = random.random()

            val check1 = measureTime {
                repeat(1_000_000) {
                    check(a1, a2)
                }
            }
            val check2 = measureTime {
                repeat(1_000_000) {
                    check2(a1, a2)
                }
            }
            val check3 = measureTime {
                repeat(1_000_000) {
                    check3(a1, a2)
                }
            }

            check1global += check1
            check2global += check2
            check3global += check3
        }

        println("check1: ${check1global / 10_000}")
        println("check2: ${check2global / 10_000}")
        println("check3: ${check3global / 10_000}")
    }

    private fun check(a1: ByteArray, a2: ByteArray): Boolean {
        return a1.contentEquals(a2)
    }

    private fun check2(a1: ByteArray, a2: ByteArray): Boolean {
        for (i in 1 until 32) {
            if (a1[i] != a2[i]) {
                return false
            }
        }
        return true
    }

    private fun check3(a1: ByteArray, a2: ByteArray): Boolean {
        for (i in 0 until 32) {
            if (a1[i] != a2[i]) {
                return false
            }
        }
        return true
    }


    private fun check(a1: ByteString, a2: ByteString): Boolean {
        return a1 == a2
    }

    private fun check2(a1: ByteString, a2: ByteString): Boolean {
        for (i in 1 until 32) {
            if (a1[i] != a2[i]) {
                return false
            }
        }
        return true
    }

    private fun check3(a1: ByteString, a2: ByteString): Boolean {
        for (i in 0 until 32) {
            if (a1[i] != a2[i]) {
                return false
            }
        }
        return true
    }

}
