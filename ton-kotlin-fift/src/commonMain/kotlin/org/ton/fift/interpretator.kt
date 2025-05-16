package org.ton.fift

import org.ton.kotlin.bigint.BigInt
import org.ton.logger.Logger

class FiftInterpretator(
    val dictionary: Dictionary = Dictionary().apply { defineBasicWords() },
    var output: (String) -> Unit = { print(it) },
    var logger: Logger = Logger.println("Fift")
) {
    // Uses only for debug logging indent
    internal var debugExecutionDepth: Int = 0
    internal fun debugExecutionDepthIndent(): String = "  ".repeat(debugExecutionDepth)

    val stack: Stack = Stack(this)
    var state: Int = 0
    var charPos = 0
    var currentLine: String = ""

    fun interpret(line: String) = try {
        currentLine = line
        charPos = 0

        while (canRead()) {
            skipSpace()
            val pos = charPos
            var word = scanInput(' ')
            var wordDef = dictionary[word]

            if (wordDef == null) {
                wordDef = dictionary["$word "]
                if (wordDef != null) {
                    charPos++
                }
            }

            if (wordDef == null) {
                charPos = pos
                val wordBuilder = StringBuilder()
                while (canRead()) {
                    val char = currentLine[charPos++]
                    if (char != ' ' && char != '\t') {
                        wordBuilder.append(char)

                        val currentWord = wordBuilder.toString()
                        wordDef = dictionary[currentWord]
                        if (wordDef != null) {
                            word = currentWord
                            break
                        }
                    }
                }
                if (wordDef == null) {
                    charPos = pos
                }
            }

            if (wordDef != null) {
                interpret(wordDef)
            } else {
                charPos += word.length
                val int257s = word.parseInt256()
                logger.debug { "Interpret: ${int257s.joinToString(" ")} state=$state" }
                when (int257s.size) {
                    1 -> {
                        stack.push(int257s.first())
                        stack.pushArgCount(1)
                    }

                    2 -> {
                        stack.push(int257s[0])
                        stack.push(int257s[1])
                        stack.pushArgCount(2)
                    }

                    else -> throw FiftException(-1, "Unknown word: $word")
                }
                compileExecute()
            }
        }
        output(" ok\n")
    } catch (e: FiftException) {
        stack.clear()
        state = 0
        throw e
    }

    fun interpret(wordDef: WordDef) {
        logger.debug { "Interpret: '$wordDef' state=$state isActive=${wordDef.isActive}" }
        if (wordDef.isActive) {
            wordDef.execute(this)
            compileExecute()
        } else {
            stack.push(0)
            stack.push(wordDef)
            compileExecute()
        }
    }

    fun compileExecute() {
        if (state > 0) {
            dictionary["(compile) "]?.execute(this)
        } else {
            dictionary["(execute) "]?.execute(this)
        }
    }

    fun canRead() = charPos in 0..currentLine.lastIndex

    fun scanInput(separator: Char = ' ', readSeparator: Boolean = false): String {
        val wordBuilder = StringBuilder()
        while (canRead()) {
            val char = currentLine[charPos]
            if (char != separator) {
                charPos++
                wordBuilder.append(char)
            } else {
                if (readSeparator) {
                    charPos++
                }
                break
            }
        }
        val result = wordBuilder.toString()
        logger.debug { "Scanned input: \"$result\"" }
        return result
    }

    fun skipSpace() {
        while (canRead()) {
            val char = currentLine[charPos]
            if (char == ' ' || char == '\t' || char == '\n') {
                charPos++
            } else {
                break
            }
        }
    }

    fun String.parseInt256(): Array<BigInt> = try {
        val fracStrings = split('/')
        when {
            fracStrings.size == 1 -> arrayOf(BigInt(fracStrings.first()))
            fracStrings.size > 1 -> arrayOf(BigInt(fracStrings.first()), BigInt(fracStrings[1]))
            else -> emptyArray()
        }
    } catch (e: NumberFormatException) {
        emptyArray()
    }

    operator fun invoke(block: FiftInterpretator.() -> Unit) = apply(block)

    val String.utf8Code: Int
        get() = when {
            isNotEmpty() && get(0).code < 0x80 -> get(0).code
            length >= 2
                    && (get(0).code and 0xe0) == 0xc0
                    && (get(1).code and 0xc0) == 0x80 ->
                ((get(0).code and 0x1f) shl 6) or (get(1).code and 0x3f)

            length >= 3
                    && (get(0).code and 0xf0) == 0xe0
                    && (get(1).code and 0xc0) == 0x80
                    && (get(2).code and 0xc0) == 0x80 ->
                ((get(0).code and 0x0f) shl 12) or ((get(1).code and 0x3f) shl 6) or (get(2).code and 0x3f)

            length >= 4
                    && (get(0).code and 0xf8) == 0xf0
                    && (get(1).code and 0xc0) == 0x80
                    && (get(2).code and 0xc0) == 0x80
                    && (get(3).code and 0xc0) == 0x80 ->
                ((get(0).code and 7) shl 18) or ((get(1).code and 0x3f) shl 12) or ((get(2).code and 0x3f) shl 6) or (get(
                    3
                ).code and 0x3f)

            else -> -1
        }

    val Int.utf8Char: String
        get() = when {
            this < 0x80 -> toChar().toString()
            this < 0x800 -> charArrayOf(
                (0xc0 + (this shr 6)).toChar(),
                (0x80 + (this and 0x3f)).toChar(),
            ).concatToString()

            this < 0x10000 -> charArrayOf(
                (0xe0 + (this shr 12)).toChar(),
                (0x80 + ((this shr 6) and 0x3f)).toChar(),
                (0x80 + (this and 0x3d)).toChar()
            ).concatToString()

            this < 0x200000 -> charArrayOf(
                (0xf0 + (this shr 18)).toChar(),
                (0x80 + ((this shr 12) and 0x3f)).toChar(),
                (0x80 + ((this shr 6) and 0x3f)).toChar(),
                (0x80 + (this and 0x3f)).toChar()
            ).concatToString()

            else -> ""
        }
}

fun FiftInterpretator.checkCompile() = check(state > 0) { "Compilation mode only" }
fun FiftInterpretator.checkExecute() = check(state > 0) { "Interpretation mode only" }
fun FiftInterpretator.checkNotIntExec() = check(state >= 0) { "not allowed in internal interpret mode" }
fun FiftInterpretator.checkIntExec() = check(state < 0) { "internal interpret mode only" }
