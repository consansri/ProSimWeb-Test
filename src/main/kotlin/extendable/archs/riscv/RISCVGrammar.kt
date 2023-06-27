package extendable.archs.riscv

import extendable.components.assembly.Assembly
import extendable.components.assembly.Grammar

class RISCVGrammar : Grammar() {

    /**
     * program: { [label] [directive | instruction] [comment] newline }
     * label: "name:"
     * instruction: "name reg,reg,imm" for example
     *
     */

    var labels = mutableListOf<Label>()
    var comments = mutableListOf<Comment>()
    override fun clear() {
        labels.clear()
    }

    override fun check(tokenLines: List<List<Assembly.Token>>): GrammarTree {

        var node = mutableListOf<TreeNode>()

        for (lineID in tokenLines.indices) {
            var remainingTokens = tokenLines[lineID].toMutableList()

            // search Label
            for (token in remainingTokens) {
                when (token) {
                    is Assembly.Token.Symbol -> {
                        if (token.content == ":") {
                            val tokenIndex = remainingTokens.indexOf(token)
                            val colon = token
                            var labelName = mutableListOf<Assembly.Token>()

                            if (tokenIndex + 1 < remainingTokens.size) {
                                if (remainingTokens[tokenIndex + 1] !is Assembly.Token.Space) {
                                    continue
                                }
                            }

                            var previous = tokenIndex - 1

                            while (previous >= 0) {
                                val prevToken = remainingTokens[previous]
                                when (prevToken) {
                                    is Assembly.Token.Space -> {
                                        break
                                    }

                                    else -> {
                                        labelName.add(0, prevToken)
                                    }
                                }

                                previous--
                            }

                            val label = Label(*labelName.toTypedArray(), colon = colon)
                            remainingTokens.removeAll(labelName)
                            remainingTokens.remove(colon)
                            node.add(label)
                            labels.add(label)
                            break
                        }
                    }

                    else -> {

                    }
                }
            }

            // search directive


            // search instruction


            // search comment
            for (token in remainingTokens) {
                when (token) {
                    is Assembly.Token.Symbol -> {
                        if (token.content == "#") {
                            val content = remainingTokens.dropWhile { it != token }.drop(1).toTypedArray()
                            val comment = Comment(token, *content)
                            node.add(comment)
                            comments.add(comment)
                            break
                        }
                    }

                    else -> {

                    }
                }
            }

        }

        return GrammarTree(node)
    }


    class Label(vararg val name: Assembly.Token, colon: Assembly.Token.Symbol) : TreeNode.TokenNode(RISCVFlags.label, *name, colon) {

        val wholeName: String

        init {
            wholeName = name.joinToString("") { it.content }
            console.log("Grammar: RiscV Label <$wholeName>")
        }
    }

    class Directive(vararg tokens: Assembly.Token) : TreeNode.TokenNode(RISCVFlags.directive, *tokens)

    class Instruction(val insToken: Assembly.Token.Instruction, vararg val params: Assembly.Token) : TreeNode.CollectionNode(TokenNode("", insToken), TokenNode("", *params))

    class Comment(val prefix: Assembly.Token.Symbol, vararg val content: Assembly.Token) : TreeNode.TokenNode(RISCVFlags.comment, prefix, *content) {

        val wholeContent: String

        init {
            wholeContent = content.joinToString("") { it.content }
            console.log("Grammar: RiscV Comment <$wholeContent>")
        }
    }

}