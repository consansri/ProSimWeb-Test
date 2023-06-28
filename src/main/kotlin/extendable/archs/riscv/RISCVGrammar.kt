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
    var directives = mutableListOf<Directive>()
    var instructions = mutableListOf<Instruction>()
    var pseudoInstructions = mutableListOf<PseudoInstruction>()
    var params = mutableListOf<ParamCollection>()
    var comments = mutableListOf<Comment>()


    override fun clear() {
        labels.clear()
        directives.clear()
        instructions.clear()
        pseudoInstructions.clear()
        params.clear()
        comments.clear()
    }

    override fun check(tokenLines: List<List<Assembly.Token>>): GrammarTree {

        var node = mutableListOf<TreeNode>()

        for (lineID in tokenLines.indices) {
            var remainingTokens = tokenLines[lineID].toMutableList()

            console.log("line ${lineID + 1} ---------")

            // search Label
            for (token in remainingTokens) {
                when (token) {
                    is Assembly.Token.Symbol -> {
                        if (token.content == ":") {
                            val tokenIndex = remainingTokens.indexOf(token)
                            val colon = token
                            val labelName = mutableListOf<Assembly.Token>()

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
            var directiveName = mutableListOf<Assembly.Token.Word>()
            var dot: Assembly.Token.Symbol? = null
            for (token in remainingTokens) {
                when (token) {
                    is Assembly.Token.Symbol -> {
                        if (token.content == ".") {
                            dot = token
                            directiveName.clear()
                            continue
                        } else {
                            break
                        }
                    }

                    is Assembly.Token.Space -> {
                        if (dot != null && directiveName.isNotEmpty()) {
                            break
                        } else {
                            continue
                        }
                    }

                    is Assembly.Token.Word -> {
                        if (dot != null) {
                            directiveName.add(token)
                        } else {
                            break
                        }
                    }

                    else -> {
                        break
                    }
                }
            }


            if (dot != null && directiveName.isNotEmpty()) {
                val name = directiveName.joinToString { it.content }
                when (name) {
                    Directive.Type.data.name -> {
                        val directive = Directive(dot, Directive.Type.data, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        node.add(directive)
                        directives.add(directive)
                    }

                    Directive.Type.text.name -> {
                        val directive = Directive(dot, Directive.Type.text, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        node.add(directive)
                        directives.add(directive)
                    }

                    else -> {}
                }
            }


            // search instruction
            for (token in remainingTokens) {
                when (token) {
                    is Assembly.Token.Space -> {
                        continue
                    }

                    is Assembly.Token.Word -> {
                        for (type in PseudoInstruction.Types.list) {
                            if (type.name.uppercase() == token.content.uppercase()) {
                                val pseudoInstr = PseudoInstruction(token, type)
                                remainingTokens.remove(token)
                                node.add(pseudoInstr)
                                pseudoInstructions.add(pseudoInstr)
                            }
                        }
                    }

                    is Assembly.Token.Instruction -> {
                        val instruction = Instruction(token)
                        remainingTokens.remove(token)
                        node.add(instruction)
                        instructions.add(instruction)
                    }

                    else -> {
                        break
                    }
                }
            }

            // search Parameters
            val parameterList = mutableListOf<Param>()
            var validParams = true
            var labelBuilder = mutableListOf<Assembly.Token>()

            while (remainingTokens.isNotEmpty()) {
                var firstToken = remainingTokens.first()

                if (firstToken is Assembly.Token.Space) {
                    remainingTokens.remove(firstToken)
                    continue
                }

                if (parameterList.isEmpty() || parameterList.last() is Param.SplitSymbol) {
                    // Parameters
                    val offsetResult = Param.Offset.Syntax.sequence.match(*remainingTokens.toTypedArray())
                    if (offsetResult.matches) {
                        val constant = offsetResult.sequenceMap.get(0)
                        val lParan = offsetResult.sequenceMap.get(1)
                        val reg = offsetResult.sequenceMap.get(2)
                        val rParan = offsetResult.sequenceMap.get(3)
                        val offset = Param.Offset(constant.token, lParan.token, reg.token, rParan.token)
                        parameterList.add(offset)
                        remainingTokens.removeAll(offset.tokens.toSet())
                        continue
                    }

                    if (firstToken is Assembly.Token.Register) {
                        parameterList.add(Param.Register(firstToken))
                        remainingTokens.remove(firstToken)
                        continue
                    }

                    if (firstToken is Assembly.Token.Constant) {
                        parameterList.add(Param.Constant(firstToken))
                        remainingTokens.remove(firstToken)
                        continue
                    }
                } else {
                    // SplitSymbols
                    if (firstToken is Assembly.Token.Symbol && firstToken.content == ",") {
                        parameterList.add(Param.SplitSymbol(firstToken))
                        remainingTokens.remove(firstToken)
                        continue
                    }
                }

                validParams = false
                break
            }

            if (validParams && parameterList.isNotEmpty()) {
                val paramArray = parameterList.toTypedArray()
                val parameterCollection = ParamCollection(*paramArray)
                node.add(parameterCollection)
                params.add(parameterCollection)
            }


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


    sealed class Param(hlFlag: String, vararg val paramTokens: Assembly.Token) : TreeNode.TokenNode(hlFlag, *paramTokens) {

        class Offset(val offset: Assembly.Token, val openParan: Assembly.Token, val register: Assembly.Token, val closeParan: Assembly.Token) : Param(RISCVFlags.offset, offset, openParan, register, closeParan) {


            object Syntax {
                val sequence = Sequence(Sequence.SequenceComponent.InSpecific.Constant(), Sequence.SequenceComponent.Specific.Symbol("("), Sequence.SequenceComponent.InSpecific.Register(), Sequence.SequenceComponent.Specific.Symbol(")"), ignoreSpaces = true)
            }

        }

        class Variable(val word: Assembly.Token.Word) : Param(RISCVFlags.label, word) {

        }

        class Constant(val constant: Assembly.Token.Constant) : Param("", constant) {

        }

        class Register(val register: Assembly.Token.Register) : Param("", register) {

        }

        class SplitSymbol(val splitSymbol: Assembly.Token.Symbol) : Param("", splitSymbol) {

        }
    }

    class ParamCollection(vararg val params: Param) : TreeNode.CollectionNode(*params) {

        init {
            var parameterList = mutableListOf<String>()
            for (param in params) {
                if (param !is Param.SplitSymbol) {
                    parameterList += param.paramTokens.joinToString { it.content }
                }
            }
            console.log("line ${params[0].tokens[0].lineLoc.lineID + 1}: Grammar RiscV ParameterCollection <${parameterList.joinToString(",") { it }}>")
        }
    }

    class Label(vararg val name: Assembly.Token, colon: Assembly.Token.Symbol) : TreeNode.TokenNode(RISCVFlags.label, *name, colon) {

        val wholeName: String

        init {
            wholeName = name.joinToString("") { it.content }
            console.log("line ${colon.lineLoc.lineID + 1}: Grammar RiscV Label <$wholeName>")
        }
    }

    class Directive(val dot: Assembly.Token.Symbol, val type: Directive.Type, vararg tokens: Assembly.Token) : TreeNode.TokenNode(RISCVFlags.directive, dot, *tokens) {
        init {
            console.log("line ${dot.lineLoc.lineID + 1}: Grammar RiscV Directive <${type.name}>")
        }

        enum class Type {
            data,
            text
        }

    }

    class Instruction(val insToken: Assembly.Token.Instruction) : TreeNode.TokenNode(RISCVFlags.instruction, insToken) {

        init {
            console.log("line ${insToken.lineLoc.lineID + 1}: Grammar RiscV Instruction <${insToken.content}>")
        }
    }

    class PseudoInstruction(val name: Assembly.Token.Word, val type: Type) : TreeNode.TokenNode(RISCVFlags.pseudoInstruction, name) {

        init {

            console.log("line ${name.lineLoc.lineID + 1}: Grammar RiscV Instruction <${name.content}>")
        }

        enum class Type {
            li,
            beqz,
            j,
            ret,
            bltz,
            mv
        }

        object Types {
            val list = listOf(Type.li, Type.beqz, Type.j, Type.ret, Type.bltz, Type.mv)
        }
    }

    class Comment(val prefix: Assembly.Token.Symbol, vararg val content: Assembly.Token) : TreeNode.TokenNode(RISCVFlags.comment, prefix, *content) {

        val wholeContent: String

        init {
            wholeContent = content.joinToString("") { it.content }
            console.log("line ${prefix.lineLoc.lineID + 1}: Grammar RiscV Comment <$wholeContent>")
        }
    }

}