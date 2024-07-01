import cengine.structures.Code
import cengine.structures.RopeModel
import cengine.structures.StringModel
import emulator.kit.nativeLog
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.measureTime


class TestCode() {

    val RANDOM_STRING_RANGE = 1..128

    @Test
    fun testRope() {
        val inital = "Hello World\nI like you!\n".repeat(10000)
        val models = StringModel(inital) to RopeModel(inital)

        repeat(20){
            models.compareInsert()
            models.compareSubstring()
        }
        repeat(20){
            models.compareDeletes()
            models.compareCharAt()
        }
    }

    private fun Pair<Code, Code>.compareInsert() {
        val range = 0..<this.first.length
        val index = Random.nextInt(range)

        nativeLog("DELETE: $index in $range")

        val string = getRandomString()
        val fTime = measureTime {
            first.insert(index, string)
        }
        val sTime = measureTime {
            second.insert(index, string)
        }
        assertEquals(first.toString(), second.toString())
        nativeLog("CompareInserts: " +
                "\n\t${first::class.simpleName}\t\t: ${fTime.inWholeNanoseconds}ns" +
                "\n\t${second::class.simpleName}\t\t: ${sTime.inWholeNanoseconds}ns")
    }

    private fun Pair<Code, Code>.compareDeletes() {
        val maxlength = this.first.length
        val start = Random.nextInt(0, maxlength - 1)
        val end = Random.nextInt(start, maxlength)

        nativeLog("DELETE: $end in 0..$maxlength")

        val fTime = measureTime {
            first.delete(start, end)
        }
        val sTime = measureTime {
            second.delete(start, end)
        }
        assertEquals(first.toString(), second.toString())
        nativeLog("CompareDeletes: $end in 0..$maxlength" +
                "\n\t${first::class.simpleName}\t\ttook ${fTime.inWholeNanoseconds}ns" +
                "\n\t${second::class.simpleName}\t\ttook ${sTime.inWholeNanoseconds}ns")
    }

    private fun Pair<Code, Code>.compareSubstring(){
        val maxlength = this.first.length
        val start = Random.nextInt(0, maxlength - 1)
        val end = Random.nextInt(start, maxlength)

        nativeLog("SUBSTRING: $end in 0..<$maxlength")

        val fSub: String
        val fTime = measureTime {
            fSub = first.substring(start, end)
        }
        val sSub: String
        val sTime = measureTime {
            sSub = second.substring(start, end)
        }
        assertEquals(fSub, sSub)
        nativeLog("CompareSubstring: $end in 0..$maxlength \n\t${first::class.simpleName} took ${fTime.inWholeNanoseconds}ns\n\t${second::class.simpleName} took ${sTime.inWholeNanoseconds}ns")
    }

    private fun Pair<Code,Code>.compareCharAt(){
        val maxlength = this.first.length
        val start = Random.nextInt(0, maxlength - 1)

        nativeLog("CHARAT: $start in 0..<$maxlength")

        val fSub: Char
        val fTime = measureTime {
            fSub = first.charAt(start)
        }
        val sSub: Char
        val sTime = measureTime {
            sSub = second.charAt(start)
        }
        assertEquals(fSub, sSub)
        nativeLog("CompareCharAt: $start in 0..$maxlength \n\t${first::class.simpleName} took ${fTime.inWholeNanoseconds}ns\n\t${second::class.simpleName} took ${sTime.inWholeNanoseconds}ns")
    }

    fun testLineColumnIds() {
        /*val rope = Rope("Hello,\nworld!\nHow are you?\nThis is a test.")
        println(rope.toString())
        println("Total lines: ${rope.getLineCount()}")

        // Test getIndexFromLineAndColumn with various scenarios
        println("Index for line 2, column 3: ${rope.getIndexFromLineAndColumn(2, 3)}")
        println("Index for line 2, column 10 (beyond end of line): ${rope.getIndexFromLineAndColumn(2, 10)}")
        println("Index for line 3, column 1: ${rope.getIndexFromLineAndColumn(3, 1)}")
        println("Index for line 5, column 1 (beyond last line): ${rope.getIndexFromLineAndColumn(5, 1)}")

        // Test getLineLength
        println("Length of line 1: ${rope.getLineLength(1)}")
        println("Length of line 2: ${rope.getLineLength(2)}")
        println("Length of line 3: ${rope.getLineLength(3)}")
        println("Length of line 4: ${rope.getLineLength(4)}")*/
    }

    private fun getRandomString(): String {
        val size = Random.nextInt(RANDOM_STRING_RANGE)
        return (1..size).map { getRandomChar() }.joinToString("")
    }

    private fun getRandomChar(): Char {
        val minCodePoint = 0
        val maxCodePoint = 0xFFFF

        val randomCodePoint = Random.nextInt(minCodePoint, maxCodePoint + 1)
        return randomCodePoint.toChar()
    }

}


