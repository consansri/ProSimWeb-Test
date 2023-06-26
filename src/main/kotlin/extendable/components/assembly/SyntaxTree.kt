package extendable.components.assembly

class SyntaxTree(val root: Node) {

    sealed class Node() {

        var nodes: MutableList<Node> = mutableListOf()

        constructor(vararg node: Node) : this() {
            nodes += node
        }

        class LabelDef(vararg val dot: Assembly.Token.Symbol, val token: Assembly.Token.AlphaNum, val colon: Assembly.Token.Symbol) : Node()

        class TokenNode(val token: Assembly.Token) : Node()

        class InstructionNode(val instrToken: Assembly.Token.Instruction, vararg val paramToken: Assembly.Token) : Node()

    }

}