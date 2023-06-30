package extendable.archs.riscv

import extendable.archs.riscv.RISCVGrammar.Instruction.ParameterType.*
import extendable.archs.riscv.RISCVGrammar.Instruction.Type.*
import extendable.archs.riscv.RISCVGrammar.PseudoInstruction.Type.*
import extendable.components.assembly.Assembly
import extendable.components.assembly.Grammar

class RISCVGrammar : Grammar() {

    /**
     * program: { [label] [directive | instruction] [comment] newline }
     * label: "name:"
     * instruction: "name reg,reg,imm" for example
     *
     */
    override val applyStandardHLForRest: Boolean = true

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

                    Directive.Type.byte.name -> {
                        val directive = Directive(dot, Directive.Type.byte, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        node.add(directive)
                        directives.add(directive)
                    }

                    Directive.Type.half.name -> {
                        val directive = Directive(dot, Directive.Type.half, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        node.add(directive)
                        directives.add(directive)
                    }

                    Directive.Type.word.name -> {
                        val directive = Directive(dot, Directive.Type.word, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        node.add(directive)
                        directives.add(directive)
                    }

                    Directive.Type.dword.name -> {
                        val directive = Directive(dot, Directive.Type.dword, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        node.add(directive)
                        directives.add(directive)
                    }

                    Directive.Type.asciz.name -> {
                        val directive = Directive(dot, Directive.Type.asciz, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        node.add(directive)
                        directives.add(directive)
                    }

                    Directive.Type.string.name -> {
                        val directive = Directive(dot, Directive.Type.string, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        node.add(directive)
                        directives.add(directive)
                    }
                }
            }


            // search instruction
            for (token in remainingTokens) {
                when (token) {
                    is Assembly.Token.Space -> {
                        continue
                    }

                    is Assembly.Token.Word -> {
                        for (type in PseudoInstruction.Type.values()) {
                            if (type.name.uppercase() == token.content.uppercase()) {
                                val pseudoInstr = PseudoInstruction(token, type)
                                remainingTokens.remove(token)
                                node.add(pseudoInstr)
                                pseudoInstructions.add(pseudoInstr)
                            }
                        }
                        for (type in Instruction.Type.values()) {
                            if (type.name.uppercase() == token.content.uppercase()) {
                                val instr = Instruction(token, type)
                                remainingTokens.remove(token)
                                node.add(instr)
                                instructions.add(instr)
                            }
                        }
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
                    val offsetResult = Param.Offset.Syntax.tokenSequence.matches(*remainingTokens.toTypedArray())
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
                            val labelResult = label.tokenSequence.exactlyMatches(*tokensForLabelToCheck.toTypedArray())
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

        // check Syntax
        var isTextArea = true

        for (treeNode in node) {
            val index = node.indexOf(treeNode)
            when (treeNode) {
                is PseudoInstruction -> {

                }

                is Instruction -> {

                }


                else -> {}
            }


        }




        return GrammarTree(node)
    }


    sealed class Param(hlFlag: String, val type: String, vararg val paramTokens: Assembly.Token) : TreeNode.TokenNode(hlFlag, type, *paramTokens) {

        class Offset(val offset: Assembly.Token, val openParan: Assembly.Token, val register: Assembly.Token, val closeParan: Assembly.Token) : Param(RISCVFlags.offset, RISCVGrammar.Syntax.NODE_PARAM_OFFSET, offset, openParan, register, closeParan) {


            object Syntax {
                val tokenSequence = TokenSequence(TokenSequence.SequenceComponent.InSpecific.Constant(), TokenSequence.SequenceComponent.Specific("("), TokenSequence.SequenceComponent.InSpecific.Register(), TokenSequence.SequenceComponent.Specific(")"), ignoreSpaces = true)
            }

        }

        class Constant(val constant: Assembly.Token.Constant) : Param("", Syntax.NODE_PARAM_CONST, constant) {

        }

        class Register(val register: Assembly.Token.Register) : Param("", Syntax.NODE_PARAM_REG, register) {

        }

        class SplitSymbol(val splitSymbol: Assembly.Token.Symbol) : Param(RISCVFlags.instruction, Syntax.NODE_PARAM_SPLIT, splitSymbol) {

        }

        class LabelLink(val linkedLabel: Label, vararg val labelName: Assembly.Token) : Param(RISCVFlags.label, Syntax.NODE_PARAM_LABELLINK, *labelName) {

        }

    }

    class ParamCollection(vararg val params: Param) : TreeNode.CollectionNode(Syntax.NODE_PARAMCOLLECTION, *params) {

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

    class Label(vararg val labelName: Assembly.Token, colon: Assembly.Token.Symbol) : TreeNode.TokenNode(RISCVFlags.label, Syntax.NODE_LABEL, *labelName, colon) {

        val wholeName: String
        val tokenSequence: TokenSequence

        init {
            wholeName = labelName.joinToString("") { it.content }
            val tokenSequenceComponents = mutableListOf<TokenSequence.SequenceComponent>()
            for (token in labelName) {
                tokenSequenceComponents.add(TokenSequence.SequenceComponent.Specific(token.content))
            }
            tokenSequence = TokenSequence(*tokenSequenceComponents.toTypedArray(), ignoreSpaces = false)
            console.log("line ${colon.lineLoc.lineID + 1}: Grammar RiscV Label <$wholeName>")
        }
    }

    class Directive(val dot: Assembly.Token.Symbol, val type: Directive.Type, vararg tokens: Assembly.Token) : TreeNode.TokenNode(RISCVFlags.directive, Syntax.NODE_DIRECTIVE, dot, *tokens) {
        init {
            console.log("line ${dot.lineLoc.lineID + 1}: Grammar RiscV Directive <${type.name}>")
        }

        enum class Type {
            data,
            text,
            byte,
            half,
            word,
            dword,
            asciz,
            string
        }

    }

    class Instruction(val insToken: Assembly.Token.Word, val type: Type) : TreeNode.TokenNode(RISCVFlags.instruction, Syntax.NODE_INSTR, insToken) {

        val paramType: ParameterType

        init {
            console.log("line ${insToken.lineLoc.lineID + 1}: Grammar RiscV Instruction <${insToken.content}>")
            paramType = when (type) {
                LUI -> RI
                AUIPC -> RI
                JAL -> JUMP
                JALR -> JUMP
                ECALL -> NONE
                EBREAK -> NONE
                BEQ -> BRANCH
                BNE -> BRANCH
                BLT -> BRANCH
                BGE -> BRANCH
                BLTU -> BRANCH
                BGEU -> BRANCH
                LB -> LOADSAVE
                LH -> LOADSAVE
                LW -> LOADSAVE
                LBU -> LOADSAVE
                LHU -> LOADSAVE
                SB -> LOADSAVE
                SH -> LOADSAVE
                SW -> LOADSAVE
                ADDI -> LOGICANDCALCIMM
                SLTI -> LOGICANDCALCIMM
                SLTIU -> LOGICANDCALCIMM
                XORI -> LOGICANDCALCIMM
                ORI -> LOGICANDCALCIMM
                ANDI -> LOGICANDCALCIMM
                SLLI -> LOGICANDCALCIMM
                SRLI -> LOGICANDCALCIMM
                SRAI -> LOGICANDCALCIMM
                ADD -> LOGICANDCALC
                SUB -> LOGICANDCALC
                SLL -> LOGICANDCALC
                SLT -> LOGICANDCALC
                SLTU -> LOGICANDCALC
                XOR -> LOGICANDCALC
                SRL -> LOGICANDCALC
                SRA -> LOGICANDCALC
                OR -> LOGICANDCALC
                AND -> LOGICANDCALC
            }
        }

        private fun check(parameterCollection: ParamCollection): Boolean {
            val matches: Boolean
            val trimmedParamColl = mutableListOf<Param>()
            for (param in parameterCollection.params) {
                when (param) {
                    is Param.SplitSymbol -> {}
                    else -> {
                        trimmedParamColl.add(param)
                    }
                }
            }

            when (paramType) {
                RI -> {
                    matches = if (trimmedParamColl.size == 2) {
                        trimmedParamColl[0] is Param.Register && trimmedParamColl[1] is Param.Constant
                    } else {
                        false
                    }
                }

                LOADSAVE -> {
                    matches = if (trimmedParamColl.size == 2) {
                        trimmedParamColl[0] is Param.Register &&
                                (trimmedParamColl[1] is Param.Offset || trimmedParamColl[1] is Param.LabelLink)
                    } else {
                        false
                    }
                }

                LOGICANDCALC -> {
                    matches = if (trimmedParamColl.size == 3) {
                        trimmedParamColl[0] is Param.Register &&
                                trimmedParamColl[1] is Param.Register &&
                                trimmedParamColl[2] is Param.Register
                    } else {
                        false
                    }
                }

                LOGICANDCALCIMM -> {
                    matches = if (trimmedParamColl.size == 3) {
                        trimmedParamColl[0] is Param.Register &&
                                trimmedParamColl[1] is Param.Register &&
                                trimmedParamColl[2] is Param.Constant
                    } else {
                        false
                    }
                }

                BRANCH -> {
                    matches = if (trimmedParamColl.size == 3) {
                        trimmedParamColl[0] is Param.Register &&
                                trimmedParamColl[1] is Param.Register &&
                                (trimmedParamColl[2] is Param.Constant || trimmedParamColl[2] is Param.LabelLink)
                    } else {
                        false
                    }
                }

                JUMP -> {
                    matches = if (trimmedParamColl.size == 2) {
                        trimmedParamColl[0] is Param.Register &&
                                trimmedParamColl[1] is Param.LabelLink
                    } else {
                        false
                    }
                }

                NONE -> {
                    return trimmedParamColl.size == 0
                }
            }
            return matches
        }

        enum class ParameterType {
            RI, // rd, imm
            LOADSAVE, // load: rd, imm(rs) oder store: rs2, imm(rs1)
            LOGICANDCALC, // rd, rs1, rs2
            LOGICANDCALCIMM, // rd, rs, imm/shamt
            BRANCH, // rs1, rs2, imm
            JUMP, // rd, rs, imm
            NONE,
        }

        enum class Type {
            LUI,
            AUIPC,
            JAL,
            JALR,
            ECALL,
            EBREAK,
            BEQ,
            BNE,
            BLT,
            BGE,
            BLTU,
            BGEU,
            LB,
            LH,
            LW,
            LBU,
            LHU,
            SB,
            SH,
            SW,
            ADDI,
            SLTI,
            SLTIU,
            XORI,
            ORI,
            ANDI,
            SLLI,
            SRLI,
            SRAI,
            ADD,
            SUB,
            SLL,
            SLT,
            SLTU,
            XOR,
            SRL,
            SRA,
            OR,
            AND
        }

    }

    class PseudoInstruction(val insName: Assembly.Token.Word, val type: Type) : TreeNode.TokenNode(RISCVFlags.pseudoInstruction, Syntax.NODE_PSEUDOINSTR, insName) {

        val paramType: ParameterType

        init {
            console.log("line ${insName.lineLoc.lineID + 1}: Grammar RiscV Instruction <${insName.content}>")
            paramType = when (type) {
                li -> ParameterType.RI
                beqz -> ParameterType.RL
                bltz -> ParameterType.RL
                j -> ParameterType.L
                ret -> ParameterType.NONE
                mv -> ParameterType.RR
            }
        }

        fun check(paramCollection: ParamCollection): Boolean {
            val matches: Boolean
            val trimmedParamColl = mutableListOf<Param>()
            for (param in paramCollection.params) {
                when (param) {
                    is Param.SplitSymbol -> {}
                    else -> {
                        trimmedParamColl.add(param)
                    }
                }
            }

            matches = when (paramType) {
                ParameterType.RI -> if (trimmedParamColl.size == 2) {
                    trimmedParamColl[0] is Param.Register && trimmedParamColl[1] is Param.Constant
                } else {
                    false
                }

                ParameterType.RL -> if (trimmedParamColl.size == 2) {
                    trimmedParamColl[0] is Param.Register && trimmedParamColl[1] is Param.LabelLink
                } else {
                    false
                }

                ParameterType.L -> if (trimmedParamColl.size == 1) {
                    trimmedParamColl[0] is Param.LabelLink
                } else {
                    false
                }

                ParameterType.RR -> if (trimmedParamColl.size == 2) {
                    trimmedParamColl[0] is Param.Register && trimmedParamColl[1] is Param.Register
                } else {
                    false
                }

                ParameterType.NONE -> trimmedParamColl.size == 0
            }
            return matches

        }

        enum class Type {
            li,
            beqz,
            bltz,
            j,
            ret,
            mv
        }

        enum class ParameterType {
            RI, // rd, imm
            RL, // rs, label
            L,  // label
            RR, // rd, rs
            NONE
        }

        object Types {
            val list = listOf(li, beqz, j, ret, bltz, mv)
        }
    }

    class Comment(val prefix: Assembly.Token.Symbol, vararg val content: Assembly.Token) : TreeNode.TokenNode(RISCVFlags.comment, Syntax.NODE_COMMENT, prefix, *content) {

        val wholeContent: String

        init {
            wholeContent = content.joinToString("") { it.content }
            console.log("line ${prefix.lineLoc.lineID + 1}: Grammar RiscV Comment <$wholeContent>")
        }
    }


    class LabelDefinition(val type: Type, val label: Label, val directive: Directive? = null, val param: Param? = null) {

        enum class Type {
            JUMP,
            MEMALLOC,
        }

    }

    class InstructionDefinition(val instruction: Instruction, val paramCollection: ParamCollection) {


    }

    object Syntax {
        // Tier 1
        const val NODE_LABEL = "Label"
        const val NODE_DIRECTIVE = "Directive"
        const val NODE_INSTR = "Instr"
        const val NODE_PSEUDOINSTR = "PseudoInstr"
        const val NODE_PARAMCOLLECTION = "Params"
        const val NODE_COMMENT = "Comment"

        const val NODE_PARAM_OFFSET = "Offset"
        const val NODE_PARAM_SPLIT = "Splitter"
        const val NODE_PARAM_CONST = "Constant"
        const val NODE_PARAM_REG = "Register"
        const val NODE_PARAM_LABELLINK = "LabelLink"

        // Tier 2
        const val NODE2_LABELDEF = "LabelDefinition"
        const val NODE2_INSTRDEF = "InstructionDefinition"

        val NODE2_INSTRDEF_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE_INSTR))
        val NODE2_INSTRDEF2_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE_INSTR), SyntaxSequence.Component(NODE_PARAMCOLLECTION))
        val NODE2_PSEUDOINSTRDEF_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE_PSEUDOINSTR))
        val NODE2_PSEUDOINSTRDEF2_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE_PSEUDOINSTR), SyntaxSequence.Component(NODE_PARAMCOLLECTION))
        val NODE2_JUMPLABEL_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE_LABEL))
        val NODE2_DIRECTIVE_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE_DIRECTIVE))




    }


}