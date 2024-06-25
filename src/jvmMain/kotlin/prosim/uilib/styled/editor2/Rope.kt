package prosim.uilib.styled.editor2

class Rope(var root: Node) {
    sealed class Node {
        abstract val weight: Int
        abstract fun sumOfLeafWeights(): Int
        abstract fun concatenate(): String
    }

    class Leaf(val string: String) : Node() {
        override val weight: Int = string.length
        override fun concatenate(): String = string
        override fun sumOfLeafWeights(): Int = weight
    }

    class Intermediate(val left: Node, val right: Node) : Node() {
        override val weight: Int = left.sumOfLeafWeights()
        override fun concatenate(): String = left.concatenate() + right.concatenate()
        override fun sumOfLeafWeights(): Int = left.sumOfLeafWeights() + right.sumOfLeafWeights()
    }

    data class Metrics(val weight: Int, val charCount: Int, val lineCount: Int)

}
