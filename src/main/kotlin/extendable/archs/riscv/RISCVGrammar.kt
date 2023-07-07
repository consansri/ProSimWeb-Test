package extendable.archs.riscv

import extendable.archs.riscv.RISCVBinMapper.*
import extendable.archs.riscv.RISCVGrammar.T1Directive.Type.*
import extendable.archs.riscv.RISCVGrammar.T1Instr.ParameterType.*
import extendable.archs.riscv.RISCVGrammar.T1Instr.Type.*
import extendable.components.assembly.Compiler
import extendable.components.assembly.Grammar
import extendable.components.types.ByteValue

import tools.DebugTools

class RISCVGrammar : Grammar() {

    /**
     * program: { [label] [directive | instruction] [comment] newline }
     * label: "name:"
     * instruction: "name reg,reg,imm" for example
     *
     */
    override val applyStandardHLForRest: Boolean = true

    var t1Labels = mutableListOf<T1Label>()
    var t1Directives = mutableListOf<T1Directive>()
    var t1Instrs = mutableListOf<T1Instr>()
    var params = mutableListOf<T1ParamColl>()

    var t1Comments = mutableListOf<T1Comment>()
    var errors = mutableListOf<Grammar.Error>()

    override fun clear() {
        t1Labels.clear()
        t1Directives.clear()
        t1Instrs.clear()
        params.clear()
        t1Comments.clear()
    }

