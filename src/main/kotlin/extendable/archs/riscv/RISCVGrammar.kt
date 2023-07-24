package extendable.archs.riscv

import extendable.Architecture
import extendable.archs.riscv.RISCVBinMapper.*
import extendable.archs.riscv.RISCVGrammar.T1Directive.Type.*
import extendable.archs.riscv.RISCVGrammar.T1Instr.ParameterType.*
import extendable.archs.riscv.RISCVGrammar.T1Instr.Type.*
import extendable.archs.riscv.RISCVGrammar.T2LabelDef.Type.*
import extendable.components.assembly.Compiler
import extendable.components.assembly.Grammar
import extendable.components.types.MutVal

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

    override fun check(compiler: Compiler, tokenLines: List<List<Compiler.Token>>, others: List<Compiler.OtherFile>): GrammarTree {

        errors.clear()

        val tier1Lines = mutableListOf<List<TreeNode>>()

        for (lineID in tokenLines.indices) {
            tier1Lines.add(lineID, emptyList())
        }

        val remainingLines = tokenLines.toMutableList()

        // -------------------------------------------------------------------------- TIER 1 PRE Elements (Labels) --------------------------------------------------------------------------
        for (lineID in remainingLines.indices) {
            val remainingTokens = remainingLines[lineID].toMutableList()
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

                            // check if sublabel
                            var sublabelFrom: T1Label? = null
                            if (labelNameString.first() == '.') {
                                val superLabelsToCheck = t1Labels.toMutableList()
                                while (superLabelsToCheck.isNotEmpty()) {
                                    // super label can't be sub label!
                                    if (!superLabelsToCheck.last().isSubLabel) {
                                        // check if super label line contains equ so equ constant labels can't be super labels
                                        val remainingLineElements = mutableListOf<Compiler.Token>()
                                        remainingLines.get(superLabelsToCheck.last().getAllTokens().first().lineLoc.lineID).forEach { if (it.content == "equ") remainingLineElements.add(it) }
                                        if (remainingLineElements.isEmpty()) {
                                            sublabelFrom = superLabelsToCheck.last()
                                            break
                                        } else {
                                            superLabelsToCheck.removeLast()
                                        }
                                    } else {
                                        superLabelsToCheck.removeLast()
                                    }
                                }
                            }

                            // check if label is already in use!
                            var alreadyDefined = false
                            for (label in t1Labels) {
                                if (label.wholeName == (if (sublabelFrom != null) sublabelFrom.wholeName else "") + labelNameString) {
                                    alreadyDefined = true
                                }
                            }
                            if (alreadyDefined) {
                                errors.add(Error("Mutliple labels with same name not possible!", *labelName.toTypedArray(), colon))
                                remainingTokens.removeAll(labelName)
                                remainingTokens.remove(colon)
                                break
                            }

                            // adding label
                            val t1Label = T1Label(*labelName.toTypedArray(), colon = colon, sublabelFrom = sublabelFrom)
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
        if (DebugTools.RISCV_showGrammarScanTiers) {
            console.log("Grammar: Tier 1 Pre Label Scan -> Labels: ${t1Labels.joinToString(", ") { it.wholeName }}")
        }


        // -------------------------------------------------------------------------- TIER 1 MAIN Scan --------------------------------------------------------------------------
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
                    DATA.name -> {
                        val t1Directive = T1Directive(dot, DATA, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        t1Directives.add(t1Directive)
                        tier1Line.add(t1Directive)
                    }

                    TEXT.name -> {
                        val t1Directive = T1Directive(dot, TEXT, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        t1Directives.add(t1Directive)
                        tier1Line.add(t1Directive)
                    }

                    BYTE.name -> {
                        val t1Directive = T1Directive(dot, BYTE, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        t1Directives.add(t1Directive)
                        tier1Line.add(t1Directive)
                    }

                    HALF.name -> {
                        val t1Directive = T1Directive(dot, HALF, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        t1Directives.add(t1Directive)
                        tier1Line.add(t1Directive)
                    }

                    WORD.name -> {
                        val t1Directive = T1Directive(dot, WORD, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        t1Directives.add(t1Directive)
                        tier1Line.add(t1Directive)
                    }

                    DWORD.name -> {
                        val t1Directive = T1Directive(dot, DWORD, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        t1Directives.add(t1Directive)
                        tier1Line.add(t1Directive)
                    }

                    ASCIZ.name -> {
                        val t1Directive = T1Directive(dot, ASCIZ, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        t1Directives.add(t1Directive)
                        tier1Line.add(t1Directive)
                    }

                    STRING.name -> {
                        val t1Directive = T1Directive(dot, STRING, *directiveName.toTypedArray())
                        remainingTokens.removeAll(directiveName)
                        remainingTokens.remove(dot)
                        t1Directives.add(t1Directive)
                        tier1Line.add(t1Directive)
                    }

                    EQU.name -> {
                        val t1Directive = T1Directive(dot, EQU, *directiveName.toTypedArray())
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
                    val tokensForLabelToCheck = mutableListOf<Compiler.Token>()
                    for (possibleLabelToken in remainingTokens.dropWhile { firstToken != it }) {
                        if (possibleLabelToken is Compiler.Token.Space || (possibleLabelToken.content == ",")) {
                            break
                        } else {
                            tokensForLabelToCheck.add(possibleLabelToken)
                        }
                    }
                    if (tokensForLabelToCheck.isNotEmpty()) {
                        for (label in t1Labels) {

                            if (label.isSubLabel) {
                                // check sublabel names
                                val wholeLinkName = tokensForLabelToCheck.joinToString("") { it.content }
                                if (label.wholeName == wholeLinkName) {
                                    val labelNameTokens = mutableListOf<Compiler.Token>()
                                    labelNameTokens.addAll(tokensForLabelToCheck)
                                    labelLink = T1Param.LabelLink(label, *labelNameTokens.toTypedArray())
                                    break
                                }
                            }

                            // check not sublabelnames
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

        if (DebugTools.RISCV_showGrammarScanTiers) {
            console.log("Grammar: Tier 1 Main Scan -> ${tier1Lines.joinToString(", line: ") { it.joinToString(" ") { it.name } }}")
        }

        // -------------------------------------------------------------------------- TIER 2 MAIN Scan (check Syntax) | Ignore Comments --------------------------------------------------------------------------
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
                        tier2Node = T2LabelDef(MEMALLOC, t1Label, t1Directive, t1ParamColl)
                        tier2Lines.add(tier2Node)
                    } else if (t1ParamColl.paramsWithOutSplitSymbols.size == 1 && t1Directive.isConstantDirective() && t1ParamColl.paramsWithOutSplitSymbols.first() is T1Param.Constant) {
                        tier2Node = T2LabelDef(CONSTANT, t1Label, t1Directive, t1ParamColl)
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
                    tier2Node = T2LabelDef(JUMP, t1Label)
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
                        DATA -> {
                            tier2Node = T2DataSectionStart(t1Directive)
                            tier2Lines.add(tier2Node)
                            result.error?.let {
                                errors.add(it)
                            }
                            continue
                        }

                        TEXT -> {
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

        if (DebugTools.RISCV_showGrammarScanTiers) {
            console.log("Grammar: Tier 2 Main Scan -> ${tier2Lines.filterNotNull().joinToString { it.name }}")
        }

        // -------------------------------------------------------------------------- Tier 2.5 ReCheck Label Types --------------------------------------------------------------------------
        val recheckedT2Lines = mutableListOf<TreeNode>()
        val notNullT2lines = tier2Lines.filterNotNull().toMutableList()
        val jumpLabels = mutableListOf<T1Label>()
        tier2Lines.filterNotNull().forEach {
            if (it is T2LabelDef && it.type == JUMP) {
                jumpLabels.add(it.t1Label)
            }
        }
        val memAllocLabels = mutableListOf<T1Label>()
        tier2Lines.filterNotNull().forEach {
            if (it is T2LabelDef && it.type == MEMALLOC) {
                memAllocLabels.add(it.t1Label)
            }
        }
        val constantLabels = mutableListOf<T1Label>()
        tier2Lines.filterNotNull().forEach {
            if (it is T2LabelDef && it.type == CONSTANT) {
                constantLabels.add(it.t1Label)
            }
        }

        for (entry in notNullT2lines) {
            when (entry) {
                is T2InstrDef -> {
                    val labelsToCheck = mutableListOf<T1Label>()
                    entry.t1ParamColl?.t1Params?.forEach {
                        if (it is T1Param.LabelLink) {
                            labelsToCheck.add(it.linkedT1Label)
                        }
                    }
                    when (entry.type.paramType.expectedLabelType) {
                        JUMP -> {
                            if (jumpLabels.containsAll(labelsToCheck)) {
                                recheckedT2Lines.add(entry)
                            } else {
                                errors.add(Error("[${labelsToCheck.joinToString { it.wholeName }}] not a jump label!", entry))
                            }
                        }

                        MEMALLOC -> {
                            if (memAllocLabels.containsAll(labelsToCheck)) {
                                recheckedT2Lines.add(entry)
                            } else {
                                errors.add(Error("[${labelsToCheck.joinToString { it.wholeName }}] not a mem alloc label!", entry))
                            }
                        }

                        CONSTANT -> {
                            if (constantLabels.containsAll(labelsToCheck)) {
                                recheckedT2Lines.add(entry)
                            } else {
                                errors.add(Grammar.Error("[${labelsToCheck.joinToString { it.wholeName }}] not a equ label!", entry))
                            }
                        }

                        null -> {
                            recheckedT2Lines.add(entry)
                        }
                    }
                }

                else -> {
                    recheckedT2Lines.add(entry)
                }
            }
        }

        if (DebugTools.RISCV_showGrammarScanTiers) {
            console.log("Grammar: Tier 2.5 ReCheck Label Types -> ${recheckedT2Lines.filterNotNull().joinToString { it.name }}")
        }

        // -------------------------------------------------------------------------- TIER 3 MAIN Scan (apply Sections) | Ignore Comments --------------------------------------------------------------------------
        val sections = mutableListOf<TreeNode.SectionNode>()

        var isTextSection = true
        var sectionIdentification: TreeNode.RowNode? = null
        var sectionContent = mutableListOf<TreeNode.RowNode>()

        while (recheckedT2Lines.isNotEmpty()) {
            val firstT2Line = recheckedT2Lines.first()

            when (firstT2Line) {
                is T2DataSectionStart -> {
                    if (isTextSection) {
                        if (sectionIdentification != null) {
                            sections.add(T3TextSection(sectionIdentification as T2TextSectionStart, *sectionContent.toTypedArray()))
                        } else {
                            sections.add(T3TextSection(rowNodes = sectionContent.toTypedArray()))
                        }
                        sectionContent.clear()

                    } else {
                        sections.add(T3DataSection(sectionIdentification as T2DataSectionStart, *sectionContent.toTypedArray()))
                        sectionContent.clear()
                    }
                    isTextSection = false
                    sectionIdentification = firstT2Line
                    recheckedT2Lines.removeFirst()
                    continue
                }

                is T2TextSectionStart -> {
                    if (isTextSection) {
                        if (sectionIdentification != null) {
                            sections.add(T3TextSection(sectionIdentification as T2TextSectionStart, *sectionContent.toTypedArray()))
                        } else {
                            sections.add(T3TextSection(rowNodes = sectionContent.toTypedArray()))
                        }

                        sectionContent.clear()

                    } else {
                        sections.add(T3DataSection(sectionIdentification as T2DataSectionStart, *sectionContent.toTypedArray()))
                        sectionContent.clear()
                    }
                    isTextSection = true
                    sectionIdentification = firstT2Line
                    recheckedT2Lines.removeFirst()
                    continue
                }

                is T2InstrDef -> {
                    if (isTextSection) {
                        sectionContent.add(firstT2Line)
                        recheckedT2Lines.removeFirst()
                        continue
                    }
                }

                is T2LabelDef -> {
                    if (isTextSection) {
                        if (firstT2Line.type == JUMP) {
                            sectionContent.add(firstT2Line)
                            recheckedT2Lines.removeFirst()
                            continue
                        }
                        if (firstT2Line.type == CONSTANT) {
                            sectionContent.add(firstT2Line)
                            recheckedT2Lines.removeFirst()
                            continue
                        }
                    } else {
                        if (firstT2Line.type == MEMALLOC) {
                            sectionContent.add(firstT2Line)
                            recheckedT2Lines.removeFirst()
                            continue
                        }
                    }
                }

                else -> {}

            }

            errors.add(Grammar.Error(message = "Element not possible in ${if (isTextSection) "text section" else "data section"}!", linkedTreeNode = firstT2Line))
            recheckedT2Lines.removeFirst()
        }

        if (isTextSection) {
            if (sectionIdentification != null) {
                sections.add(T3TextSection(sectionIdentification as T2TextSectionStart, rowNodes = sectionContent.toTypedArray()))
            } else {
                sections.add(T3TextSection(rowNodes = sectionContent.toTypedArray()))
            }
            sectionContent.clear()

        } else {
            if (sectionIdentification != null) {
                sections.add(T3DataSection(sectionIdentification as T2DataSectionStart, rowNodes = sectionContent.toTypedArray()))
                sectionContent.clear()
            } else {
                errors.add(Grammar.Error(message = "No Valid Data Section Identification found!", sectionContent.first()))
            }
        }
        if (DebugTools.RISCV_showGrammarScanTiers) {
            console.log("Grammar: Tier 3 Main Scan -> ${sections.joinToString { it.name }}")
        }

        // -------------------------------------------------------------------------- Build Comment Node --------------------------------------------------------------------------
        val commentNode = T2CommentRow(*t1Comments.toTypedArray())
        // -------------------------------------------------------------------------- Build CodeRoot Node --------------------------------------------------------------------------
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

        // Tier 4
        const val NODE4_ROOT = "root"
    }


    /* ---------- All SYNTAX Tokens which all implement a TreeNode ------------ */

    /* ---------- SYNTAX Tokens: Tier 1 ------------ */

    sealed class T1Param(hlFlag: String, val type: String, vararg val paramTokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(hlFlag), type, *paramTokens) {

        class Offset(val offset: Compiler.Token.Constant, val openParan: Compiler.Token, val register: Compiler.Token.Register, val closeParan: Compiler.Token) : T1Param(RISCVFlags.offset, RISCVGrammar.Syntax.NODE_PARAM_OFFSET, offset, openParan, register, closeParan) {

            object Syntax {
                val tokenSequence = TokenSequence(TokenSequence.SequenceComponent.InSpecific.Constant(), TokenSequence.SequenceComponent.Specific("("), TokenSequence.SequenceComponent.InSpecific.Register(), TokenSequence.SequenceComponent.Specific(")"), ignoreSpaces = true)
            }

            fun getValueArray(): Array<MutVal.Value.Binary> {
                return arrayOf(offset.getValue().toBin(), register.reg.address.toBin())
            }
        }

        class Constant(val constant: Compiler.Token.Constant) : T1Param("", Syntax.NODE_PARAM_CONST, constant) {
            fun getValue(): MutVal.Value.Binary {
                return constant.getValue().toBin()
            }
        }

        class Register(val register: Compiler.Token.Register) : T1Param("", Syntax.NODE_PARAM_REG, register) {
            fun getAddress(): MutVal.Value.Binary {
                return register.reg.address.toBin()
            }
        }

        class SplitSymbol(val splitSymbol: Compiler.Token.Symbol) : T1Param(RISCVFlags.instruction, Syntax.NODE_PARAM_SPLIT, splitSymbol) {

        }

        class LabelLink(val linkedT1Label: T1Label, vararg val labelName: Compiler.Token) : T1Param(RISCVFlags.label, Syntax.NODE_PARAM_LABELLINK, *labelName) {

        }

    }

    class T1ParamColl(vararg val t1Params: T1Param) : TreeNode.RowNode(Syntax.NODE_PARAMCOLLECTION, *t1Params) {

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

        fun getValues(): Array<MutVal.Value.Binary> {
            val values = mutableListOf<MutVal.Value.Binary>()
            paramsWithOutSplitSymbols.forEach {
                when (it) {
                    is T1Param.Constant -> {
                        var value = it.getValue()
                        if (value.size.byteCount < MutVal.Size.Bit32().byteCount) {
                            value = when (it.constant) {
                                is Compiler.Token.Constant.Dec -> {
                                    value.getResized(MutVal.Size.Bit32())
                                }

                                else -> {
                                    value.getUResized(MutVal.Size.Bit32())
                                }
                            }
                        }
                        values.add(value)
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
                when (it) {
                    is T1Param.LabelLink -> {
                        labels.add(it.linkedT1Label)
                    }

                    else -> {}
                }
            }
            return labels.toTypedArray()
        }

    }

    class T1Label(vararg val labelName: Compiler.Token, colon: Compiler.Token.Symbol, val sublabelFrom: T1Label? = null) : TreeNode.ElementNode(ConnectedHL(RISCVFlags.label), Syntax.NODE_LABEL, *labelName, colon) {

        val wholeName: String
        val tokenSequence: TokenSequence
        val isSubLabel: Boolean

        init {
            if (sublabelFrom != null) {
                isSubLabel = true
            } else {
                isSubLabel = false
            }

            wholeName = (if (sublabelFrom != null) sublabelFrom.wholeName else "") + labelName.joinToString("") { it.content }
            val tokenSequenceComponents = mutableListOf<TokenSequence.SequenceComponent>()
            for (token in labelName) {
                tokenSequenceComponents.add(TokenSequence.SequenceComponent.Specific(token.content))
            }
            tokenSequence = TokenSequence(*tokenSequenceComponents.toTypedArray(), ignoreSpaces = false)
        }
    }

    class T1Directive(val dot: Compiler.Token.Symbol, val type: T1Directive.Type, vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RISCVFlags.directive), Syntax.NODE_DIRECTIVE, dot, *tokens) {

        fun isTypeDirective(): Boolean {
            when (type) {
                BYTE, HALF, WORD, DWORD, ASCIZ, STRING -> {
                    return true
                }

                else -> {
                    return false
                }
            }
        }

        fun isConstantDirective(): Boolean {
            return type == EQU
        }

        enum class DirParam {
            NONE,
            VALUE,
        }

        enum class Type(val dirParam: DirParam) {
            // SECTIONS
            DATA(DirParam.NONE),
            TEXT(DirParam.NONE),

            // VALUE TYPES
            BYTE(DirParam.VALUE),
            HALF(DirParam.VALUE),
            WORD(DirParam.VALUE),
            DWORD(DirParam.VALUE),
            ASCIZ(DirParam.VALUE),
            STRING(DirParam.VALUE),

            //
            EQU(DirParam.VALUE),
        }

    }

    class T1Instr(val insToken: Compiler.Token.Word, vararg val types: Type) : TreeNode.ElementNode(ConnectedHL(RISCVFlags.instruction), Syntax.NODE_INSTR, insToken) {

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
                    RD_I20 -> {
                        matches = if (trimmedT1ParamColl.size == 2) {
                            trimmedT1ParamColl[0] is T1Param.Register && trimmedT1ParamColl[1] is T1Param.Constant
                        } else {
                            false
                        }
                    }

                    PS_RD_Const20 -> {
                        matches = if (trimmedT1ParamColl.size == 2) {
                            trimmedT1ParamColl[0] is T1Param.Register && trimmedT1ParamColl[1] is T1Param.LabelLink
                        } else {
                            false
                        }
                    }

                    RD_Off12 -> {
                        matches = if (trimmedT1ParamColl.size == 2) {
                            trimmedT1ParamColl[0] is T1Param.Register &&
                                    (trimmedT1ParamColl[1] is T1Param.Offset || trimmedT1ParamColl[1] is T1Param.LabelLink)
                        } else {
                            false
                        }
                    }

                    RS2_Off5 -> {
                        matches = if (trimmedT1ParamColl.size == 2) {
                            trimmedT1ParamColl[0] is T1Param.Register &&
                                    (trimmedT1ParamColl[1] is T1Param.Offset || trimmedT1ParamColl[1] is T1Param.LabelLink)
                        } else {
                            false
                        }
                    }

                    RD_RS1_RS1 -> {
                        matches = if (trimmedT1ParamColl.size == 3) {
                            trimmedT1ParamColl[0] is T1Param.Register &&
                                    trimmedT1ParamColl[1] is T1Param.Register &&
                                    trimmedT1ParamColl[2] is T1Param.Register
                        } else {
                            false
                        }
                    }

                    RD_RS1_I12 -> {
                        matches = if (trimmedT1ParamColl.size == 3) {
                            trimmedT1ParamColl[0] is T1Param.Register &&
                                    trimmedT1ParamColl[1] is T1Param.Register &&
                                    trimmedT1ParamColl[2] is T1Param.Constant
                        } else {
                            false
                        }
                    }

                    PS_RD_RS1_Const12 -> {
                        matches = if (trimmedT1ParamColl.size == 3) {
                            trimmedT1ParamColl[0] is T1Param.Register &&
                                    trimmedT1ParamColl[1] is T1Param.Register &&
                                    trimmedT1ParamColl[2] is T1Param.LabelLink
                        } else {
                            false
                        }
                    }

                    RD_RS1_I5 -> {
                        matches = if (trimmedT1ParamColl.size == 3) {
                            trimmedT1ParamColl[0] is T1Param.Register &&
                                    trimmedT1ParamColl[1] is T1Param.Register &&
                                    trimmedT1ParamColl[2] is T1Param.Constant
                        } else {
                            false
                        }
                    }

                    PS_RD_RS1_Const5 -> {
                        matches = if (trimmedT1ParamColl.size == 3) {
                            trimmedT1ParamColl[0] is T1Param.Register &&
                                    trimmedT1ParamColl[1] is T1Param.Register &&
                                    trimmedT1ParamColl[2] is T1Param.LabelLink
                        } else {
                            false
                        }
                    }

                    RS1_RS2_I12 -> {
                        matches = if (trimmedT1ParamColl.size == 3) {
                            trimmedT1ParamColl[0] is T1Param.Register &&
                                    trimmedT1ParamColl[1] is T1Param.Register &&
                                    trimmedT1ParamColl[2] is T1Param.Constant
                        } else {
                            false
                        }
                    }

                    PS_RS1_RS2_Const12 -> {
                        matches = if (trimmedT1ParamColl.size == 3) {
                            trimmedT1ParamColl[0] is T1Param.Register &&
                                    trimmedT1ParamColl[1] is T1Param.Register &&
                                    trimmedT1ParamColl[2] is T1Param.LabelLink
                        } else {
                            false
                        }
                    }

                    PS_RS1_RS2_Jlbl -> {
                        matches = if (trimmedT1ParamColl.size == 3) {
                            trimmedT1ParamColl[0] is T1Param.Register &&
                                    trimmedT1ParamColl[1] is T1Param.Register &&
                                    trimmedT1ParamColl[2] is T1Param.LabelLink
                        } else {
                            false
                        }
                    }


                    PS_RD_I32 -> matches = if (trimmedT1ParamColl.size == 2) {
                        trimmedT1ParamColl[0] is T1Param.Register && trimmedT1ParamColl[1] is T1Param.Constant
                    } else {
                        false
                    }

                    PS_RD_Clbl -> {
                        matches = if (trimmedT1ParamColl.size == 2) {
                            trimmedT1ParamColl[0] is T1Param.Register && trimmedT1ParamColl[1] is T1Param.LabelLink
                        } else {
                            false
                        }
                    }

                    PS_RS1_Jlbl -> matches = if (trimmedT1ParamColl.size == 2) {
                        trimmedT1ParamColl[0] is T1Param.Register && trimmedT1ParamColl[1] is T1Param.LabelLink
                    } else {
                        false
                    }

                    PS_RD_Albl -> matches = if (trimmedT1ParamColl.size == 2) {
                        trimmedT1ParamColl[0] is T1Param.Register && trimmedT1ParamColl[1] is T1Param.LabelLink
                    } else {
                        false
                    }

                    PS_Jlbl -> matches = if (trimmedT1ParamColl.size == 1) {
                        trimmedT1ParamColl[0] is T1Param.LabelLink
                    } else {
                        false
                    }

                    PS_RD_RS1 -> matches = if (trimmedT1ParamColl.size == 2) {
                        trimmedT1ParamColl[0] is T1Param.Register && trimmedT1ParamColl[1] is T1Param.Register
                    } else {
                        false
                    }

                    PS_RS1 -> matches = if (trimmedT1ParamColl.size == 1) {
                        trimmedT1ParamColl[0] is T1Param.Register
                    } else {
                        false
                    }


                    PS_NONE -> {
                        matches = trimmedT1ParamColl.size == 0
                    }

                    NONE -> {
                        matches = trimmedT1ParamColl.size == 0
                    }


                }
                if (matches) {
                    return Pair(matches, type)
                }
            }
            return Pair(false, type)
        }

        enum class ParameterType(val pseudo: Boolean, val exampleString: String, val expectedLabelType: T2LabelDef.Type? = null) {
            // NORMAL INSTRUCTIONS
            RD_I20(false, "rd, imm20"), // rd, imm
            RD_Off12(false, "rd, imm12(rs)"), // rd, imm12(rs)
            RS2_Off5(false, "rs2, imm5(rs1)"), // rs2, imm5(rs1)
            RD_RS1_RS1(false, "rd, rs1, rs2"), // rd, rs1, rs2
            RD_RS1_I12(false, "rd, rs1, imm12"), // rd, rs, imm
            RD_RS1_I5(false, "rd, rs1, shamt5"), // rd, rs, shamt
            RS1_RS2_I12(false, "rs1, rs2, imm12"), // rs1, rs2, imm

            // PSEUDO INSTRUCTIONS
            PS_RD_Const20(true, "rd, const20", CONSTANT), // rd, constant
            PS_RD_RS1_Const12(true, "rd, rs1, const12", CONSTANT), // rd, rs, constant
            PS_RD_RS1_Const5(true, "rd, rs1, const5", CONSTANT), //rd, rs1, constant5
            PS_RS1_RS2_Const12(true, "rs1, rs2, const", CONSTANT), // rs1, rs2, constant
            PS_RS1_RS2_Jlbl(true, "rs1, rs2, jlabel", JUMP),
            PS_RD_I32(true, "rd, imm32"), // rd, imm
            PS_RD_Clbl(true, "rd, clabel", CONSTANT), // rd, imm
            PS_RS1_Jlbl(true, "rs, jlabel", JUMP), // rs, label
            PS_RD_Albl(true, "rd, alabel", MEMALLOC), // rd, label
            PS_Jlbl(true, "jlabel", JUMP),  // label
            PS_RD_RS1(true, "rd, rs"), // rd, rs
            PS_RS1(true, "rs1"),

            // NONE PARAM INSTR
            NONE(false, "none"),
            PS_NONE(true, "none")
        }

        enum class Type(val id: String, val pseudo: Boolean, val paramType: ParameterType, val opCode: OpCode? = null, val memWords: Int = 1, val relative: Type? = null) {
            LUI("LUI", false, RD_I20, OpCode("00000000000000000000 00000 0110111", arrayOf(MaskLabel.IMM20, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    if (rdAddr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val imm20 = paramMap.get(MaskLabel.IMM20)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && imm20 != null) {
                            val shiftedIMM32 = imm20.getUResized(MutVal.Size.Bit32()) shl 12
                            rd.set(shiftedIMM32)
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            LUI_Const("LUI", true, PS_RD_Const20, relative = LUI),
            AUIPC("AUIPC", false, RD_I20, OpCode("00000000000000000000 00000 0010111", arrayOf(MaskLabel.IMM20, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    if (rdAddr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val imm20 = paramMap.get(MaskLabel.IMM20)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && imm20 != null) {
                            val shiftedIMM32 = imm20.getUResized(MutVal.Size.Bit32()) shl 12
                            val sum = pc.value.get() + shiftedIMM32
                            rd.set(sum)
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            AUIPC_Const("AUIPC", true, PS_RD_Const20, relative = AUIPC),
            JAL("JAL", false, RD_I20, OpCode("00000000000000000000 00000 1101111", arrayOf(MaskLabel.IMM20, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    if (rdAddr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val imm20 = paramMap.get(MaskLabel.IMM20)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && imm20 != null) {
                            val imm20str = imm20.getRawBinaryStr()

                            /**
                             *      RV32IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                             *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                             *        Location       20 [      10 : 1               ] 11 [ 19 : 12             ]
                             */

                            val shiftedImm = MutVal.Value.Binary(imm20str[0].toString() + imm20str.substring(12) + imm20str[11] + imm20str.substring(1, 11), MutVal.Size.Bit20()).getResized(MutVal.Size.Bit32()) shl 1

                            rd.set(pc.value.get() + MutVal.Value.Hex("4"))
                            pc.value.set(pc.value.get() + shiftedImm)
                        }
                    }
                }
            },
            JAL_Const("JAL", true, PS_RD_Const20, relative = JAL),
            JALR("JALR", false, RD_Off12, OpCode("000000000000 00000 000 00000 1100111", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm12 = paramMap.get(MaskLabel.IMM12)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val jumpAddr = rs1.get() + imm12.getResized(MutVal.Size.Bit32())
                            rd.set(pc.value.get() + MutVal.Value.Hex("4"))
                            pc.value.set(jumpAddr)
                        }
                    }
                }
            },
            ECALL("ECALL", false, NONE, OpCode("000000000000 00000 000 00000 1110011", arrayOf(MaskLabel.NONE, MaskLabel.NONE, MaskLabel.NONE, MaskLabel.NONE, MaskLabel.OPCODE))),
            EBREAK("EBREAK", false, NONE, OpCode("000000000001 00000 000 00000 1110011", arrayOf(MaskLabel.NONE, MaskLabel.NONE, MaskLabel.NONE, MaskLabel.NONE, MaskLabel.OPCODE))),
            BEQ("BEQ", false, RS1_RS2_I12, OpCode("0000000 00000 00000 000 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val rs2Addr = paramMap.get(MaskLabel.RS2)
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm7 = paramMap.get(MaskLabel.IMM7)
                        val imm5 = paramMap.get(MaskLabel.IMM5)
                        val pc = architecture.getRegisterContainer().pc
                        if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                            val imm7str = imm7.getResized(MutVal.Size.Bit7()).getRawBinaryStr()
                            val imm5str = imm5.getResized(MutVal.Size.Bit5()).getRawBinaryStr()
                            val imm12 = MutVal.Value.Binary(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), MutVal.Size.Bit12())

                            console.warn("$imm7str $imm5str -> ${imm12.getRawBinaryStr()}")

                            val offset = imm12.toBin().getResized(MutVal.Size.Bit32()) shl 1
                            if (rs1.get().toDec() == rs2.get().toDec()) {
                                pc.value.set(pc.value.get() + offset)
                            } else {
                                pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                            }
                        }
                    }
                }
            },
            BEQ_Const("BEQC", true, PS_RS1_RS2_Const12, relative = BEQ),
            BNE("BNE", false, RS1_RS2_I12, OpCode("0000000 00000 00000 001 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val rs2Addr = paramMap.get(MaskLabel.RS2)
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm7 = paramMap.get(MaskLabel.IMM7)
                        val imm5 = paramMap.get(MaskLabel.IMM5)
                        val pc = architecture.getRegisterContainer().pc
                        if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                            val imm7str = imm7.getResized(MutVal.Size.Bit7()).getRawBinaryStr()
                            val imm5str = imm5.getResized(MutVal.Size.Bit5()).getRawBinaryStr()
                            val imm12 = MutVal.Value.Binary(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), MutVal.Size.Bit12())
                            val offset = imm12.toBin().getResized(MutVal.Size.Bit32()) shl 1
                            if (rs1.get().toDec() != rs2.get().toDec()) {
                                pc.value.set(pc.value.get() + offset)
                            } else {
                                pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                            }
                        }
                    }
                }
            },
            BNE_Const("BNEC", true, PS_RS1_RS2_Const12, relative = BNE),
            BLT("BLT", false, RS1_RS2_I12, OpCode("0000000 00000 00000 100 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val rs2Addr = paramMap.get(MaskLabel.RS2)
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm7 = paramMap.get(MaskLabel.IMM7)
                        val imm5 = paramMap.get(MaskLabel.IMM5)
                        val pc = architecture.getRegisterContainer().pc
                        if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                            val imm7str = imm7.getResized(MutVal.Size.Bit7()).getRawBinaryStr()
                            val imm5str = imm5.getResized(MutVal.Size.Bit5()).getRawBinaryStr()
                            val imm12 = MutVal.Value.Binary(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), MutVal.Size.Bit12())
                            val offset = imm12.toBin().getResized(MutVal.Size.Bit32()) shl 1
                            if (rs1.get().toDec() < rs2.get().toDec()) {
                                pc.value.set(pc.value.get() + offset)
                            } else {
                                pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                            }
                        }
                    }
                }
            },
            BLT_Const("BLTC", true, PS_RS1_RS2_Const12, relative = BLT),
            BGE("BGE", false, RS1_RS2_I12, OpCode("0000000 00000 00000 101 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val rs2Addr = paramMap.get(MaskLabel.RS2)
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm7 = paramMap.get(MaskLabel.IMM7)
                        val imm5 = paramMap.get(MaskLabel.IMM5)
                        val pc = architecture.getRegisterContainer().pc
                        if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                            val imm7str = imm7.getResized(MutVal.Size.Bit7()).getRawBinaryStr()
                            val imm5str = imm5.getResized(MutVal.Size.Bit5()).getRawBinaryStr()
                            val imm12 = MutVal.Value.Binary(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), MutVal.Size.Bit12())
                            val offset = imm12.toBin().getResized(MutVal.Size.Bit32()) shl 1
                            if (rs1.get().toDec() >= rs2.get().toDec()) {
                                pc.value.set(pc.value.get() + offset)
                            } else {
                                pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                            }
                        }
                    }
                }
            },
            BGE_Const("BGEC", true, PS_RS1_RS2_Const12, relative = BGE),
            BLTU("BLTU", false, RS1_RS2_I12, OpCode("0000000 00000 00000 110 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val rs2Addr = paramMap.get(MaskLabel.RS2)
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm7 = paramMap.get(MaskLabel.IMM7)
                        val imm5 = paramMap.get(MaskLabel.IMM5)
                        val pc = architecture.getRegisterContainer().pc
                        if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                            val imm7str = imm7.getResized(MutVal.Size.Bit7()).getRawBinaryStr()
                            val imm5str = imm5.getResized(MutVal.Size.Bit5()).getRawBinaryStr()
                            val imm12 = MutVal.Value.Binary(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), MutVal.Size.Bit12())
                            val offset = imm12.toBin().getResized(MutVal.Size.Bit32()) shl 1
                            if (rs1.get().toUDec() < rs2.get().toUDec()) {
                                pc.value.set(pc.value.get() + offset)
                            } else {
                                pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                            }
                        }
                    }
                }
            },
            BLTU_Const("BLTUC", true, PS_RS1_RS2_Const12, relative = BLTU),
            BGEU("BGEU", false, RS1_RS2_I12, OpCode("0000000 00000 00000 111 00000 1100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val rs2Addr = paramMap.get(MaskLabel.RS2)
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm7 = paramMap.get(MaskLabel.IMM7)
                        val imm5 = paramMap.get(MaskLabel.IMM5)
                        val pc = architecture.getRegisterContainer().pc
                        if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                            val imm7str = imm7.getResized(MutVal.Size.Bit7()).getRawBinaryStr()
                            val imm5str = imm5.getResized(MutVal.Size.Bit5()).getRawBinaryStr()
                            val imm12 = MutVal.Value.Binary(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), MutVal.Size.Bit12())
                            val offset = imm12.toBin().getResized(MutVal.Size.Bit32()) shl 1
                            if (rs1.get().toUDec() >= rs2.get().toUDec()) {
                                pc.value.set(pc.value.get() + offset)
                            } else {
                                pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                            }
                        }
                    }
                }
            },
            BGEU_Const("BGEUC", true, PS_RS1_RS2_Const12, relative = BGEU),
            BEQ1("BEQ", true, PS_RS1_RS2_Jlbl, relative = BEQ),
            BNE1("BNE", true, PS_RS1_RS2_Jlbl, relative = BNE),
            BLT1("BLT", true, PS_RS1_RS2_Jlbl, relative = BLT),
            BGE1("BGE", true, PS_RS1_RS2_Jlbl, relative = BGE),
            BLTU1("BLTU", true, PS_RS1_RS2_Jlbl, relative = BLTU),
            BGEU1("BGEU", true, PS_RS1_RS2_Jlbl, relative = BGEU),
            LB("LB", false, RD_Off12, OpCode("000000000000 00000 000 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val imm12 = paramMap.get(MaskLabel.IMM12)
                    if (rdAddr != null && rs1Addr != null && imm12 != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && rs1 != null) {
                            val memAddr = rs1.get().toBin() + imm12
                            val loadedByte = architecture.getMemory().load(memAddr).get().toBin().getResized(MutVal.Size.Bit32())
                            rd.set(loadedByte)
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            LH("LH", false, RD_Off12, OpCode("000000000000 00000 001 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val imm12 = paramMap.get(MaskLabel.IMM12)
                    if (rdAddr != null && rs1Addr != null && imm12 != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && rs1 != null) {
                            val memAddr = rs1.get().toBin() + imm12
                            val loadedByte = architecture.getMemory().load(memAddr, 2).get().toBin().getResized(MutVal.Size.Bit32())
                            rd.set(loadedByte)
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            LW("LW", false, RD_Off12, OpCode("000000000000 00000 010 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val imm12 = paramMap.get(MaskLabel.IMM12)
                    if (rdAddr != null && rs1Addr != null && imm12 != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && rs1 != null) {
                            val memAddr = rs1.get().toBin() + imm12
                            val loadedByte = architecture.getMemory().load(memAddr, 4)
                            rd.set(loadedByte.get())
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            LBU("LBU", false, RD_Off12, OpCode("000000000000 00000 100 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val imm12 = paramMap.get(MaskLabel.IMM12)
                    if (rdAddr != null && rs1Addr != null && imm12 != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && rs1 != null) {
                            val memAddr = rs1.get().toBin() + imm12
                            val loadedByte = architecture.getMemory().load(memAddr)
                            rd.set(MutVal.Value.Binary(rd.get().toBin().getRawBinaryStr().substring(0, 24) + loadedByte.get().toBin().getRawBinaryStr(), MutVal.Size.Bit32()))
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            LHU("LHU", false, RD_Off12, OpCode("000000000000 00000 101 00000 0000011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val imm12 = paramMap.get(MaskLabel.IMM12)
                    if (rdAddr != null && rs1Addr != null && imm12 != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && rs1 != null) {
                            val memAddr = rs1.get().toBin() + imm12
                            val loadedByte = architecture.getMemory().load(memAddr, 2)
                            rd.set(MutVal.Value.Binary(rd.get().toBin().getRawBinaryStr().substring(0, 16) + loadedByte.get().toBin().getRawBinaryStr(), MutVal.Size.Bit32()))
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            SB("SB", false, RS2_Off5, OpCode("0000000 00000 00000 000 00000 0100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val rs2Addr = paramMap.get(MaskLabel.RS2)
                    val imm5 = paramMap.get(MaskLabel.IMM5)
                    if (rs1Addr != null && rs2Addr != null && imm5 != null) {
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val pc = architecture.getRegisterContainer().pc
                        if (rs1 != null && rs2 != null) {
                            val memAddr = rs1.get().toBin().getResized(MutVal.Size.Bit32()) + imm5
                            architecture.getMemory().save(memAddr, rs2.get().toBin().getResized(MutVal.Size.Bit8()))
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            SH("SH", false, RS2_Off5, OpCode("0000000 00000 00000 001 00000 0100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val rs2Addr = paramMap.get(MaskLabel.RS2)
                    val imm5 = paramMap.get(MaskLabel.IMM5)
                    if (rs1Addr != null && rs2Addr != null && imm5 != null) {
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val pc = architecture.getRegisterContainer().pc
                        if (rs1 != null && rs2 != null) {
                            val memAddr = rs1.get().toBin().getResized(MutVal.Size.Bit32()) + imm5
                            architecture.getMemory().save(memAddr, rs2.get().toBin().getResized(MutVal.Size.Bit16()))
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            SW("SW", false, RS2_Off5, OpCode("0000000 00000 00000 010 00000 0100011", arrayOf(MaskLabel.IMM7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.IMM5, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val rs2Addr = paramMap.get(MaskLabel.RS2)
                    val imm5 = paramMap.get(MaskLabel.IMM5)
                    if (rs1Addr != null && rs2Addr != null && imm5 != null) {
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val pc = architecture.getRegisterContainer().pc
                        if (rs1 != null && rs2 != null) {
                            val memAddr = rs1.mutVal.get().toBin().getResized(MutVal.Size.Bit32()) + imm5
                            architecture.getMemory().save(memAddr, rs2.get().toBin().getResized(MutVal.Size.Bit32()))
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            ADDI("ADDI", false, RD_RS1_I12, OpCode("000000000000 00000 000 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm12 = paramMap.get(MaskLabel.IMM12)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm32 = imm12.getResized(MutVal.Size.Bit32())
                            val sum = rs1.get().toBin() + paddedImm32
                            rd.set(sum)
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            ADDI_Const("ADDI", true, PS_RD_RS1_Const12, relative = ADDI),
            SLTI("SLTI", false, RD_RS1_I12, OpCode("000000000000 00000 010 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm12 = paramMap.get(MaskLabel.IMM12)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm32 = imm12.getResized(MutVal.Size.Bit32())
                            rd.set(if (rs1.get().toDec() < paddedImm32.toDec()) MutVal.Value.Binary("1", MutVal.Size.Bit32()) else MutVal.Value.Binary("0", MutVal.Size.Bit32()))
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            SLTI_Const("SLTI", true, PS_RD_RS1_Const12, relative = SLTI),
            SLTIU("SLTIU", false, RD_RS1_I12, OpCode("000000000000 00000 011 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm12 = paramMap.get(MaskLabel.IMM12)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm32 = imm12.getUResized(MutVal.Size.Bit32())
                            rd.set(if (rs1.get().toBin() < paddedImm32) MutVal.Value.Binary("1", MutVal.Size.Bit32()) else MutVal.Value.Binary("0", MutVal.Size.Bit32()))
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            SLTIU_Const("SLTIU", true, PS_RD_RS1_Const12, relative = SLTIU),
            XORI("XORI", false, RD_RS1_I12, OpCode("000000000000 00000 100 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm12 = paramMap.get(MaskLabel.IMM12)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm32 = imm12.getUResized(MutVal.Size.Bit32())
                            rd.set(rs1.get().toBin() xor paddedImm32)
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            XORI_Const("XORI", true, PS_RD_RS1_Const12, relative = XORI),
            ORI("ORI", false, RD_RS1_I12, OpCode("000000000000 00000 110 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm12 = paramMap.get(MaskLabel.IMM12)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm32 = imm12.getUResized(MutVal.Size.Bit32())
                            rd.set(rs1.get().toBin() or paddedImm32)
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            ORI_Const("ORI", true, PS_RD_RS1_Const12, relative = ORI),
            ANDI("ANDI", false, RD_RS1_I12, OpCode("000000000000 00000 111 00000 0010011", arrayOf(MaskLabel.IMM12, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm12 = paramMap.get(MaskLabel.IMM12)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm32 = imm12.getUResized(MutVal.Size.Bit32())
                            rd.set(rs1.get().toBin() and paddedImm32)
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            ANDI_Const("ANDI", true, PS_RD_RS1_Const12, relative = ANDI),
            SLLI("SLLI", false, RD_RS1_I5, OpCode("0000000 00000 00000 001 00000 0010011", arrayOf(MaskLabel.FUNCT7, MaskLabel.SHAMT, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val shamt = paramMap.get(MaskLabel.SHAMT)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && shamt != null && rs1 != null) {
                            rd.set(rs1.get().toBin() ushl shamt.getRawBinaryStr().toInt(2))
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            SLLI_Const("SLLI", true, PS_RD_RS1_Const5, relative = SLLI),
            SRLI("SRLI", false, RD_RS1_I5, OpCode("0000000 00000 00000 101 00000 0010011", arrayOf(MaskLabel.FUNCT7, MaskLabel.SHAMT, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val shamt = paramMap.get(MaskLabel.SHAMT)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && shamt != null && rs1 != null) {
                            rd.set(rs1.get().toBin() ushr shamt.getRawBinaryStr().toInt(2))
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            SRLI_Const("SRLI", true, PS_RD_RS1_Const5, relative = SRLI),
            SRAI("SRAI", false, RD_RS1_I5, OpCode("0100000 00000 00000 101 00000 0010011", arrayOf(MaskLabel.FUNCT7, MaskLabel.SHAMT, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val shamt = paramMap.get(MaskLabel.SHAMT)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && shamt != null && rs1 != null) {
                            rd.set(rs1.get().toBin() shr shamt.getRawBinaryStr().toInt(2))
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            SRAI_Const("SRAI", true, PS_RD_RS1_Const5, relative = SRAI),
            ADD("ADD", false, RD_RS1_RS1, OpCode("0000000 00000 00000 000 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val rs2Addr = paramMap.get(MaskLabel.RS2)
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() + rs2.get().toBin())
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            SUB("SUB", false, RD_RS1_RS1, OpCode("0100000 00000 00000 000 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val rs2Addr = paramMap.get(MaskLabel.RS2)
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() - rs2.get().toBin())
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            SLL("SLL", false, RD_RS1_RS1, OpCode("0000000 00000 00000 001 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val rs2Addr = paramMap.get(MaskLabel.RS2)
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() ushl rs2.get().toBin().getRawBinaryStr().toInt(2))
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            SLT("SLT", false, RD_RS1_RS1, OpCode("0000000 00000 00000 010 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val rs2Addr = paramMap.get(MaskLabel.RS2)
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(if (rs1.get().toDec() < rs2.get().toDec()) MutVal.Value.Binary("1", MutVal.Size.Bit32()) else MutVal.Value.Binary("0", MutVal.Size.Bit32()))
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            SLTU("SLTU", false, RD_RS1_RS1, OpCode("0000000 00000 00000 011 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val rs2Addr = paramMap.get(MaskLabel.RS2)
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(if (rs1.get().toBin() < rs2.get().toBin()) MutVal.Value.Binary("1", MutVal.Size.Bit32()) else MutVal.Value.Binary("0", MutVal.Size.Bit32()))
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            XOR("XOR", false, RD_RS1_RS1, OpCode("0000000 00000 00000 100 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val rs2Addr = paramMap.get(MaskLabel.RS2)
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() xor rs2.get().toBin())
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            SRL("SRL", false, RD_RS1_RS1, OpCode("0000000 00000 00000 101 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val rs2Addr = paramMap.get(MaskLabel.RS2)
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() ushr rs2.get().toBin().getRawBinaryStr().toInt(2))
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            SRA("SRA", false, RD_RS1_RS1, OpCode("0100000 00000 00000 101 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val rs2Addr = paramMap.get(MaskLabel.RS2)
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() shr rs2.get().toBin().getRawBinaryStr().toInt(2))
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            OR("OR", false, RD_RS1_RS1, OpCode("0000000 00000 00000 110 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val rs2Addr = paramMap.get(MaskLabel.RS2)
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() or rs2.get().toBin())
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            AND("AND", false, RD_RS1_RS1, OpCode("0000000 00000 00000 111 00000 0110011", arrayOf(MaskLabel.FUNCT7, MaskLabel.RS2, MaskLabel.RS1, MaskLabel.FUNCT3, MaskLabel.RD, MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(MaskLabel.RD)
                    val rs1Addr = paramMap.get(MaskLabel.RS1)
                    val rs2Addr = paramMap.get(MaskLabel.RS2)
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() and rs2.get().toBin())
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            Nop("NOP", true, PS_NONE),
            Mv("MV", true, PS_RD_RS1),
            Li("LI", true, PS_RD_I32, memWords = 2),
            Li_Const("LI", true, PS_RD_Clbl, memWords = 2),
            La("LA", true, PS_RD_Albl, memWords = 2),
            Not("NOT", true, PS_RD_RS1),
            Neg("NEG", true, PS_RD_RS1),
            Seqz("SEQZ", true, PS_RD_RS1),
            Snez("SNEZ", true, PS_RD_RS1),
            Sltz("SLTZ", true, PS_RD_RS1),
            Sgtz("SGTZ", true, PS_RD_RS1),
            Beqz("BEQZ", true, PS_RS1_Jlbl),
            Bnez("BNEZ", true, PS_RS1_Jlbl),
            Blez("BLEZ", true, PS_RS1_Jlbl),
            Bgez("BGEZ", true, PS_RS1_Jlbl),
            Bltz("BLTZ", true, PS_RS1_Jlbl),
            BGTZ("BGTZ", true, PS_RS1_Jlbl),
            Bgt("BGT", true, PS_RS1_RS2_Jlbl),
            Ble("BLE", true, PS_RS1_RS2_Jlbl),
            Bgtu("BGTU", true, PS_RS1_RS2_Jlbl),
            Bleu("BLEU", true, PS_RS1_RS2_Jlbl),
            J("J", true, PS_Jlbl),
            JAL1("JAL", true, PS_RS1_Jlbl),
            JAL2("JAL", true, PS_Jlbl),
            Jr("JR", true, PS_RS1),
            JALR1("JALR", true, PS_RS1),
            Ret("RET", true, PS_NONE);

            open fun execute(architecture: Architecture, paramMap: Map<MaskLabel, MutVal.Value.Binary>) {
                architecture.getConsole().info("executing $id ...")
            }
        }
    }

    class T1Comment(val prefix: Compiler.Token.Symbol, vararg val content: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RISCVFlags.comment), Syntax.NODE_COMMENT, prefix, *content) {

        val wholeContent: String

        init {
            wholeContent = content.joinToString("") { it.content }
        }
    }

    /* ---------- SYNTAX Tokens: Tier 2 ------------ */

    class T2CommentRow(vararg val comments: T1Comment) : TreeNode.RowNode(Syntax.NODE2_COMMENTCOLLECTION, *comments)

    class T2TextSectionStart(val t1Directive: T1Directive) : TreeNode.RowNode(Syntax.NODE2_TEXTSECTIONSTART, t1Directive)

    class T2DataSectionStart(val t1Directive: T1Directive) : TreeNode.RowNode(Syntax.NODE2_DATASECTIONSTART, t1Directive)

    class T2LabelDef(val type: Type, val t1Label: T1Label, val t1Directive: T1Directive? = null, val t1Param: T1ParamColl? = null) : TreeNode.RowNode(if (type == JUMP) Syntax.NODE2_LABELJUMP else Syntax.NODE2_LABELDEF, t1Label, t1Directive, *t1Param?.elementNodes ?: emptyArray()) {

        enum class Type {
            JUMP,
            MEMALLOC,
            CONSTANT
        }
    }

    class T2InstrDef(val t1Instr: T1Instr, val t1ParamColl: T1ParamColl? = null, val type: T1Instr.Type) : TreeNode.RowNode(Syntax.NODE2_INSTRDEF, t1Instr, *t1ParamColl?.elementNodes ?: emptyArray()) {
        fun check(): Boolean {
            return if (t1ParamColl != null) {
                t1Instr.check(t1ParamColl).first
            } else {
                t1Instr.check().first
            }
        }
    }

    /* ---------- SYNTAX Tokens: Tier 3 ------------ */

    class T3DataSection(val dataDirective: T2DataSectionStart, vararg val rowNodes: TreeNode.RowNode) : TreeNode.SectionNode(Syntax.NODE3_SECTION_DATA, dataDirective, *rowNodes)

    class T3TextSection(val textDirective: T2TextSectionStart? = null, vararg val rowNodes: TreeNode.RowNode) : TreeNode.SectionNode(Syntax.NODE3_SECTION_TEXT, collNodes = arrayOf(textDirective, *rowNodes).filterNotNull().toTypedArray())

    /* ---------- SYNTAX Tokens: Tier 4 ------------ */

    class T4CodeRoot(allComments: T2CommentRow, allErrors: List<Error>, vararg sectionNodes: TreeNode.SectionNode) : TreeNode.RootNode(allErrors, emptyList(), ContainerNode("comments", allComments), ContainerNode("sections", *sectionNodes))
}