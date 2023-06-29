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

        var remainingLines = tokenLines.toMutableList()

        // PRE Elements (Labels)

        for (lineID in remainingLines.indices) {
            var remainingTokens = remainingLines[lineID].toMutableList()

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

            remainingLines[lineID] = remainingTokens
        }

        // MAIN Scan

        for (lineID in remainingLines.indices) {
            var remainingTokens = remainingLines[lineID].toMutableList()

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
                console.log("line ${lineID + 1}: remaining Tokens: ${remainingTokens.joinToString("") { it.content }}")
                var firstToken = remainingTokens.first()

                if (firstToken is Assembly.Token.Space) {
                    remainingTokens.remove(firstToken)
                    continue
                }

                if (parameterList.isEmpty() || parameterList.last() is Param.SplitSymbol) {
                    // Parameters
                    val offsetResult = Param.Offset.Syntax.sequence.matches(*remainingTokens.toTypedArray())
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

                    var labelLink: Param.LabelLink? = null
                    var tokensForLabelToCheck = mutableListOf<Assembly.Token>()
                    for (possibleLabelToken in remainingTokens.dropWhile { firstToken != it }) {
                        if (possibleLabelToken is Assembly.Token.Space || (possibleLabelToken.content == ",")) {
                            break
                        } else {
                            tokensForLabelToCheck.add(possibleLabelToken)
                        }
                    }
                    if (tokensForLabelToCheck.isNotEmpty()) {
                        for (label in labels) {
                            val labelResult = label.sequence.exactlyMatches(*tokensForLabelToCheck.toTypedArray())
                            if (labelResult.matches) {
                                val labelNameTokens = mutableListOf<Assembly.Token>()
                                for (entry in labelResult.sequenceMap) {
                                    labelNameTokens.add(entry.token)
                                }
                                labelLink = Param.LabelLink(label, *labelNameTokens.toTypedArray())
                                break
                            }
                        }
                        if (labelLink != null) {
                            parameterList.add(labelLink)
                            remainingTokens.removeAll(labelLink.labelName)
                            continue
                        }
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

            remainingLines[lineID] = remainingTokens
        }

        return GrammarTree(node)
    }


    sealed class Param(hlFlag: String, val type: String, vararg val paramTokens: Assembly.Token) : TreeNode.TokenNode(hlFlag, type, *paramTokens) {

        class Offset(val offset: Assembly.Token, val openParan: Assembly.Token, val register: Assembly.Token, val closeParan: Assembly.Token) : Param(RISCVFlags.offset, "Offset", offset, openParan, register, closeParan) {


            object Syntax {
                val sequence = Sequence(Sequence.SequenceComponent.InSpecific.Constant(), Sequence.SequenceComponent.Specific("("), Sequence.SequenceComponent.InSpecific.Register(), Sequence.SequenceComponent.Specific(")"), ignoreSpaces = true)
            }

        }

        class Constant(val constant: Assembly.Token.Constant) : Param("", "Constant", constant) {

        }

        class Register(val register: Assembly.Token.Register) : Param("", "Register", register) {

        }

        class SplitSymbol(val splitSymbol: Assembly.Token.Symbol) : Param(RISCVFlags.instruction, "SplitSymbol", splitSymbol) {

        }

        class LabelLink(val linkedLabel: Label, vararg val labelName: Assembly.Token) : Param(RISCVFlags.label, "LabelLink", *labelName) {

        }

    }

    class ParamCollection(vararg val params: Param) : TreeNode.CollectionNode("ParameterCollection", *params) {

        init {
            var parameterList = mutableListOf<String>()
            for (param in params) {
                if (param !is Param.SplitSymbol) {
                    parameterList += param.paramTokens.joinToString("") { it.content }
                }
            }
            console.log("line ${params[0].tokens[0].lineLoc.lineID + 1}: Grammar RiscV ParameterCollection <${parameterList.joinToString(",") { it }}>")
        }
    }

    class Label(vararg val labelName: Assembly.Token, colon: Assembly.Token.Symbol) : TreeNode.TokenNode(RISCVFlags.label, "Label", *labelName, colon) {

        val wholeName: String
        val sequence: Sequence

        init {
            wholeName = labelName.joinToString("") { it.content }
            val sequenceComponents = mutableListOf<Sequence.SequenceComponent>()
            for (token in labelName) {
                sequenceComponents.add(Sequence.SequenceComponent.Specific(token.content))
            }
            sequence = Sequence(*sequenceComponents.toTypedArray(), ignoreSpaces = false)
            console.log("line ${colon.lineLoc.lineID + 1}: Grammar RiscV Label <$wholeName>")
        }
    }

    class Directive(val dot: Assembly.Token.Symbol, val type: Directive.Type, vararg tokens: Assembly.Token) : TreeNode.TokenNode(RISCVFlags.directive, "Directive", dot, *tokens) {
        init {
            console.log("line ${dot.lineLoc.lineID + 1}: Grammar RiscV Directive <${type.name}>")
        }

        enum class Type {
            data,
            text
        }

    }

    class Instruction(val insToken: Assembly.Token.Instruction) : TreeNode.TokenNode(RISCVFlags.instruction, "Instruction", insToken) {

        init {
            console.log("line ${insToken.lineLoc.lineID + 1}: Grammar RiscV Instruction <${insToken.content}>")
        }
    }

    class PseudoInstruction(val insName: Assembly.Token.Word, val type: Type) : TreeNode.TokenNode(RISCVFlags.pseudoInstruction, "PseudoInstruction", insName) {

        init {

            console.log("line ${insName.lineLoc.lineID + 1}: Grammar RiscV Instruction <${insName.content}>")
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

    class Comment(val prefix: Assembly.Token.Symbol, vararg val content: Assembly.Token) : TreeNode.TokenNode(RISCVFlags.comment, "Comment", prefix, *content) {

        val wholeContent: String

        init {
            wholeContent = content.joinToString("") { it.content }
            console.log("line ${prefix.lineLoc.lineID + 1}: Grammar RiscV Comment <$wholeContent>")
        }
    }

}