    override fun check(tokenLines: List<List<Compiler.Token>>): GrammarTree {

        errors.clear()

        val tier1Lines = mutableListOf<List<TreeNode>>()

        for (lineID in tokenLines.indices) {
            tier1Lines.add(lineID, emptyList())
        }

        var remainingLines = tokenLines.toMutableList()

        // TIER 1 PRE Elements (Labels)
        for (lineID in remainingLines.indices) {
            var remainingTokens = remainingLines[lineID].toMutableList()
            val tier1Line = mutableListOf<TreeNode>()

            // search Label
            for (token in remainingTokens) {
                when (token) {
                    is Compiler.Token.Symbol -> {
                        if (token.content == ":") {
                            val tokenIndex = remainingTokens.indexOf(token)
                            val colon = token
                            val labelName = mutableListOf<Compiler.Token>()

                            if (tokenIndex + 1 < remainingTokens.size) {
                                if (remainingTokens[tokenIndex + 1] !is Compiler.Token.Space) {
                                    continue
                                }
                            }

                            var previous = tokenIndex - 1

                            while (previous >= 0) {
                                val prevToken = remainingTokens[previous]
                                when (prevToken) {
                                    is Compiler.Token.Space -> {
                                        break
                                    }

                                    else -> {
                                        labelName.add(0, prevToken)
                                    }
                                }

                                previous--
                            }
                            val labelNameString = labelName.joinToString("") { it.content }
                            var alreadyDefined = false
                            for (label in t1Labels) {
                                if (label.wholeName == labelNameString) {
                                    alreadyDefined = true
                                }
                            }
                            if (alreadyDefined) {
                                errors.add(Error("Mutliple Labels with same name not possible!", *labelName.toTypedArray(), colon))
                                remainingTokens.removeAll(labelName)
                                remainingTokens.remove(colon)
                                break
                            }

                            val t1Label = T1Label(*labelName.toTypedArray(), colon = colon)
                            remainingTokens.removeAll(labelName)
                            remainingTokens.remove(colon)
                            t1Labels.add(t1Label)
                            tier1Line.add(t1Label)
                            break
                        }
                    }

                    else -> {

                    }
                }
            }

            remainingLines[lineID] = remainingTokens
            tier1Lines[lineID] += tier1Line
        }

        // TIER 1 MAIN Scan
        for (lineID in remainingLines.indices) {
            var remainingTokens = remainingLines[lineID].toMutableList()
            val tier1Line = mutableListOf<TreeNode>()

            // search directive
            var directiveName = mutableListOf<Compiler.Token.Word>()
            var dot: Compiler.Token.Symbol? = null
            for (token in remainingTokens) {
                when (token) {
                    is Compiler.Token.Symbol -> {
                        if (token.content == ".") {
                            dot = token
                            directiveName.clear()
                            continue
                        } else {
                            break
                        }
                    }

                    is Compiler.Token.Space -> {
                        if (dot != null && directiveName.isNotEmpty()) {
                            break
                        } else {
                            continue
                        }
                    }

                    is Compiler.Token.Word -> {
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
                    data.name -> {
                        val t1Directive = T1Directive(dot, data, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        t1Directives.add(t1Directive)
                        tier1Line.add(t1Directive)
                    }

                    text.name -> {
                        val t1Directive = T1Directive(dot, text, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        t1Directives.add(t1Directive)
                        tier1Line.add(t1Directive)
                    }

                    byte.name -> {
                        val t1Directive = T1Directive(dot, byte, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        t1Directives.add(t1Directive)
                        tier1Line.add(t1Directive)
                    }

                    half.name -> {
                        val t1Directive = T1Directive(dot, half, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        t1Directives.add(t1Directive)
                        tier1Line.add(t1Directive)
                    }

                    word.name -> {
                        val t1Directive = T1Directive(dot, word, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        t1Directives.add(t1Directive)
                        tier1Line.add(t1Directive)
                    }

                    dword.name -> {
                        val t1Directive = T1Directive(dot, dword, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        t1Directives.add(t1Directive)
                        tier1Line.add(t1Directive)
                    }

                    asciz.name -> {
                        val t1Directive = T1Directive(dot, asciz, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        t1Directives.add(t1Directive)
                        tier1Line.add(t1Directive)
                    }

                    string.name -> {
                        val t1Directive = T1Directive(dot, string, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        t1Directives.add(t1Directive)
                        tier1Line.add(t1Directive)
                    }
                }
            }


            // search instruction
            for (token in remainingTokens) {
                when (token) {
                    is Compiler.Token.Space -> {
                        continue
                    }

                    is Compiler.Token.Word -> {
                        val validTypes = mutableListOf<T1Instr.Type>()
                        for (type in T1Instr.Type.values()) {
                            if (type.id.uppercase() == token.content.uppercase()) {
                                validTypes.add(type)

                            }
                        }
                        if (validTypes.isNotEmpty()) {
                            val instr = T1Instr(token, *validTypes.toTypedArray())
                            remainingTokens.remove(token)
                            tier1Line.add(instr)
                            t1Instrs.add(instr)
                        }
                    }

                    else -> {
                        break
                    }
                }
            }

            // search Parameters
            val parameterList = mutableListOf<T1Param>()
            var validParams = true
            var labelBuilder = mutableListOf<Compiler.Token>()

            while (remainingTokens.isNotEmpty()) {
                var firstToken = remainingTokens.first()

                if (firstToken is Compiler.Token.Space) {
                    remainingTokens.remove(firstToken)
                    continue
                }

                if (parameterList.isEmpty() || parameterList.last() is T1Param.SplitSymbol) {
                    // Parameters
                    val offsetResult = T1Param.Offset.Syntax.tokenSequence.matches(*remainingTokens.toTypedArray())
                    if (offsetResult.matches) {
                        val constant = offsetResult.sequenceMap.get(0)
                        val lParan = offsetResult.sequenceMap.get(1)
                        val reg = offsetResult.sequenceMap.get(2)
                        val rParan = offsetResult.sequenceMap.get(3)
                        val offset = T1Param.Offset(constant.token as Compiler.Token.Constant, lParan.token, reg.token as Compiler.Token.Register, rParan.token)
                        parameterList.add(offset)
                        remainingTokens.removeAll(offset.tokens.toSet())
                        continue
                    }

                    if (firstToken is Compiler.Token.Register) {
                        parameterList.add(T1Param.Register(firstToken))
                        remainingTokens.remove(firstToken)
                        continue
                    }

                    if (firstToken is Compiler.Token.Constant) {
                        parameterList.add(T1Param.Constant(firstToken))
                        remainingTokens.remove(firstToken)
                        continue
                    }

                    var labelLink: T1Param.LabelLink? = null
                    var tokensForLabelToCheck = mutableListOf<Compiler.Token>()
                    for (possibleLabelToken in remainingTokens.dropWhile { firstToken != it }) {
                        if (possibleLabelToken is Compiler.Token.Space || (possibleLabelToken.content == ",")) {
                            break
                        } else {
                            tokensForLabelToCheck.add(possibleLabelToken)
                        }
                    }
                    if (tokensForLabelToCheck.isNotEmpty()) {
                        for (label in t1Labels) {
                            val labelResult = label.tokenSequence.exactlyMatches(*tokensForLabelToCheck.toTypedArray())
                            if (labelResult.matches) {
                                val labelNameTokens = mutableListOf<Compiler.Token>()
                                for (entry in labelResult.sequenceMap) {
                                    labelNameTokens.add(entry.token)
                                }
                                labelLink = T1Param.LabelLink(label, *labelNameTokens.toTypedArray())
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
                    if (firstToken is Compiler.Token.Symbol && firstToken.content == ",") {
                        parameterList.add(T1Param.SplitSymbol(firstToken))
                        remainingTokens.remove(firstToken)
                        continue
                    }
                }

                validParams = false
                break
            }

            if (validParams && parameterList.isNotEmpty()) {
                val paramArray = parameterList.toTypedArray()
                val parameterCollection = T1ParamColl(*paramArray)
                tier1Line.add(parameterCollection)
                params.add(parameterCollection)
            }


            // search comment
            for (token in remainingTokens) {
                when (token) {
                    is Compiler.Token.Symbol -> {
                        if (token.content == "#") {
                            val content = remainingTokens.dropWhile { it != token }.drop(1).toTypedArray()
                            val t1Comment = T1Comment(token, *content)
                            t1Comments.add(t1Comment) // not included in tier1Lines
                            remainingTokens.remove(token)
                            remainingTokens.removeAll(content)
                            break
                        }
                    }

                    else -> {

                    }
                }
            }

            remainingLines[lineID] = remainingTokens
            if (remainingTokens.isNotEmpty()) {
                errors.add(Grammar.Error(message = "couldn't match Tokens to RiscV Tier1 TokenNode!", *remainingTokens.toTypedArray()))
            }

            tier1Lines[lineID] += tier1Line
        }
        if (DebugTools.RISCV.showGrammarScanTiers) {
            console.log("Tier 1 Main Scan: ${tier1Lines.joinToString(", line: ") { it.joinToString(" ") { it.name } }}")
        }

        // TIER 2 MAIN Scan (check Syntax) | Ignore Comments
        var isTextArea = true
        val tier2Lines = mutableListOf<TreeNode?>()

        if (tier1Lines.isEmpty()) {
            return GrammarTree()
        }

        for (tier1Line in tier1Lines) {
            val tier2Node: TreeNode?

            if (tier1Line.isEmpty()) {
                continue
            }

            var result = Syntax.NODE2_INSTRDEF_SYNTAX.exacltyMatches(*tier1Line.toTypedArray())
            if (result.matches) {
                val t1Instr = result.matchingTreeNodes[0] as T1Instr
                val paramCheck = t1Instr.check()
                if (paramCheck.first) {
                    tier2Node = T2InstrDef(t1Instr, null, paramCheck.second)
                    tier2Lines.add(tier2Node)
                    result.error?.let {
                        errors.add(it)
                    }
                    continue
                } else {
                    errors.add(Grammar.Error("Instruction Definition: parameters aren't matching instruction ${t1Instr.types.first().name}!\nExpecting: ${t1Instr.types.joinToString(" or ") { it.paramType.exampleString }}", *tier1Line.toTypedArray()))
                    continue
                }
            }

            result = Syntax.NODE2_INSTRDEF2_SYNTAX.exacltyMatches(*tier1Line.toTypedArray())
            if (result.matches) {
                if (result.matchingTreeNodes.size > 0) {
                    val t1Instr = result.matchingTreeNodes[0] as T1Instr

                    val t1ParamColl: T1ParamColl?
                    if (result.matchingTreeNodes.size == 2) {
                        t1ParamColl = result.matchingTreeNodes[1] as T1ParamColl
                    } else {
                        t1ParamColl = null
                    }
                    if (t1ParamColl != null) {
                        val paramCheck = t1Instr.check(t1ParamColl)
                        if (paramCheck.first) {
                            tier2Node = T2InstrDef(t1Instr, t1ParamColl, paramCheck.second)
                            tier2Lines.add(tier2Node)
                            result.error?.let {
                                errors.add(it)
                            }
                            continue
                        } else {
                            errors.add(Grammar.Error("Instruction Definition: parameters aren't matching instruction ${t1Instr.types.first().name}!\nExpecting: ${t1Instr.types.joinToString(" or ") { it.paramType.exampleString }}", *tier1Line.toTypedArray()))
                            continue
                        }
                    }
                }
            }

            result = Syntax.NODE2_ADDRESSALLOCLABEL_SYNTAX.exacltyMatches(*tier1Line.toTypedArray())
            if (result.matches) {
                if (result.matchingTreeNodes.size == 3) {
                    val t1Label = result.matchingTreeNodes[0] as T1Label
                    val t1Directive = result.matchingTreeNodes[1] as T1Directive
                    val t1ParamColl = result.matchingTreeNodes[2] as T1ParamColl
                    if (t1ParamColl.paramsWithOutSplitSymbols.size == 1 && t1Directive.isTypeDirective() && t1ParamColl.paramsWithOutSplitSymbols.first() is T1Param.Constant) {
                        tier2Node = T2LabelDef(T2LabelDef.Type.MEMALLOC, t1Label, t1Directive, t1ParamColl)
                        tier2Lines.add(tier2Node)
                    } else {
                        errors.add(Grammar.Error(message = "Memory / Address Allocation: " + if (!t1Directive.isTypeDirective()) "invalid type!" else "invalid parameter count!", nodes = tier1Line.toTypedArray()))
                        continue
                    }
                    result.error?.let {
                        errors.add(it)
                    }
                    continue
                }
            }

            result = Syntax.NODE2_JUMPLABEL_SYNTAX.exacltyMatches(*tier1Line.toTypedArray())

            if (result.matches) {
                if (result.matchingTreeNodes.size == 1) {
                    val t1Label = result.matchingTreeNodes[0] as T1Label
                    tier2Node = T2LabelDef(T2LabelDef.Type.JUMP, t1Label)
                    tier2Lines.add(tier2Node)
                    result.error?.let {
                        errors.add(it)
                    }
                    continue
                }
            }
            result = Syntax.NODE2_SECTIONSTART_SYNTAX.exacltyMatches(*tier1Line.toTypedArray())

            if (result.matches) {
                if (result.matchingTreeNodes.size == 1) {
                    val t1Directive = result.matchingTreeNodes[0] as T1Directive
                    when (t1Directive.type) {
                        data -> {
                            tier2Node = T2DataSectionStart(t1Directive)
                            tier2Lines.add(tier2Node)
                            result.error?.let {
                                errors.add(it)
                            }
                            continue
                        }

                        text -> {
                            tier2Node = T2TextSectionStart(t1Directive)
                            tier2Lines.add(tier2Node)
                            result.error?.let {
                                errors.add(it)
                            }
                            continue
                        }

                        else -> {

                        }
                    }
                }
            }

            for (tokenNode in tier1Line) {
                errors.add(Grammar.Error("couldn't match RiscV Tier1 Nodes to RiscV Tier2 Node!", tokenNode))
            }

        }
        if (DebugTools.RISCV.showGrammarScanTiers) {
            console.log("Tier 2 Main Scan: ${tier2Lines.filterNotNull().joinToString { it.name }}")
        }

        // TIER 3 MAIN Scan (apply Sections) | Ignore Comments
        val sections = mutableListOf<TreeNode.SectionNode>()

        val notNullT2Lines = tier2Lines.filterNotNull().toMutableList()
        var isTextSection = true
        var sectionIdentification: TreeNode.CollectionNode? = null
        var sectionContent = mutableListOf<TreeNode.CollectionNode>()

        while (notNullT2Lines.isNotEmpty()) {
            val firstT2Line = notNullT2Lines.first()

            when (firstT2Line) {
                is T2DataSectionStart -> {
                    if (isTextSection) {
                        if (sectionIdentification != null) {
                            sections.add(T3TextSection(sectionIdentification as T2TextSectionStart, *sectionContent.toTypedArray()))
                        } else {
                            sections.add(T3TextSection(collectionNodes = sectionContent.toTypedArray()))
                        }
                        sectionContent.clear()

                    } else {
                        sections.add(T3DataSection(sectionIdentification as T2DataSectionStart, *sectionContent.toTypedArray()))
                        sectionContent.clear()
                    }
                    isTextSection = false
                    sectionIdentification = firstT2Line
                    notNullT2Lines.removeFirst()
                    continue

                }

                is T2TextSectionStart -> {
                    if (isTextSection) {
                        if (sectionIdentification != null) {
                            sections.add(T3TextSection(sectionIdentification as T2TextSectionStart, *sectionContent.toTypedArray()))
                        } else {
                            sections.add(T3TextSection(collectionNodes = sectionContent.toTypedArray()))
                        }

                        sectionContent.clear()

                    } else {
                        sections.add(T3DataSection(sectionIdentification as T2DataSectionStart, *sectionContent.toTypedArray()))
                        sectionContent.clear()
                    }
                    isTextSection = true
                    sectionIdentification = firstT2Line
                    notNullT2Lines.removeFirst()
                    continue
                }

                is T2InstrDef -> {
                    if (isTextSection) {
                        sectionContent.add(firstT2Line)
                        notNullT2Lines.removeFirst()
                        continue
                    }
                }

                is T2LabelDef -> {
                    if (isTextSection) {
                        if (firstT2Line.type == T2LabelDef.Type.JUMP) {
                            sectionContent.add(firstT2Line)
                            notNullT2Lines.removeFirst()
                            continue
                        }
                    } else {
                        if (firstT2Line.type == T2LabelDef.Type.MEMALLOC) {
                            sectionContent.add(firstT2Line)
                            notNullT2Lines.removeFirst()
                            continue
                        }
                    }
                }

                else -> {}

            }

            errors.add(Grammar.Error(message = "Element not possible in ${if (isTextSection) "text section" else "data section"}!", linkedTreeNode = firstT2Line))
            notNullT2Lines.removeFirst()
        }

        if (isTextSection) {
            if (sectionIdentification != null) {
                sections.add(T3TextSection(sectionIdentification as T2TextSectionStart, collectionNodes = sectionContent.toTypedArray()))
            } else {
                sections.add(T3TextSection(collectionNodes = sectionContent.toTypedArray()))
            }
            sectionContent.clear()

        } else {
            if (sectionIdentification != null) {
                sections.add(T3DataSection(sectionIdentification as T2DataSectionStart, collectionNodes = sectionContent.toTypedArray()))
                sectionContent.clear()
            } else {
                errors.add(Grammar.Error(message = "No Valid Data Section Identification found!", sectionContent.first()))
            }
        }
        if (DebugTools.RISCV.showGrammarScanTiers) {
            console.log("Tier 3 Main Scan: ${sections.joinToString { it.name }}")
        }

        // Build Comment Node
        val commentNode = T2CommentCollection(*t1Comments.toTypedArray())
        // Build CodeRoot Node
        val rootNode = T4CodeRoot(commentNode, errors, *sections.toTypedArray())

        return GrammarTree(rootNode)
    }

    /* ---------- SYNTAX ------------ */

    object Syntax {
        // Tier 1
        const val NODE_LABEL = "Label"
        const val NODE_DIRECTIVE = "Directive"
        const val NODE_INSTR = "Instr"
        const val NODE_PARAMCOLLECTION = "Params"
        const val NODE_COMMENT = "Comment"

        const val NODE_PARAM_OFFSET = "Offset"
        const val NODE_PARAM_SPLIT = "Splitter"
        const val NODE_PARAM_CONST = "Constant"
        const val NODE_PARAM_REG = "Register"
        const val NODE_PARAM_LABELLINK = "LabelLink"

        // Tier 2
        const val NODE2_LABELDEF = "LabelDef"
        const val NODE2_LABELJUMP = "LabelJump"
        const val NODE2_INSTRDEF = "InstrDef"
        const val NODE2_TEXTSECTIONSTART = "TextSectionStart"
        const val NODE2_DATASECTIONSTART = "DataSectionStart"
        const val NODE2_COMMENTCOLLECTION = "AllComments"

        val NODE2_INSTRDEF_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE_INSTR))
        val NODE2_INSTRDEF2_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE_INSTR), SyntaxSequence.Component(NODE_PARAMCOLLECTION))
        val NODE2_JUMPLABEL_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE_LABEL))
        val NODE2_ADDRESSALLOCLABEL_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE_LABEL), SyntaxSequence.Component(NODE_DIRECTIVE), SyntaxSequence.Component(NODE_PARAMCOLLECTION))
        val NODE2_SECTIONSTART_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE_DIRECTIVE))

        // Tier 3
        const val NODE3_SECTION_DATA = "Data Section"
        const val NODE3_SECTION_TEXT = "Text Section"

        val NODE3_SECTION_DATA_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE2_DATASECTIONSTART), SyntaxSequence.Component(NODE2_LABELDEF, repeatable = true))
        val NODE3_SECTION_TEXT_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE2_TEXTSECTIONSTART), SyntaxSequence.Component(NODE2_INSTRDEF, NODE2_LABELJUMP, repeatable = true))
        val NODE3_SECTION_TEXT1_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE2_INSTRDEF, NODE2_LABELJUMP, repeatable = true))

        // Tier 4
        const val NODE4_ROOT = "root"
    }


    /* ---------- All SYNTAX Tokens which all implement a TreeNode ------------ */

    /* ---------- SYNTAX Tokens: Tier 1 ------------ */

    sealed class T1Param(hlFlag: String, val type: String, vararg val paramTokens: Compiler.Token) : TreeNode.TokenNode(hlFlag, type, *paramTokens) {

        class Offset(val offset: Compiler.Token.Constant, val openParan: Compiler.Token, val register: Compiler.Token.Register, val closeParan: Compiler.Token) : T1Param(RISCVFlags.offset, RISCVGrammar.Syntax.NODE_PARAM_OFFSET, offset, openParan, register, closeParan) {

            object Syntax {
                val tokenSequence = TokenSequence(TokenSequence.SequenceComponent.InSpecific.Constant(), TokenSequence.SequenceComponent.Specific("("), TokenSequence.SequenceComponent.InSpecific.Register(), TokenSequence.SequenceComponent.Specific(")"), ignoreSpaces = true)
            }

            fun getValueArray(): Array<ByteValue.Type> {
                return arrayOf(offset.getValue(), register.reg.address)
            }

        }

        class Constant(val constant: Compiler.Token.Constant) : T1Param("", Syntax.NODE_PARAM_CONST, constant) {
            fun getValue(): ByteValue.Type {
                return constant.getValue()
            }
        }

        class Register(val register: Compiler.Token.Register) : T1Param("", Syntax.NODE_PARAM_REG, register) {
            fun getAddress(): ByteValue.Type {
                return register.reg.address
            }
        }

        class SplitSymbol(val splitSymbol: Compiler.Token.Symbol) : T1Param(RISCVFlags.instruction, Syntax.NODE_PARAM_SPLIT, splitSymbol) {

        }

        class LabelLink(val linkedT1Label: T1Label, vararg val labelName: Compiler.Token) : T1Param(RISCVFlags.label, Syntax.NODE_PARAM_LABELLINK, *labelName) {

        }

    }

    class T1ParamColl(vararg val t1Params: T1Param) : TreeNode.CollectionNode(Syntax.NODE_PARAMCOLLECTION, *t1Params) {

        val paramsWithOutSplitSymbols: Array<T1Param>

        init {
            val parameterList = mutableListOf<T1Param>()
            for (param in t1Params) {
                if (param !is T1Param.SplitSymbol) {
                    parameterList.add(param)
                }
            }
            paramsWithOutSplitSymbols = parameterList.toTypedArray()
        }

        fun getValues(): Array<ByteValue.Type> {
            val values = mutableListOf<ByteValue.Type>()
            paramsWithOutSplitSymbols.forEach {
                when (it) {
                    is T1Param.Constant -> {
                        values.add(it.getValue())
                    }

                    is T1Param.Offset -> {
                        values.addAll(it.getValueArray())
                    }

                    is T1Param.Register -> {
                        values.add(it.getAddress())
                    }

                    else -> {}
                }
            }
            return values.toTypedArray()
        }

        fun getLabels(): Array<T1Label> {
            val labels = mutableListOf<T1Label>()
            paramsWithOutSplitSymbols.forEach {
                when(it){
                    is T1Param.LabelLink -> {
                        labels.add(it.linkedT1Label)
                    }
                    else -> {}
                }
            }
            return labels.toTypedArray()
        }

    }

    class T1Label(vararg val labelName: Compiler.Token, colon: Compiler.Token.Symbol) : TreeNode.TokenNode(RISCVFlags.label, Syntax.NODE_LABEL, *labelName, colon) {

        val wholeName: String
        val tokenSequence: TokenSequence

        init {
            wholeName = labelName.joinToString("") { it.content }
            val tokenSequenceComponents = mutableListOf<TokenSequence.SequenceComponent>()
            for (token in labelName) {
                tokenSequenceComponents.add(TokenSequence.SequenceComponent.Specific(token.content))
            }
            tokenSequence = TokenSequence(*tokenSequenceComponents.toTypedArray(), ignoreSpaces = false)
        }
    }

    class T1Directive(val dot: Compiler.Token.Symbol, val type: T1Directive.Type, vararg tokens: Compiler.Token) : TreeNode.TokenNode(RISCVFlags.directive, Syntax.NODE_DIRECTIVE, dot, *tokens) {
        init {

        }

        fun isTypeDirective(): Boolean {
            when (type) {
                byte, half, word, dword, asciz, string -> {
                    return true
                }

                else -> {
                    return false
                }
            }
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

    class T1Instr(val insToken: Compiler.Token.Word, vararg val types: Type) : TreeNode.TokenNode(RISCVFlags.instruction, Syntax.NODE_INSTR, insToken) {


        fun check(parameterCollection: T1ParamColl = T1ParamColl()): Pair<Boolean, Type> {

            val trimmedT1ParamColl = mutableListOf<T1Param>()
            for (param in parameterCollection.t1Params) {
                when (param) {
                    is T1Param.SplitSymbol -> {}
                    else -> {
                        trimmedT1ParamColl.add(param)
                    }
                }
            }
            var type: Type = types.first()
            types.forEach {
                val matches: Boolean
                type = it
                when (it.paramType) {
                    RI -> {
                        matches = if (trimmedT1ParamColl.size == 2) {
                            trimmedT1ParamColl[0] is T1Param.Register && trimmedT1ParamColl[1] is T1Param.Constant
                        } else {
                            false
                        }
                    }

                    LOAD -> {
                        matches = if (trimmedT1ParamColl.size == 2) {
                            trimmedT1ParamColl[0] is T1Param.Register &&
                                    (trimmedT1ParamColl[1] is T1Param.Offset || trimmedT1ParamColl[1] is T1Param.LabelLink)
                        } else {
                            false
                        }
                    }

                    STORE -> {
                        matches = if (trimmedT1ParamColl.size == 2) {
                            trimmedT1ParamColl[0] is T1Param.Register &&
                                    (trimmedT1ParamColl[1] is T1Param.Offset || trimmedT1ParamColl[1] is T1Param.LabelLink)
                        } else {
                            false
                        }
                    }

                    OP_R -> {
                        matches = if (trimmedT1ParamColl.size == 3) {
                            trimmedT1ParamColl[0] is T1Param.Register &&
                                    trimmedT1ParamColl[1] is T1Param.Register &&
                                    trimmedT1ParamColl[2] is T1Param.Register
                        } else {
                            false
                        }
                    }

                    LOGICCALCIMM -> {
                        matches = if (trimmedT1ParamColl.size == 3) {
                            trimmedT1ParamColl[0] is T1Param.Register &&
                                    trimmedT1ParamColl[1] is T1Param.Register &&
                                    trimmedT1ParamColl[2] is T1Param.Constant
                        } else {
                            false
                        }
                    }

                    SHIFTIMM -> {
                        matches = if (trimmedT1ParamColl.size == 3) {
                            trimmedT1ParamColl[0] is T1Param.Register &&
                                    trimmedT1ParamColl[1] is T1Param.Register &&
                                    trimmedT1ParamColl[2] is T1Param.Constant
                        } else {
                            false
                        }
                    }

                    BRANCH -> {
                        matches = if (trimmedT1ParamColl.size == 3) {
                            trimmedT1ParamColl[0] is T1Param.Register &&
                                    trimmedT1ParamColl[1] is T1Param.Register &&
                                    trimmedT1ParamColl[2] is T1Param.Constant
                        } else {
                            false
                        }
                    }

                    PS_BRANCHLBL -> {
                        matches = if (trimmedT1ParamColl.size == 3) {
                            trimmedT1ParamColl[0] is T1Param.Register &&
                                    trimmedT1ParamColl[1] is T1Param.Register &&
                                    trimmedT1ParamColl[2] is T1Param.LabelLink
                        } else {
                            false
                        }
                    }

                    JUMPLR -> {
                        matches = if (trimmedT1ParamColl.size == 2) {
                            trimmedT1ParamColl[0] is T1Param.Register &&
                                    trimmedT1ParamColl[1] is T1Param.Offset
                        } else {
                            false
                        }
                    }

                    NONE -> {
                        matches = trimmedT1ParamColl.size == 0
                    }

                    PS_RI -> matches = if (trimmedT1ParamColl.size == 2) {
                        trimmedT1ParamColl[0] is T1Param.Register && trimmedT1ParamColl[1] is T1Param.Constant
                    } else {
                        false
                    }

                    PS_RL -> matches = if (trimmedT1ParamColl.size == 2) {
                        trimmedT1ParamColl[0] is T1Param.Register && trimmedT1ParamColl[1] is T1Param.LabelLink
                    } else {
                        false
                    }

                    PS_L -> matches = if (trimmedT1ParamColl.size == 1) {
                        trimmedT1ParamColl[0] is T1Param.LabelLink
                    } else {
                        false
                    }

                    PS_RR -> matches = if (trimmedT1ParamColl.size == 2) {
                        trimmedT1ParamColl[0] is T1Param.Register && trimmedT1ParamColl[1] is T1Param.Register
                    } else {
                        false
                    }

                    PS_NONE -> matches = trimmedT1ParamColl.size == 0


                }
                if (matches) {
                    return Pair(matches, type)
                }
            }
            return Pair(false, type)
        }

        enum class ParameterType(val pseudo: Boolean, val exampleString: String) {
            // NORMAL INSTRUCTIONS
            RI(false, "rd, imm20"), // rd, imm
            LOAD(false, "rd, imm12(rs)"), // rd, imm12(rs)
            STORE(false, "rs2, imm12(rs1)"), // rs2, imm12(rs1)
            OP_R(false, "rd, rs1, rs2"), // rd, rs1, rs2
            LOGICCALCIMM(false, "rd, rs1, imm12"), // rd, rs, imm
            SHIFTIMM(false, "rd, rs1, shamt5"), // rd, rs, shamt
            BRANCH(false, "rs1, rs2, imm12"), // rs1, rs2, imm
            JUMPLR(false, "rd, imm12(rs1)"), // rd, label
            NONE(false, "none"),

            // PSEUDO INSTRUCTIONS
            PS_BRANCHLBL(true,"rs1, rs2, label"),
            PS_RI(true, "rd, imm32"), // rd, imm
            PS_RL(true, "rs, label"), // rs, label
            PS_L(true, "label"),  // label
            PS_RR(true, "rd, rs"), // rd, rs
            PS_NONE(true, "none")
        }

        enum class Type(val id: String, val pseudo: Boolean, val paramType: ParameterType, val opCode: OpCode? = null, val memWords: Int = 1) {
            LUI("LUI", false, RI, OpCode("00000000000000000000 00000 0110111", arrayOf(MaskLabel.IMM20, MaskLabel.RD, MaskLabel.OPCODE))),
            AUIPC("AUIPC", false, RI, OpCode("00000000000000000000 00000 0010111", arrayOf(MaskLabel.IMM20, MaskLabel.RD, MaskLabel.OPCODE))),
            JAL("JAL", false, RI, OpCode("00000000000000000000 00000 1101111", arrayOf(MaskLabel.IMM20, MaskLabel.RD, MaskLabel.OPCODE))),
            JALR("JALR", false, JUMPLR, OpCode("000000000000 00000 000 00000 1100111", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            ECALL("ECALL", false, NONE, OpCode("000000000000 00000 000 00000 1110011", arrayOf(MaskLabel.NONE, MaskLabel.NONE, MaskLabel.NONE, MaskLabel.NONE, MaskLabel.OPCODE))),
            EBREAK("EBREAK", false, NONE, OpCode("000000000001 00000 000 00000 1110011", arrayOf(MaskLabel.NONE, MaskLabel.NONE, MaskLabel.NONE, MaskLabel.NONE, MaskLabel.OPCODE))),
            BEQ("BEQ", false, BRANCH, OpCode("0000000 00000 00000 000 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))),
            BNE("BNE", false, BRANCH, OpCode("0000000 00000 00000 001 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))),
            BLT("BLT", false, BRANCH, OpCode("0000000 00000 00000 100 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))),
            BGE("BGE", false, BRANCH, OpCode("0000000 00000 00000 101 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))),
            BLTU("BLTU", false, BRANCH, OpCode("0000000 00000 00000 110 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))),
            BGEU("BGEU", false, BRANCH, OpCode("0000000 00000 00000 111 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))),
            BEQ1("BEQ", true, PS_BRANCHLBL),
            BNE1("BNE", true, PS_BRANCHLBL),
            BLT1("BLT", true, PS_BRANCHLBL),
            BGE1("BGE", true, PS_BRANCHLBL),
            BLTU1("BLTU", true, PS_BRANCHLBL),
            BGEU1("BGEU", true, PS_BRANCHLBL),
            LB("LB", false, LOAD, OpCode("000000000000 00000 000 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            LH("LH", false, LOAD, OpCode("000000000000 00000 001 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            LW("LW", false, LOAD, OpCode("000000000000 00000 010 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            LBU("LBU", false, LOAD, OpCode("000000000000 00000 100 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            LHU("LHU", false, LOAD, OpCode("000000000000 00000 101 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            SB("SB", false, STORE, OpCode("0000000 00000 00000 000 00000 0100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))),
            SH("SH", false, STORE, OpCode("0000000 00000 00000 001 00000 0100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))),
            SW("SW", false, STORE, OpCode("0000000 00000 00000 010 00000 0100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))),
            ADDI("ADDI", false, LOGICCALCIMM, OpCode("000000000000 00000 000 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            SLTI("SLTI", false, LOGICCALCIMM, OpCode("000000000000 00000 010 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            SLTIU("SLTIU", false, LOGICCALCIMM, OpCode("000000000000 00000 011 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            XORI("XORI", false, LOGICCALCIMM, OpCode("000000000000 00000 100 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            ORI("ORI", false, LOGICCALCIMM, OpCode("000000000000 00000 110 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            ANDI("ANDI", false, LOGICCALCIMM, OpCode("000000000000 00000 111 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            SLLI("SLLI", false, SHIFTIMM, OpCode("0000000 00000 00000 001 00000 0010011", arrayOf(MaskLabel.FUNCT7, MaskLabel.SHAMT, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            SRLI("SRLI", false, SHIFTIMM, OpCode("0000000 00000 00000 101 00000 0010011", arrayOf(MaskLabel.FUNCT7, MaskLabel.SHAMT, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            SRAI("SRAI", false, SHIFTIMM, OpCode("0100000 00000 00000 101 00000 0010011", arrayOf(MaskLabel.FUNCT7, MaskLabel.SHAMT, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            ADD("ADD", false, OP_R, OpCode("0000000 00000 00000 000 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            SUB("SUB", false, OP_R, OpCode("0100000 00000 00000 000 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            SLL("SLL", false, OP_R, OpCode("0000000 00000 00000 001 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            SLT("SLT", false, OP_R, OpCode("0000000 00000 00000 010 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            SLTU("SLTU", false, OP_R, OpCode("0000000 00000 00000 011 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            XOR("XOR", false, OP_R, OpCode("0000000 00000 00000 100 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            SRL("SRL", false, OP_R, OpCode("0000000 00000 00000 101 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            SRA("SRA", false, OP_R, OpCode("0100000 00000 00000 101 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            OR("OR", false, OP_R, OpCode("0000000 00000 00000 110 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),
            AND("AND", false, OP_R, OpCode("0000000 00000 00000 111 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))),

            Li("LI", true, PS_RI, memWords = 2),
            Beqz("BEQZ", true, PS_RL),
            Bltz("BLTZ", true, PS_RL),
            JAL1("JAL", true, PS_RL),
            JAL2("JAL", true, PS_L),
            J("J", true, PS_L),
            Ret("RET", true, PS_NONE),
            Mv("MV", true, PS_RR)
        }
    }

    class T1Comment(val prefix: Compiler.Token.Symbol, vararg val content: Compiler.Token) : TreeNode.TokenNode(RISCVFlags.comment, Syntax.NODE_COMMENT, prefix, *content) {

        val wholeContent: String

        init {
            wholeContent = content.joinToString("") { it.content }
        }
    }

    /* ---------- SYNTAX Tokens: Tier 2 ------------ */

    class T2CommentCollection(vararg val comments: T1Comment) : TreeNode.CollectionNode(Syntax.NODE2_COMMENTCOLLECTION, *comments)

    class T2TextSectionStart(val t1Directive: T1Directive) : TreeNode.CollectionNode(Syntax.NODE2_TEXTSECTIONSTART, t1Directive)

    class T2DataSectionStart(val t1Directive: T1Directive) : TreeNode.CollectionNode(Syntax.NODE2_DATASECTIONSTART, t1Directive)

    class T2LabelDef(val type: Type, val t1Label: T1Label, val t1Directive: T1Directive? = null, val t1Param: T1ParamColl? = null) : TreeNode.CollectionNode(if (type == Type.JUMP) Syntax.NODE2_LABELJUMP else Syntax.NODE2_LABELDEF, t1Label, t1Directive, *t1Param?.tokenNodes ?: emptyArray()) {

        enum class Type {
            JUMP,
            MEMALLOC,
        }
    }

    class T2InstrDef(val t1Instr: T1Instr, val t1ParamColl: T1ParamColl? = null, val type: T1Instr.Type) : TreeNode.CollectionNode(Syntax.NODE2_INSTRDEF, t1Instr, *t1ParamColl?.tokenNodes ?: emptyArray()) {

    }

    /* ---------- SYNTAX Tokens: Tier 3 ------------ */

    class T3DataSection(val dataDirective: T2DataSectionStart, vararg val collectionNodes: TreeNode.CollectionNode) : TreeNode.SectionNode(Syntax.NODE3_SECTION_DATA, dataDirective, *collectionNodes)

    class T3TextSection(val textDirective: T2TextSectionStart? = null, vararg val collectionNodes: TreeNode.CollectionNode) : TreeNode.SectionNode(Syntax.NODE3_SECTION_TEXT, collNodes = arrayOf(textDirective, *collectionNodes).filterNotNull().toTypedArray())

    /* ---------- SYNTAX Tokens: Tier 4 ------------ */

    class T4CodeRoot(allComments: T2CommentCollection, allErrors: List<Error>, vararg sectionNodes: TreeNode.SectionNode) : TreeNode.RootNode(Syntax.NODE4_ROOT, allComments, allErrors, *sectionNodes)


}