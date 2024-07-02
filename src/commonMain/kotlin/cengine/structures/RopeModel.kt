package cengine.structures


/**
 * Rope is the data structure for holding the text editor state.
 *
 */
class RopeModel(text: String = "") : CodeModel {
    private var root: Node = buildTree(text)
    override val length: Int get() = root.weight
    override val lines: Int get() = root.lineBreaks + 1


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
        return root.substring(start, end).toString()
    }

    override fun charAt(index: Int): Char {
        require(index in 0 until length) { "Index out of bounds" }
        return root.charAt(index)
    }

    override fun getLineAndColumn(index: Int): Pair<Int, Int> {
        require(index in 0..length) { "Index out of bounds" }
        val result = root.getLineAndColumn(index)
        return result.line to result.col + 1
    }

    override fun getIndexFromLineAndColumn(line: Int, column: Int): Int {
        require(line > 0 && column > 0) { "Line and column must be positive" }
        return root.getIndexFromLineAndColumn(line, column).index - 1
    }

    override fun toString(): String = root.substring(0, length).toString()

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
        abstract var weight: Int
        abstract var depth: Int
        abstract var lineBreaks: Int

        abstract fun getLineAndColumn(countUntil: Int): LC
        abstract fun getIndexFromLineAndColumn(line: Int, column: Int): AbsIndex
        abstract fun insert(index: Int, newText: String): Node
        abstract fun delete(start: Int, end: Int): Node
        abstract fun substring(start: Int, end: Int): StringBuilder
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

    private class Leaf(text: String) : Node() {
        var text: String = text
            set(value) {
                field = value
                updateMetrics()
            }

        override var weight: Int = text.length
        override var depth: Int = 0
        override var lineBreaks: Int = text.count { it == '\n' }

        private fun updateMetrics() {
            weight = text.length
            lineBreaks = text.count { it == '\n' }
        }

        override fun getLineAndColumn(countUntil: Int): LC {
            val lc = LC()
            for (i in 0..<countUntil) {
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
                    if (currLine > line) {
                        return AbsIndex(currColumn, i)
                    }
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
                text = updatedText
                this
            } else {
                buildTree(updatedText)
            }
        }

        override fun delete(start: Int, end: Int): Node {
            text = text.removeRange(start, end)
            return this
        }

        override fun substring(start: Int, end: Int): StringBuilder = StringBuilder(text.substring(start, end))

        override fun charAt(index: Int): Char = text[index]
    }

    private class Branch(left: Node, right: Node) : Node() {
        var left: Node = left
            set(value) {
                field = value
                updateMetrics()
            }

        var right: Node = right
            set(value) {
                field = value
                updateMetrics()
            }

        override var weight: Int = left.weight + right.weight
        override var depth: Int = maxOf(left.depth, right.depth) + 1
        override var lineBreaks: Int = left.lineBreaks + right.lineBreaks

        fun updateMetrics() {
            weight = left.weight + right.weight
            depth = maxOf(left.depth, right.depth) + 1
            lineBreaks = left.lineBreaks + right.lineBreaks
        }

        override fun getLineAndColumn(countUntil: Int): LC {
            return if (countUntil < left.weight) {
                left.getLineAndColumn(countUntil)
            } else {
                val leftLC = left.getLineAndColumn(left.weight)
                val rightLC = right.getLineAndColumn(countUntil - left.weight)
                leftLC + rightLC
            }
        }

        override fun getIndexFromLineAndColumn(line: Int, column: Int): AbsIndex {
            val leftLastLine = left.lineBreaks
            return if (line < leftLastLine) {
                left.getIndexFromLineAndColumn(line, column)
            } else if(line == leftLastLine) {
                val leftIndex = left.getIndexFromLineAndColumn(line, column)
                val rightIndex = right.getIndexFromLineAndColumn(0, column - leftIndex.afterLastLineBreak)
                val index = left.weight + rightIndex.index
                AbsIndex(index, index)
            } else {
                val rightIndex = right.getIndexFromLineAndColumn(line - leftLastLine, column)
                val index = left.weight + rightIndex.index
                AbsIndex(rightIndex.afterLastLineBreak, index)
            }
        }

        override fun insert(index: Int, newText: String): Node {
            return if (index < left.weight) {
                left = left.insert(index, newText)
                this
            } else {
                right = right.insert(index - left.weight, newText)
                this
            }
        }

        override fun delete(start: Int, end: Int): Node {
            val leftWeight = left.weight
            return when {
                end <= leftWeight -> {
                    left = left.delete(start, end)
                    this
                }

                start >= leftWeight -> {
                    right = right.delete(start - leftWeight, end - leftWeight)
                    this
                }

                else -> {
                    val newLeft = left.delete(start, leftWeight)
                    val newRight = right.delete(0, end - leftWeight)
                    if (newLeft is Leaf && newRight is Leaf && newLeft.weight + newRight.weight <= LEAF_MAX_LENGTH) {
                        Leaf(newLeft.text + newRight.text)
                    } else {
                        left = newLeft
                        right = newRight
                        this
                    }
                }
            }
        }

        override fun substring(start: Int, end: Int): StringBuilder {
            val leftWeight = left.weight
            return when {
                end <= leftWeight -> left.substring(start, end)
                start >= leftWeight -> right.substring(start - leftWeight, end - leftWeight)
                else -> left.substring(start, leftWeight).append(right.substring(0, end - leftWeight).toString())
            }
        }

        override fun charAt(index: Int): Char {
            return if (index < left.weight) {
                left.charAt(index)
            } else {
                right.charAt(index - left.weight)
            }
        }
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