package cengine.structures


/**
 * Rope is the data structure for holding the text editor state.
 *
 */
class RopeModel(text: String = ""): Code {
    private var root: Node = buildTree(text)
    override val length: Int get() = root.weight

    override fun insert(index: Int, new: String) {
        require(index in 0..length) { "Index out of bounds" }
        root = root.insert(index, new)
        rebalance()
    }

    override fun delete(start: Int, end: Int) {
        require(start in 0..length && end in start..length) { "Invalid range" }
        root = root.delete(start, end)
        rebalance()
    }

    override fun substring(start: Int, end: Int): String {
        require(start in 0..length && end in start..length) { "Invalid range" }
        return root.substring(start, end)
    }

    override fun charAt(index: Int): Char {
        require(index in 0 until length) { "Index out of bounds" }
        return root.charAt(index)
    }

    fun getLineAndColumn(index: Int): Pair<Int, Int> {
        require(index in 0..length) { "Index out of bounds" }
        val result = root.getLineAndColumn(index)
        return result.line to result.col
    }

    fun getIndexFromLineAndColumn(line: Int, column: Int): Int {
        require(line > 0 && column > 0) { "Line and column must be positive" }
        return root.getIndexFromLineAndColumn(line, column).index
    }

    override fun toString(): String = root.toString()

    private fun rebalance() {
        if (root.depth > MAX_DEPTH) {
            val newRoot = Node.rebalance(root)
            if (newRoot != root) {
                root = newRoot
            }
        }
    }

    companion object {
        const val LEAF_MAX_LENGTH = 64
        const val MAX_DEPTH = 100

        private fun buildTree(text: String): Node {
            return if (text.length <= LEAF_MAX_LENGTH) {
                Leaf(text)
            } else {
                val mid = text.length / 2
                val left = buildTree(text.substring(0, mid))
                val right = buildTree(text.substring(mid))
                Branch(left, right)
            }
        }
    }

    /**
     *
     */
    private sealed class Node {
        abstract val weight: Int
        abstract val depth: Int
        abstract val lineBreaks: Int

        abstract fun getLineAndColumn(index: Int): LC
        abstract fun getIndexFromLineAndColumn(line: Int, column: Int): AbsIndex

        abstract fun insert(index: Int, newText: String): Node
        abstract fun delete(start: Int, end: Int): Node
        abstract fun substring(start: Int, end: Int): String
        abstract fun charAt(index: Int): Char

        companion object {
            fun rebalance(node: Node): Node {
                val nodes = mutableListOf<Leaf>()
                flattenToLeaves(node, nodes)
                return buildBalancedTree(nodes, 0, nodes.size)
            }

            private fun flattenToLeaves(node: Node, leaves: MutableList<Leaf>) {
                when (node) {
                    is Leaf -> leaves.add(node)
                    is Branch -> {
                        flattenToLeaves(node.left, leaves)
                        flattenToLeaves(node.right, leaves)
                    }
                }
            }

            private fun buildBalancedTree(leaves: List<Leaf>, start: Int, end: Int): Node {
                return when (val count = end - start) {
                    0 -> Leaf("")
                    1 -> leaves[start]
                    else -> {
                        val mid = start + count / 2
                        val left = buildBalancedTree(leaves, start, mid)
                        val right = buildBalancedTree(leaves, mid, end)
                        Branch(left, right)
                    }
                }
            }
        }
    }

    private class Leaf(var text: String) : Node() {
        override val weight: Int get() = text.length
        override val depth: Int get() = 0
        override val lineBreaks: Int get() = text.count { it == '\n' }

        override fun getLineAndColumn(index: Int): LC {
            val lc = LC()
            for (i in 0 until index) {
                if (text[i] == '\n') {
                    lc.line++
                    lc.col = 0
                } else {
                    lc.col++
                }
            }
            return lc
        }

