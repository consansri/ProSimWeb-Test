
import cengine.structures.Rope
import emulator.kit.nativeLog
import kotlin.test.Test
import kotlin.test.assertEquals


class TestRope(){

    @Test
    fun testRope() {
        val rope = Rope("Hello, world!")
        nativeLog(rope.toString()) // Output: Hello, world!

        rope.insert(7, "beautiful ")
        nativeLog(rope.toString()) // Output: Hello, beautiful world!

        rope.delete(0, 7)
        nativeLog(rope.toString()) // Output: beautiful world!

        nativeLog(rope.substring(0, 9)) // Output: beautiful
        nativeLog(rope.charAt(10).toString()) // Output: o

        val expected = "beautiful world!"
        assertEquals(expected, rope.toString(), "String isn't matching! (${expected}) ")
    }

    @Test
    fun testLineColumnIds(){
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

}


