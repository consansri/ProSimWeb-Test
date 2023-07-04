package extendable.archs.riscv

import extendable.archs.riscv.RISCVGrammar.T1Directive.Type.*
import extendable.archs.riscv.RISCVGrammar.T1Instr.ParameterType.*
import extendable.archs.riscv.RISCVGrammar.T1Instr.Type.*
import extendable.archs.riscv.RISCVGrammar.T1PseudoInstr.Type.*
import extendable.components.assembly.Assembly
import extendable.components.assembly.Grammar
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
    var t1PseudoInstrs = mutableListOf<T1PseudoInstr>()
    var params = mutableListOf<T1ParamColl>()

    var t1Comments = mutableListOf<T1Comment>()
    var errors = mutableListOf<Grammar.Error>()

    override fun clear() {
        t1Labels.clear()
        t1Directives.clear()
        t1Instrs.clear()
        t1PseudoInstrs.clear()
        params.clear()
        t1Comments.clear()
    }

    override fun check(tokenLines: List<List<Assembly.Token>>): GrammarTree {

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
                    is Assembly.Token.Space -> {
                        continue
                    }

                    is Assembly.Token.Word -> {
                        for (type in T1PseudoInstr.Type.values()) {
                            if (type.name.uppercase() == token.content.uppercase()) {
                                val pseudoInstr = T1PseudoInstr(token, type)
                                remainingTokens.remove(token)
                                tier1Line.add(pseudoInstr)
                                t1PseudoInstrs.add(pseudoInstr)
                            }
                        }
                        for (type in T1Instr.Type.values()) {
                            if (type.name.uppercase() == token.content.uppercase()) {
                                val instr = T1Instr(token, type)
                                remainingTokens.remove(token)
                                tier1Line.add(instr)
                                t1Instrs.add(instr)
                            }
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
            var labelBuilder = mutableListOf<Assembly.Token>()

            while (remainingTokens.isNotEmpty()) {
                var firstToken = remainingTokens.first()

                if (firstToken is Assembly.Token.Space) {
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
                        val offset = T1Param.Offset(constant.token, lParan.token, reg.token, rParan.token)
                        parameterList.add(offset)
                        remainingTokens.removeAll(offset.tokens.toSet())
                        continue
                    }

                    if (firstToken is Assembly.Token.Register) {
                        parameterList.add(T1Param.Register(firstToken))
                        remainingTokens.remove(firstToken)
                        continue
                    }

                    if (firstToken is Assembly.Token.Constant) {
                        parameterList.add(T1Param.Constant(firstToken))
                        remainingTokens.remove(firstToken)
                        continue
                    }

                    var labelLink: T1Param.LabelLink? = null
                    var tokensForLabelToCheck = mutableListOf<Assembly.Token>()
                    for (possibleLabelToken in remainingTokens.dropWhile { firstToken != it }) {
                        if (possibleLabelToken is Assembly.Token.Space || (possibleLabelToken.content == ",")) {
                            break
                        } else {
                            tokensForLabelToCheck.add(possibleLabelToken)
                        }
                    }
                    if (tokensForLabelToCheck.isNotEmpty()) {
                        for (label in t1Labels) {
                            val labelResult = label.tokenSequence.exactlyMatches(*tokensForLabelToCheck.toTypedArray())
                            if (labelResult.matches) {
                                val labelNameTokens = mutableListOf<Assembly.Token>()
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
                    if (firstToken is Assembly.Token.Symbol && firstToken.content == ",") {
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
                    is Assembly.Token.Symbol -> {
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
                if (t1Instr.check()) {
                    tier2Node = T2InstrDef(t1Instr)
                    tier2Lines.add(tier2Node)
                    result.error?.let {
                        errors.add(it)
                    }
                    continue
                } else {
                    errors.add(Grammar.Error("Instruction Definition: parameters aren't matching instruction ${t1Instr.type.name}!\nExpecting: ${t1Instr.paramExampleString()}", *tier1Line.toTypedArray()))
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
                        if (t1Instr.check(t1ParamColl)) {
                            tier2Node = T2InstrDef(t1Instr, t1ParamColl)
                            tier2Lines.add(tier2Node)
                            result.error?.let {
                                errors.add(it)
                            }
                            continue
                        } else {
                            errors.add(Grammar.Error("Instruction Definition: parameters aren't matching instruction ${t1Instr.type.name}!\nExpecting: ${t1Instr.paramExampleString()}", *tier1Line.toTypedArray()))
                            continue
                        }
                    }
                }
            }

            result = Syntax.NODE2_PSEUDOINSTRDEF_SYNTAX.exacltyMatches(*tier1Line.toTypedArray())

            if (result.matches) {
                if (result.matchingTreeNodes.size > 0) {
                    val t1PseudoInstr = result.matchingTreeNodes[0] as T1PseudoInstr
                    if (t1PseudoInstr.check()) {
                        tier2Node = T2PseudoInstrDef(t1PseudoInstr)
                        tier2Lines.add(tier2Node)
                        result.error?.let {
                            errors.add(it)
                        }
                        continue
                    } else {
                        errors.add(Grammar.Error("Instruction Definition: parameters aren't matching instruction ${t1PseudoInstr.type.name}!\nExpecting: ${t1PseudoInstr.paramExampleString()}", *tier1Line.toTypedArray()))
                        continue
                    }
                }

            }

            result = Syntax.NODE2_PSEUDOINSTRDEF2_SYNTAX.exacltyMatches(*tier1Line.toTypedArray())

            if (result.matches) {
                if (result.matchingTreeNodes.size > 0) {
                    val t1PseudoInstr = result.matchingTreeNodes[0] as T1PseudoInstr
                    val t1ParamColl: T1ParamColl?
                    if (result.matchingTreeNodes.size == 2) {
                        t1ParamColl = result.matchingTreeNodes[1] as T1ParamColl
                    } else {
                        t1ParamColl = null
                    }
                    if (t1ParamColl != null) {
                        if (t1PseudoInstr.check(t1ParamColl)) {
                            tier2Node = T2PseudoInstrDef(t1PseudoInstr, t1ParamColl)
                            tier2Lines.add(tier2Node)
                            result.error?.let {
                                errors.add(it)
                            }
                            continue
                        } else {
                            errors.add(Grammar.Error("Instruction Definition: parameters aren't matching instruction ${t1PseudoInstr.type.name}!\nExpecting: ${t1PseudoInstr.paramExampleString()}", *tier1Line.toTypedArray()))
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

                is T2PseudoInstrDef -> {
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
        const val NODE_PSEUDOINSTR = "PseudoInstr"
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
        const val NODE2_PSEUDOINSTRDEF = "PseudoInstrDef"
        const val NODE2_TEXTSECTIONSTART = "TextSectionStart"
        const val NODE2_DATASECTIONSTART = "DataSectionStart"
        const val NODE2_COMMENTCOLLECTION = "AllComments"

        val NODE2_INSTRDEF_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE_INSTR))
        val NODE2_INSTRDEF2_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE_INSTR), SyntaxSequence.Component(NODE_PARAMCOLLECTION))
        val NODE2_PSEUDOINSTRDEF_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE_PSEUDOINSTR))
        val NODE2_PSEUDOINSTRDEF2_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE_PSEUDOINSTR), SyntaxSequence.Component(NODE_PARAMCOLLECTION))
        val NODE2_JUMPLABEL_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE_LABEL))
        val NODE2_ADDRESSALLOCLABEL_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE_LABEL), SyntaxSequence.Component(NODE_DIRECTIVE), SyntaxSequence.Component(NODE_PARAMCOLLECTION))
        val NODE2_SECTIONSTART_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE_DIRECTIVE))

        // Tier 3
        const val NODE3_SECTION_DATA = "Data Section"
        const val NODE3_SECTION_TEXT = "Text Section"

        val NODE3_SECTION_DATA_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE2_DATASECTIONSTART), SyntaxSequence.Component(NODE2_LABELDEF, repeatable = true))
        val NODE3_SECTION_TEXT_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE2_TEXTSECTIONSTART), SyntaxSequence.Component(NODE2_INSTRDEF, NODE2_PSEUDOINSTRDEF, NODE2_LABELJUMP, repeatable = true))
        val NODE3_SECTION_TEXT1_SYNTAX = SyntaxSequence(SyntaxSequence.Component(NODE2_INSTRDEF, NODE2_PSEUDOINSTRDEF, NODE2_LABELJUMP, repeatable = true))

        // Tier 4
        const val NODE4_ROOT = "root"
    }


    /* ---------- All SYNTAX Tokens which all implement a TreeNode ------------ */

    /* ---------- SYNTAX Tokens: Tier 1 ------------ */

    sealed class T1Param(hlFlag: String, val type: String, vararg val paramTokens: Assembly.Token) : TreeNode.TokenNode(hlFlag, type, *paramTokens) {

        class Offset(val offset: Assembly.Token, val openParan: Assembly.Token, val register: Assembly.Token, val closeParan: Assembly.Token) : T1Param(RISCVFlags.offset, RISCVGrammar.Syntax.NODE_PARAM_OFFSET, offset, openParan, register, closeParan) {


            object Syntax {
                val tokenSequence = TokenSequence(TokenSequence.SequenceComponent.InSpecific.Constant(), TokenSequence.SequenceComponent.Specific("("), TokenSequence.SequenceComponent.InSpecific.Register(), TokenSequence.SequenceComponent.Specific(")"), ignoreSpaces = true)
            }

        }

        class Constant(val constant: Assembly.Token.Constant) : T1Param("", Syntax.NODE_PARAM_CONST, constant) {

        }

        class Register(val register: Assembly.Token.Register) : T1Param("", Syntax.NODE_PARAM_REG, register) {

        }

        class SplitSymbol(val splitSymbol: Assembly.Token.Symbol) : T1Param(RISCVFlags.instruction, Syntax.NODE_PARAM_SPLIT, splitSymbol) {

        }

        class LabelLink(val linkedT1Label: T1Label, vararg val labelName: Assembly.Token) : T1Param(RISCVFlags.label, Syntax.NODE_PARAM_LABELLINK, *labelName) {

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

    }

    class T1Label(vararg val labelName: Assembly.Token, colon: Assembly.Token.Symbol) : TreeNode.TokenNode(RISCVFlags.label, Syntax.NODE_LABEL, *labelName, colon) {

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

    class T1Directive(val dot: Assembly.Token.Symbol, val type: T1Directive.Type, vararg tokens: Assembly.Token) : TreeNode.TokenNode(RISCVFlags.directive, Syntax.NODE_DIRECTIVE, dot, *tokens) {
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

    class T1Instr(val insToken: Assembly.Token.Word, val type: Type) : TreeNode.TokenNode(RISCVFlags.instruction, Syntax.NODE_INSTR, insToken) {

        val paramType: ParameterType

        init {
            paramType = when (type) {
                LUI -> RI
                AUIPC -> RI
                JAL -> JUMPLR
                JALR -> JUMPLR
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


        fun check(parameterCollection: T1ParamColl = T1ParamColl()): Boolean {
            val matches: Boolean
            val trimmedT1ParamColl = mutableListOf<T1Param>()
            for (param in parameterCollection.t1Params) {
                when (param) {
                    is T1Param.SplitSymbol -> {}
                    else -> {
                        trimmedT1ParamColl.add(param)
                    }
                }
            }

            when (paramType) {
                RI -> {
                    matches = if (trimmedT1ParamColl.size == 2) {
                        trimmedT1ParamColl[0] is T1Param.Register && trimmedT1ParamColl[1] is T1Param.Constant
                    } else {
                        false
                    }
                }

                LOADSAVE -> {
                    matches = if (trimmedT1ParamColl.size == 2) {
                        trimmedT1ParamColl[0] is T1Param.Register &&
                                (trimmedT1ParamColl[1] is T1Param.Offset || trimmedT1ParamColl[1] is T1Param.LabelLink)
                    } else {
                        false
                    }
                }

                LOGICANDCALC -> {
                    matches = if (trimmedT1ParamColl.size == 3) {
                        trimmedT1ParamColl[0] is T1Param.Register &&
                                trimmedT1ParamColl[1] is T1Param.Register &&
                                trimmedT1ParamColl[2] is T1Param.Register
                    } else {
                        false
                    }
                }

                LOGICANDCALCIMM -> {
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
                                (trimmedT1ParamColl[2] is T1Param.Constant || trimmedT1ParamColl[2] is T1Param.LabelLink)
                    } else {
                        false
                    }
                }

                JUMPLR -> {
                    matches = if (trimmedT1ParamColl.size == 2) {
                        trimmedT1ParamColl[0] is T1Param.Register &&
                                trimmedT1ParamColl[1] is T1Param.LabelLink
                    } else {
                        false
                    }

                }

                NONE -> {
                    return trimmedT1ParamColl.size == 0
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
            JUMPLR, // rd, label
            NONE,
        }

        fun paramExampleString(): String {
            return when (paramType) {
                RI -> "rd, imm"
                LOADSAVE -> "rd, imm(rs) or rs2, imm(rs1)"
                LOGICANDCALC -> "rd, rs1, rs2"
                LOGICANDCALCIMM -> "rd, rs, imm"
                BRANCH -> "rs1, rs2, imm"
                JUMPLR -> "rd, label"
                NONE -> "none"
            }
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

    class T1PseudoInstr(val insName: Assembly.Token.Word, val type: Type) : TreeNode.TokenNode(RISCVFlags.pseudoInstruction, Syntax.NODE_PSEUDOINSTR, insName) {

        val paramType: ParameterType

        init {
            paramType = when (type) {
                li -> ParameterType.RI
                beqz -> ParameterType.RL
                bltz -> ParameterType.RL
                j -> ParameterType.L
                ret -> ParameterType.NONE
                mv -> ParameterType.RR
            }
        }

        fun check(t1ParamColl: T1ParamColl = T1ParamColl()): Boolean {
            val matches: Boolean
            val trimmedT1ParamColl = mutableListOf<T1Param>()

            for (param in t1ParamColl.t1Params) {
                when (param) {
                    is T1Param.SplitSymbol -> {}
                    else -> {
                        trimmedT1ParamColl.add(param)
                    }
                }
            }

            matches = when (paramType) {
                ParameterType.RI -> if (trimmedT1ParamColl.size == 2) {
                    trimmedT1ParamColl[0] is T1Param.Register && trimmedT1ParamColl[1] is T1Param.Constant
                } else {
                    false
                }

                ParameterType.RL -> if (trimmedT1ParamColl.size == 2) {
                    trimmedT1ParamColl[0] is T1Param.Register && trimmedT1ParamColl[1] is T1Param.LabelLink
                } else {
                    false
                }

                ParameterType.L -> if (trimmedT1ParamColl.size == 1) {
                    trimmedT1ParamColl[0] is T1Param.LabelLink
                } else {
                    false
                }

                ParameterType.RR -> if (trimmedT1ParamColl.size == 2) {
                    trimmedT1ParamColl[0] is T1Param.Register && trimmedT1ParamColl[1] is T1Param.Register
                } else {
                    false
                }

                ParameterType.NONE -> trimmedT1ParamColl.size == 0
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

        fun paramExampleString(): String {
            return when (paramType) {
                ParameterType.RI -> "rd, imm"
                ParameterType.RL -> "rs, label"
                ParameterType.L -> "label"
                ParameterType.RR -> "rd, rs"
                ParameterType.NONE -> "none"
            }
        }

        object Types {
            val list = listOf(Type.li, Type.beqz, Type.j, Type.ret, Type.bltz, Type.mv)
        }
    }

    class T1Comment(val prefix: Assembly.Token.Symbol, vararg val content: Assembly.Token) : TreeNode.TokenNode(RISCVFlags.comment, Syntax.NODE_COMMENT, prefix, *content) {

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

    class T2InstrDef(val t1Instr: T1Instr, val t1ParamColl: T1ParamColl? = null) : TreeNode.CollectionNode(Syntax.NODE2_INSTRDEF, t1Instr, *t1ParamColl?.tokenNodes ?: emptyArray())

    class T2PseudoInstrDef(val t1PseudoInstr: T1PseudoInstr, val t1ParamColl: T1ParamColl? = null) : TreeNode.CollectionNode(Syntax.NODE2_PSEUDOINSTRDEF, t1PseudoInstr, *t1ParamColl?.tokenNodes ?: emptyArray())

    /* ---------- SYNTAX Tokens: Tier 3 ------------ */

    class T3DataSection(val dataDirective: T2DataSectionStart, vararg val collectionNodes: TreeNode.CollectionNode) : TreeNode.SectionNode(Syntax.NODE3_SECTION_DATA, dataDirective, *collectionNodes)

    class T3TextSection(val textDirective: T2TextSectionStart? = null, vararg val collectionNodes: TreeNode.CollectionNode) : TreeNode.SectionNode(Syntax.NODE3_SECTION_TEXT, collNodes = arrayOf(textDirective, *collectionNodes).filterNotNull().toTypedArray())

    /* ---------- SYNTAX Tokens: Tier 4 ------------ */

    class T4CodeRoot(allComments: T2CommentCollection, allErrors: List<Error>, vararg sectionNodes: TreeNode.SectionNode) : TreeNode.RootNode(Syntax.NODE4_ROOT, allComments, allErrors, *sectionNodes)




}