        override fun getIndexFromLineAndColumn(line: Int, column: Int): AbsIndex {
            var currLine = 0
            var currColumn = 0
            for (i in text.indices) {
                if (currLine == line && currColumn == column) {
                    return AbsIndex(currColumn, i)
                }
                if (text[i] == '\n') {
                    currLine++
                    currColumn = 0
                } else {
                    currColumn++
                }
            }
            return AbsIndex(currColumn, text.lastIndex)
        }

        override fun insert(index: Int, newText: String): Node {
            val updatedText = text.substring(0, index) + newText + text.substring(index)
            return if (updatedText.length <= LEAF_MAX_LENGTH) {
                Leaf(updatedText)
            } else {
                buildTree(updatedText)
            }
        }

        override fun delete(start: Int, end: Int): Node {
            text = text.removeRange(start, end)
            return this
        }

        override fun substring(start: Int, end: Int): String = text.substring(start, end)

        override fun charAt(index: Int): Char = text[index]

        override fun toString(): String = text
    }

    private class Branch(var left: Node, var right: Node) : Node() {
        override val weight: Int get() = left.weight + right.weight
        override val depth: Int get() = maxOf(left.depth, right.depth) + 1
        override val lineBreaks: Int get() = left.lineBreaks + right.lineBreaks

        override fun getLineAndColumn(index: Int): LC {
            return if (index < left.weight) {
                left.getLineAndColumn(index)
            } else {
                val leftLC = left.getLineAndColumn(left.weight)
                val rightLC = right.getLineAndColumn(index - left.weight)
                leftLC + rightLC
            }
        }

        override fun getIndexFromLineAndColumn(line: Int, column: Int): AbsIndex {
            val leftLastLine = left.lineBreaks
            return if (line < leftLastLine) {
                left.getIndexFromLineAndColumn(line, column)
            } else if (line == leftLastLine) {
                val leftIndex = left.getIndexFromLineAndColumn(line, column)
                val rightIndex = right.getIndexFromLineAndColumn(0, column)
                AbsIndex(leftIndex.afterLastLineBreak + rightIndex.afterLastLineBreak, leftIndex.index + rightIndex.index)

            } else {
                val rightIndex = right.getIndexFromLineAndColumn(line - leftLastLine, column)
                AbsIndex(rightIndex.afterLastLineBreak, left.weight + rightIndex.index)
            }
        }

        override fun insert(index: Int, newText: String): Node {
            return if (index < left.weight) {
                Branch(left.insert(index, newText), right)
            } else {
                Branch(left, right.insert(index - left.weight, newText))
            }
        }

        override fun delete(start: Int, end: Int): Node {
            val leftWeight = left.weight
            return when {
                end <= leftWeight -> Branch(left.delete(start, end), right)
                start >= leftWeight -> Branch(left, right.delete(start - leftWeight, end - leftWeight))
                else -> {
                    val newLeft = left.delete(start, leftWeight)
                    val newRight = right.delete(0, end - leftWeight)
                    if (newLeft is Leaf && newRight is Leaf && newLeft.weight + newRight.weight <= LEAF_MAX_LENGTH) {
                        Leaf(newLeft.text + newRight.text)
                    } else {
                        Branch(newLeft, newRight)
                    }
                }
            }
        }

        override fun substring(start: Int, end: Int): String {
            val leftWeight = left.weight
            return when {
                end <= leftWeight -> left.substring(start, end)
                start >= leftWeight -> right.substring(start - leftWeight, end - leftWeight)
                else -> left.substring(start, leftWeight) + right.substring(0, end - leftWeight)
            }
        }

        override fun charAt(index: Int): Char {
            return if (index < left.weight) {
                left.charAt(index)
            } else {
                right.charAt(index - left.weight)
            }
        }

        override fun toString(): String = left.toString() + right.toString()
    }

    /**
     * For Summing
     */
    private data class AbsIndex(val afterLastLineBreak: Int, val index: Int)

    /**
     * For Summing
     */
    private data class LC(var line: Int = 0, var col: Int = 0) {

        operator fun plus(other: LC): LC = LC(line + other.line, if (other.line != 0) other.col else col + other.col)
    }


}