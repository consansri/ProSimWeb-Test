package emulator.archs.riscv32

import emulator.kit.Architecture
import emulator.archs.riscv32.RV32Syntax.E_DIRECTIVE.DirType.*
import emulator.kit.assembly.Compiler
import emulator.kit.assembly.Syntax
import emulator.kit.common.FileHandler
import emulator.kit.common.RegContainer
import emulator.kit.common.Transcript
import emulator.archs.riscv32.RV32BinMapper.MaskLabel.*
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*
import emulator.kit.types.Variable.Size.*
import debug.DebugTools


/**
 * This class implements the RV32 Syntax.
 *
 * [check] analyzes the given compiler tokens and builds a syntax tree.
 */
class RV32Syntax() : Syntax() {

    override val applyStandardHLForRest: Boolean = false
    override val decimalValueSize: Variable.Size = Bit32()

    override fun clear() {

    }

    override fun check(compiler: Compiler, tokenLines: List<List<Compiler.Token>>, others: List<FileHandler.File>, transcript: Transcript): SyntaxTree {

        /**
         *  -------------------------------------------------------------- GLOBAL LISTS --------------------------------------------------------------
         *  usage:
         */


        // List which holds the actual state after the actuall scans
        val remainingLines = tokenLines.toMutableList()
        var startIsDefined = false

        // For Root Node
        val errors: MutableList<Error> = mutableListOf()
        val warnings: MutableList<Warning> = mutableListOf()

        val imports: MutableList<TreeNode.SectionNode> = mutableListOf()
        var globalStart: Pre_GLOBAL? = null

        val c_sections: C_SECTIONS
        val c_pres: C_PRES

        // Referencable Elements
        val labels = mutableListOf<E_LABEL>()

        /**
         *  -------------------------------------------------------------- PRE SCAN --------------------------------------------------------------
         *  usage: replace pre elements with their main usage (using compiler.pseudoAnalyze())
         */
        val pres = mutableListOf<TreeNode.ElementNode>()

        // ----------------- resolve imports
        for (lineID in remainingLines.indices) {
            val lineStr = remainingLines[lineID].joinToString("") { it.content }
            SyntaxRegex.pre_import.matchEntire(lineStr)?.let {
                val filename = it.groupValues[2]
                var linkedFile: FileHandler.File? = null
                var linkedTree: SyntaxTree? = null
                for (file in others) {
                    if (file.getName() == filename) {
                        linkedFile = file
                        linkedTree = file.getLinkedTree()
                    }
                }

                if (linkedTree != null) {
                    // Get File Tree
                    linkedTree.rootNode?.let { root ->
                        if (root.allErrors.isNotEmpty()) {
                            errors.add(Syntax.Error("File {filename: $filename} has errors!", *remainingLines[lineID].toTypedArray()))
                        } else {
                            val linked_c_section = root.containers.filter { it is C_SECTIONS }
                            val linked_sections: List<TreeNode.SectionNode> = linked_c_section.flatMap { it.nodes.toList() }.filterIsInstance<TreeNode.SectionNode>()

                            imports.addAll(linked_sections)
                            pres.add(Pre_IMPORT(*remainingLines[lineID].toTypedArray()))
                        }
                    }
                } else {
                    errors.add(Syntax.Error("File {filename: $filename} not ${if (linkedFile != null) "compiled" else "found"}!", *remainingLines[lineID].toTypedArray()))
                }
                remainingLines[lineID] = emptyList()
            }
        }

        if (DebugTools.RV32_showGrammarScanTiers) {
            console.log("Grammar: IMPORTS -> ${
                imports.flatMap { it.collNodes.toList() }.filter { it is R_JLBL || it is R_ILBL || it is R_ULBL }.joinToString { "\n\t ${it.name}" }
            }")
        }

        // ----------------- remove Comments
        for (lineID in remainingLines.indices) {
            val lineContent = remainingLines[lineID].toMutableList()
            val removedComment = mutableListOf<Compiler.Token>()
            for (tokenID in lineContent.indices) {
                val token = lineContent[tokenID]
                if (token.content == "#") {
                    removedComment.addAll(lineContent.subList(tokenID, lineContent.size))
                    lineContent.removeAll(removedComment)
                    break
                }
            }
            if (removedComment.isNotEmpty()) {
                pres.add(Pre_COMMENT(*removedComment.toTypedArray()))
            }
            remainingLines[lineID] = lineContent
        }

        // ----------------- remove options
        for (lineID in remainingLines.indices) {
            val lineStr = remainingLines[lineID].joinToString("") { it.content }
            SyntaxRegex.pre_attribute.matchEntire(lineStr)?.let {
                pres.add(Pre_ATTRIBUTE(*remainingLines[lineID].toTypedArray()))
                remainingLines[lineID] = emptyList()
            }
        }
        // ----------------- remove attributes
        for (lineID in remainingLines.indices) {
            val lineStr = remainingLines[lineID].joinToString("") { it.content }
            SyntaxRegex.pre_option.matchEntire(lineStr)?.let {
                pres.add(Pre_OPTION(*remainingLines[lineID].toTypedArray()))
                remainingLines[lineID] = emptyList()
            }
        }

        // define global start
        for (lineID in remainingLines.indices) {
            val lineStr = remainingLines[lineID].joinToString("") { it.content }
            SyntaxRegex.pre_globalStart.forEach {
                it.matchEntire(lineStr)?.let {
                    val labelName = it.groups[SyntaxRegex.pre_globalStart_contentgroup]?.value ?: ""
                    if (globalStart == null && labelName.isNotEmpty()) {
                        val directives = remainingLines[lineID].filter { it.content == "." || it.content == "global" || it.content == "globl" }
                        val elseTokens = remainingLines[lineID] - directives
                        globalStart = Pre_GLOBAL(ConnectedHL(Pair(RV32Flags.directive, directives), Pair(RV32Flags.pre_global, elseTokens)), *remainingLines[lineID].toTypedArray(), labelName = labelName)
                    } else {
                        if (globalStart != null) {
                            errors.add(Error("global start is already defined in line ${(globalStart?.tokens?.first()?.lineLoc?.lineID ?: -1) + 1}!", *remainingLines[lineID].toTypedArray()))
                        } else {
                            errors.add(Error("global start is missing label name!", *remainingLines[lineID].toTypedArray()))
                        }
                    }
                    remainingLines[lineID] = emptyList()
                }
            }
        }

        // ----------------- replace equ constants
        // find definitions
        val equs = mutableListOf<EquDefinition>()
        for (lineID in remainingLines.indices) {
            val lineStr = remainingLines[lineID].joinToString("") { it.content }
            SyntaxRegex.pre_equ_def.matchEntire(lineStr)?.let {
                val name = compiler.pseudoAnalyze(it.groupValues[2])
                val const = compiler.pseudoAnalyze(it.groupValues[3])
                try {
                    val constMatch = (const.size == 1 && const.first() is Compiler.Token.Constant)
                    if (constMatch) {
                        equs.add(EquDefinition(name, const.first()))
                        val directiveTokens = remainingLines[lineID].filter { it.content == "." || it.content == "equ" }
                        val constant = remainingLines[lineID].filter { it.content != "." && it.content != "equ" && it is Compiler.Token.Constant }
                        val elseTokens = remainingLines[lineID].filter { it.content != "," && !directiveTokens.contains(it) && !constant.contains(it) }
                        pres.add(Pre_EQU(ConnectedHL(Pair(RV32Flags.directive, directiveTokens), Pair(RV32Flags.constant, constant), Pair(RV32Flags.pre_equ, elseTokens)), *remainingLines[lineID].toTypedArray()))
                    } else {
                        val message = "{constant: ${it.groupValues[3]}} is not a valid constant for an equ definition! "
                        errors.add(Error(message, *remainingLines[lineID].toTypedArray()))
                    }
                } catch (e: NoSuchElementException) {
                    console.log(e)
                }

                remainingLines[lineID] = emptyList()
            }
        }

        // resolve definitions
        for (lineID in remainingLines.indices) {
            val tokenList = remainingLines[lineID].toMutableList()
            if (tokenList.isNotEmpty()) {
                val tokenLineID = tokenList.first().lineLoc.lineID
                var replacementHappened = false
                for (equ in equs) {
                    val lineContent = tokenList.joinToString("") { it.content }
                    val result = equ.nameSequence.matches(*tokenList.toTypedArray())
                    val nameTokens = result.sequenceMap.map { it.token }
                    val equName = equ.name.joinToString("") { it.content }
                    if (lineContent.contains(equName)) {
                        val newLineContent = lineContent.replace(equName, equ.constant.content)
                        val newLineTokens = compiler.pseudoAnalyze(newLineContent, tokenLineID)
                        tokenList.clear()
                        tokenList.addAll(newLineTokens)
                        pres.add(Pre_EQU(tokens = nameTokens.toTypedArray()))
                        replacementHappened = true
                    }
                }
                // replace redundant pseudo analyzed tokens with original tokens to enable highlighting of the unchanged part
                if (replacementHappened) {
                    val originalTokens = remainingLines[lineID].toMutableList()
                    for (tokenID in tokenList.indices) {
                        val token = tokenList[tokenID]
                        val originalToken = originalTokens.find { it.content == token.content }
                        if (originalToken != null) {
                            tokenList[tokenID] = originalToken
                        }
                    }
                    remainingLines[lineID] = tokenList
                }
            }

        }


        // ----------------- replace macros
        // define macros
        val macros = mutableListOf<MacroDefinition>()
        var foundStart = false
        var name = ""
        var arguments = mutableListOf<String>()
        var replacementLines = mutableListOf<String>()
        val macroTokens = mutableListOf<Compiler.Token>()
        for (lineID in remainingLines.indices) {
            val lineStr = remainingLines[lineID].joinToString("") { it.content }
            if (!foundStart) {
                SyntaxRegex.pre_macro_start.matchEntire(lineStr)?.let { matchResult ->
                    var validArgs = true
                    arguments.clear()
                    name = matchResult.groupValues[2]
                    matchResult.groupValues[3].split(",").forEach {
                        SyntaxRegex.pre_macro_arg_def.find(it).let { match ->
                            if (match != null) {
                                arguments.add(match.groupValues[1])
                            } else {
                                validArgs = false
                            }
                        }
                    }
                    if(macros.map { it.name }.contains(name)){
                        validArgs = false
                    }

                    macroTokens.addAll(remainingLines[lineID])

                    if (validArgs) {
                        foundStart = true
                    } else {
                        errors.add(Error("Macro ($name) already defined or arguments {${arguments.joinToString(",") { it }}} not alpha numeric!", *macroTokens.toTypedArray()))
                        foundStart = false
                        macroTokens.clear()
                    }
                    remainingLines[lineID] = emptyList()
                }
            } else {
                if (SyntaxRegex.pre_macro_end.matches(lineStr)) {
                    foundStart = false
                    val useMap = mutableMapOf<String, Boolean>()
                    for (arg in arguments) {
                        useMap[arg] = false
                    }
                    for (line in replacementLines) {
                        SyntaxRegex.pre_macro_arg_link.findAll(line).let {
                            for (match in it) {
                                useMap[match.groupValues[1]] = true
                            }
                        }
                    }

                    macroTokens.addAll(remainingLines[lineID])
                    macros.add(MacroDefinition(name, arguments, replacementLines))

                    // for HL
                    val directiveTokens = macroTokens.filter { it.content == "." || it.content == "macro" || it.content == "endm" }
                    val constants = macroTokens.filter { it is Compiler.Token.Constant }
                    val registers = macroTokens.filter { it is Compiler.Token.Register }
                    val instructions = macroTokens.filter { R_INSTR.InstrType.entries.map { it.id.uppercase() }.contains(it.content.uppercase()) }
                    val elseTokens = (macroTokens - directiveTokens - constants - registers - instructions).filter { it.content != "," }

                    pres.add(Pre_MACRO(ConnectedHL(Pair(RV32Flags.directive, directiveTokens), Pair(RV32Flags.constant, constants), Pair(RV32Flags.register, registers), Pair(RV32Flags.instruction, instructions), Pair(RV32Flags.pre_macro, elseTokens)), *macroTokens.toTypedArray()))

                    for (argusage in useMap) {
                        if (argusage.value == false) {
                            warnings.add(Warning("Unused argument (${argusage.key}) in macro definition!", *macroTokens.toTypedArray()))
                        }
                        if (!arguments.contains(argusage.key)) {
                            warnings.add(Warning("Found usage of argument (${argusage.key}) which isn't defined in macro!", *macroTokens.toTypedArray()))
                        }
                    }
                    macroTokens.clear()
                    replacementLines = mutableListOf()
                    arguments = mutableListOf()
                } else {
                    replacementLines.add(lineStr)
                    macroTokens.addAll(remainingLines[lineID])
                }
                remainingLines[lineID] = emptyList()
            }
        }
        if (foundStart) {
            errors.add(Error("Macro definition has no ending (.endm)!", *macroTokens.toTypedArray()))
        }

        // resolve macros
        var macroLineID = 0
        while (macroLineID < remainingLines.size) {
            var skipLines = 1
            val lineStr = remainingLines[macroLineID].joinToString("") { it.content }
            SyntaxRegex.pre_macro_line.matchEntire(lineStr)?.let { matchResult ->
                val resultName = matchResult.groupValues[1]
                val argumentContent = mutableListOf<String>()
                matchResult.groupValues[2].split(",").forEach {
                    argumentContent.add(it.trimStart().trimEnd())
                }

                val matchingMacros = macros.filter { it.name == resultName }
                if (matchingMacros.size == 1) {

                    val macro = matchingMacros.first()
                    if (macro.arguments.size == argumentContent.size) {

                        // for HL
                        val registers = remainingLines[macroLineID].filter { it is Compiler.Token.Register }
                        val constants = remainingLines[macroLineID].filter { it is Compiler.Token.Constant }
                        val elseTokens = (remainingLines[macroLineID] - registers - constants).filter { it.content != "," }

                        pres.add(Pre_MACRO(ConnectedHL(Pair(RV32Flags.register, registers), Pair(RV32Flags.constant, constants), Pair(RV32Flags.pre_macro, elseTokens)), *remainingLines[macroLineID].toTypedArray()))
                        remainingLines.removeAt(macroLineID)

                        for (macroLine in macro.replacementLines.reversed()) {
                            var replacedLine = macroLine
                            for (attrID in macro.arguments.indices) {
                                replacedLine = replacedLine.replace("""\""" + macro.arguments[attrID], argumentContent[attrID])
                            }
                            if (DebugTools.RV32_showGrammarScanTiers) {
                                console.log("\tmacro insert line ${macroLineID + 1}: $replacedLine")
                            }
                            remainingLines.add(macroLineID, compiler.pseudoAnalyze(replacedLine))
                        }
                        skipLines = macro.replacementLines.size
                    }
                }
            }

            macroLineID += skipLines
        }

        /**
         * FIND UNRESOLVED
         */
        for (lineID in remainingLines.indices) {
            val lineStr = remainingLines[lineID].joinToString("") { it.content }
            for (regex in SyntaxRegex.pre_unresolvedList) {
                regex.matchEntire(lineStr)?.let {
                    pres.add(Pre_UNRESOLVED(*remainingLines[lineID].toTypedArray()))
                    remainingLines[lineID] = emptyList()
                }
            }
        }

        /**
         * FIND REMAINING ERRORS
         */
        for (lineID in remainingLines.indices) {
            val lineStr = remainingLines[lineID].joinToString("") { it.content }
            for (entry in SyntaxInfo.pre_map) {
                if (lineStr.contains(entry.key)) {
                    warnings.add(Warning("${entry.key} element doesn't match right Syntax! ${entry.value}", *remainingLines[lineID].toTypedArray()))
                }
            }
        }

        /**
         * FINISH PRE SCAN
         */
        if (DebugTools.RV32_showGrammarScanTiers) {
            console.log("Grammar: PRE Scan -> ${
                remainingLines.filter { it.isNotEmpty() }.joinToString("") { tokenList ->
                    "\n\tline ${remainingLines.indexOf(tokenList) + 1}: " + tokenList.joinToString("") { it.content }
                }
            }")
        }

        /**
         *  -------------------------------------------------------------- ELEMENT SCAN --------------------------------------------------------------
         *  usage:
         */
        val elements = mutableListOf<MutableList<TreeNode.ElementNode>>()

        // --------------------------------- SCAN LABELS
        for (lineID in remainingLines.indices) {
            val remainingTokens = remainingLines[lineID].toMutableList()
            val lineElements = mutableListOf<TreeNode.ElementNode>()

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
                                    is Compiler.Token.Space -> break
                                    else -> labelName.add(0, prevToken)
                                }

                                previous--
                            }
                            val labelNameString = labelName.joinToString("") { it.content }

                            // check if sublabel
                            var sublabelFrom: E_LABEL? = null
                            if (labelNameString.first() == '.') {
                                val superLabelsToCheck = labels.toMutableList()
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
                            for (label in labels) {
                                if (label.wholeName == (if (sublabelFrom != null) sublabelFrom.wholeName else "") + labelNameString) {
                                    alreadyDefined = true
                                }
                            }
                            for (label in imports.flatMap { it.collNodes.toList() }.flatMap { it.elementNodes.toList() }.filterIsInstance<E_LABEL>().toList()) {
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
                            val e_label = E_LABEL(*labelName.toTypedArray(), colon = colon, sublblFrom = sublabelFrom)
                            remainingTokens.removeAll(labelName)
                            remainingTokens.remove(colon)
                            labels.add(e_label)
                            lineElements.add(e_label)
                            break
                        }
                    }

                    else -> {}
                }
            }

            remainingLines[lineID] = remainingTokens
            elements.add(lineElements)
        }

        // --------------------------------- SCAN OTHER ELEMENTS

        for (lineID in remainingLines.indices) {
            val remainingTokens = remainingLines[lineID].toMutableList()
            val lineElements = mutableListOf<TreeNode.ElementNode>()

            // ----------------- search directive
            val directiveName = mutableListOf<Compiler.Token>()
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

                    is Compiler.Token.AlphaNum -> {
                        if (dot != null) {
                            directiveName.add(token)
                        } else {
                            break
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
                val dirName = directiveName.joinToString("") { it.content }
                val typeMap = E_DIRECTIVE.DirType.entries.associate { it.dirname.lowercase() to it.ordinal }
                val ordinal = typeMap.get(dirName.lowercase())
                if (ordinal != null) {
                    val e_directive = E_DIRECTIVE(E_DIRECTIVE.DirType.entries[ordinal], dot, *directiveName.toTypedArray())
                    remainingTokens.removeAll(directiveName)
                    remainingTokens.remove(dot)
                    lineElements.add(e_directive)
                }
            }

            // ----------------- search instruction
            for (token in remainingTokens) {
                when (token) {
                    is Compiler.Token.Space -> continue

                    is Compiler.Token.Word -> {
                        val validTypes = mutableListOf<R_INSTR.InstrType>()
                        for (type in R_INSTR.InstrType.entries) {
                            if (type.id.uppercase() == token.content.uppercase()) {
                                validTypes.add(type)
                            }
                        }
                        if (validTypes.isNotEmpty()) {
                            val e_instrname = E_INSTRNAME(token, *validTypes.toTypedArray())
                            remainingTokens.remove(token)
                            lineElements.add(e_instrname)
                        }
                    }

                    else -> break
                }
            }

            // ----------------- search parameters
            val paramBuffer = remainingTokens
            val parameterList = mutableListOf<E_PARAM>()
            var validParams = true
            while (paramBuffer.isNotEmpty()) {
                val firstToken = paramBuffer.first()

                if (firstToken is Compiler.Token.Space) {
                    paramBuffer.remove(firstToken)
                    continue
                }

                if (parameterList.isEmpty() || parameterList.last() is E_PARAM.SplitSymbol) {

                    // OFFSET
                    val offsetResult = E_PARAM.Offset.Syntax.tokenSeq.matchStart(*paramBuffer.toTypedArray())
                    if (offsetResult.matches) {
                        val constant = offsetResult.sequenceMap.get(0)
                        val lParan = offsetResult.sequenceMap.get(1)
                        val reg = offsetResult.sequenceMap.get(2)
                        val rParan = offsetResult.sequenceMap.get(3)
                        val e_offset = E_PARAM.Offset(constant.token as Compiler.Token.Constant, lParan.token, reg.token as Compiler.Token.Register, rParan.token)
                        parameterList.add(e_offset)
                        paramBuffer.removeAll(e_offset.tokens.toSet())
                        continue
                    }

                    // REGISTER
                    if (firstToken is Compiler.Token.Register) {
                        parameterList.add(E_PARAM.Register(firstToken))
                        paramBuffer.remove(firstToken)
                        continue
                    }

                    // CONSTANT
                    if (firstToken is Compiler.Token.Constant) {
                        parameterList.add(E_PARAM.Constant(firstToken))
                        paramBuffer.remove(firstToken)
                        continue
                    }

                    // LINK
                    var link: E_PARAM.Link?
                    val tokensForLabelToCheck = mutableListOf<Compiler.Token>()
                    for (possibleLabelToken in paramBuffer.dropWhile { firstToken != it }) {
                        if (possibleLabelToken is Compiler.Token.Space || (possibleLabelToken.content == ",")) {
                            break
                        } else {
                            tokensForLabelToCheck.add(possibleLabelToken)
                        }
                    }
                    if (tokensForLabelToCheck.isNotEmpty()) {
                        link = E_PARAM.Link(*tokensForLabelToCheck.toTypedArray())
                        parameterList.add(link)
                        paramBuffer.removeAll(link.labelName.toSet())
                        continue
                    }


                } else {
                    // SPLITSYMBOL
                    if (firstToken is Compiler.Token.Symbol && firstToken.content == ",") {
                        parameterList.add(E_PARAM.SplitSymbol(firstToken))
                        paramBuffer.remove(firstToken)
                        continue
                    }
                }

                validParams = false
                break
            }

            if (validParams && parameterList.isNotEmpty()) {
                val paramArray = parameterList.toTypedArray()
                val e_paramcoll = E_PARAMCOLL(*paramArray)
                remainingTokens.removeAll(e_paramcoll.params.flatMap { it.paramTokens.toList() })
                lineElements.add(e_paramcoll)
            } else {
                if (remainingTokens.isNotEmpty()) {
                    errors.add(Error("Tokens couldn't get matched to valid RISC-V ASM Element!", *parameterList.flatMap { it.paramTokens.toList() }.toTypedArray(), *remainingTokens.toTypedArray()))
                }
            }

            // NOT MATCHED TOKENS
            remainingLines[lineID] = remainingTokens
            elements[lineID] += lineElements
        }

        if (DebugTools.RV32_showGrammarScanTiers) {
            console.log("Grammar: ELEMENTS Scan -> ${elements.filter { it.isNotEmpty() }.joinToString("") { "\n\tline ${elements.indexOf(it) + 1}: " + it.joinToString(" ") { it.name } }}")
        }

        /**
         * FINISH ELEMENT SCAN
         */

        /**
         *  -------------------------------------------------------------- ROW SCAN --------------------------------------------------------------
         *  usage:
         *  1.  All Labels JLabel, ULabel, ILabel
         *  2.  All Directives
         *  3.  All Instructions
         *
         *
         */
        val rows: Array<TreeNode.RowNode?> = arrayOfNulls(elements.size)

        // ----------------- 1.    All Labels
        for (lineID in elements.indices) {
            val lineElements = elements[lineID].toMutableList()
            val rowNode: TreeNode.RowNode?

            var result = RowSeqs.ilabel.exacltyMatches(*lineElements.toTypedArray())
            if (result.matches) {
                if (result.matchingTreeNodes.size == 3) {
                    val eLabel = result.matchingTreeNodes[0] as E_LABEL
                    val eDir = result.matchingTreeNodes[1] as E_DIRECTIVE
                    val eParamcoll = result.matchingTreeNodes[2] as E_PARAMCOLL
                    if (eParamcoll.paramsWithOutSplitSymbols.size == 1 && eDir.isDataEmitting() && eParamcoll.paramsWithOutSplitSymbols.first() is E_PARAM.Constant) {
                        rowNode = R_ILBL(eLabel, eDir, eParamcoll)
                        rows[lineID] = rowNode
                    } else {
                        errors.add(Syntax.Error((if (!eDir.isDataEmitting()) "Not a data emitting directive for" else "Invalid parameter count for") + " initialized label!", nodes = lineElements.toTypedArray()))
                    }
                    result.error?.let {
                        errors.add(it)
                    }
                    elements[lineID] = mutableListOf()
                    continue
                }
            }

            result = RowSeqs.ulabel.exacltyMatches(*lineElements.toTypedArray())
            if (result.matches) {
                if (result.matchingTreeNodes.size == 2) {
                    val eLabel = result.matchingTreeNodes[0] as E_LABEL
                    val eDir = result.matchingTreeNodes[1] as E_DIRECTIVE
                    if (eDir.isDataEmitting()) {
                        rowNode = R_ULBL(eLabel, eDir)
                        rows[lineID] = rowNode
                    } else {
                        errors.add(Syntax.Error("Not a data emitting directive for uninitialized label!", nodes = lineElements.toTypedArray()))
                    }
                    result.error?.let {
                        errors.add(it)
                    }
                    elements[lineID] = mutableListOf()
                    continue
                }
            }

            result = RowSeqs.jlabel.exacltyMatches(*lineElements.toTypedArray())
            if (result.matches) {
                if (result.matchingTreeNodes.size == 1) {
                    val eLabel = result.matchingTreeNodes[0] as E_LABEL
                    val isGlobalStart = (eLabel.wholeName == globalStart?.labelName)
                    if (isGlobalStart) {
                        globalStart?.let {
                            pres.add(it)
                            globalStart = null
                        }
                    }
                    rowNode = R_JLBL(eLabel, !eLabel.isSubLabel, isGlobalStart)
                    rows[lineID] = rowNode
                    result.error?.let {
                        errors.add(it)
                    }
                    elements[lineID] = mutableListOf()
                    continue
                }
            }
        }

        // ----------------- 2.    All Directives
        for (lineID in elements.indices) {
            val lineElements = elements[lineID].toMutableList()
            val rowNode: TreeNode.RowNode?

            val result = RowSeqs.directive.exacltyMatches(*lineElements.toTypedArray())
            if (result.matches) {
                val eDir = result.matchingTreeNodes[0] as E_DIRECTIVE
                if (eDir.type.majorType == E_DIRECTIVE.MajorType.SECTIONSTART) {
                    rowNode = R_SECSTART(eDir)
                    rows[lineID] = rowNode
                } else {
                    errors.add(Syntax.Error("Found directive type which shouldn't indicate a section start!", *lineElements.toTypedArray()))
                }
                result.error?.let {
                    errors.add(it)
                }
                elements[lineID] = mutableListOf()
                continue
            }
        }

        // ----------------- 3.    All Instructions
        for (lineID in elements.indices) {
            val lineElements = elements[lineID].toMutableList()
            val rowNode: TreeNode.RowNode?

            var result = RowSeqs.instrWithParams.exacltyMatches(*lineElements.toTypedArray())
            if (result.matches && result.matchingTreeNodes.size == 2) {
                var lastMainJLBL: R_JLBL? = null
                for (index in lineID downTo 0) {
                    val rowContent = rows.getOrNull(index)
                    if (rowContent != null && rowContent is R_JLBL && rowContent.isMainLabel) {
                        lastMainJLBL = rowContent
                        break
                    }
                }

                val eInstrName = result.matchingTreeNodes[0] as E_INSTRNAME
                val eParamcoll = result.matchingTreeNodes[1] as E_PARAMCOLL

                // link params
                for (param in eParamcoll.paramsWithOutSplitSymbols) {
                    if (param is E_PARAM.Link) {
                        if (param.linkName.startsWith(".") && lastMainJLBL != null) {
                            // links sublabel
                            val searchName = lastMainJLBL.label.wholeName + param.linkName

                            // scan imports
                            for (importRowNode in imports.flatMap { it.collNodes.toList() }.filterIsInstance<TreeNode.RowNode>().toList()) {
                                when (importRowNode) {
                                    is R_JLBL -> {
                                        if (searchName == importRowNode.label.wholeName) {
                                            param.linkTo(importRowNode)
                                            break
                                        }
                                    }

                                    is R_ULBL -> {
                                        if (searchName == importRowNode.label.wholeName) {
                                            param.linkTo(importRowNode)
                                            break
                                        }
                                    }

                                    is R_ILBL -> {
                                        if (searchName == importRowNode.label.wholeName) {
                                            param.linkTo(importRowNode)
                                            break
                                        }
                                    }
                                }
                            }

                            for (label in rows) {
                                when (label) {
                                    is R_JLBL -> {
                                        if (searchName == label.label.wholeName) {
                                            param.linkTo(label)
                                            break
                                        }
                                    }
                                }
                            }
                        } else {
                            // links main label
                            val searchName = param.linkName

                            // scan imports
                            for (importRowNode in imports.flatMap { it.collNodes.toList() }.filterIsInstance<TreeNode.RowNode>().toList()) {
                                when (importRowNode) {
                                    is R_JLBL -> {
                                        if (searchName == importRowNode.label.wholeName) {
                                            param.linkTo(importRowNode)
                                            break
                                        }
                                    }

                                    is R_ULBL -> {
                                        if (searchName == importRowNode.label.wholeName) {
                                            param.linkTo(importRowNode)
                                            break
                                        }
                                    }

                                    is R_ILBL -> {
                                        if (searchName == importRowNode.label.wholeName) {
                                            param.linkTo(importRowNode)
                                            break
                                        }
                                    }
                                }
                            }

                            for (label in rows) {
                                when (label) {
                                    is R_JLBL -> {
                                        if (searchName == label.label.wholeName) {
                                            param.linkTo(label)
                                            break
                                        }
                                    }

                                    is R_ULBL -> {
                                        if (searchName == label.label.wholeName) {
                                            param.linkTo(label)
                                            break
                                        }
                                    }

                                    is R_ILBL -> {
                                        if (searchName == label.label.wholeName) {
                                            param.linkTo(label)
                                            break
                                        }
                                    }
                                }
                            }
                        }
                        if (param.getLinkType() == null) {
                            warnings.add(Warning("Parameter [${param.linkName}] couldn't get linked to any label!", *param.tokens))
                        }
                    }
                }

                // check params
                val checkedType = eInstrName.check(eParamcoll)
                if (checkedType != null) {
                    val matchesSize = eInstrName.matchesSize(paramcoll = eParamcoll, instrType = checkedType)
                    if (!matchesSize.toBig) {
                        rowNode = R_INSTR(eInstrName, eParamcoll, checkedType, lastMainJLBL, if (!startIsDefined) true else false)
                        startIsDefined = true
                        rows[lineID] = rowNode
                        result.error?.let {
                            errors.add(it)
                        }
                    } else {
                        warnings.add(Warning("Parameter with length of ${matchesSize.valBitLength} Bits for ${checkedType.id} instruction exceeds maximum size of ${checkedType.paramType.immMaxSize?.bitWidth} Bits!", *lineElements.toTypedArray()))
                        rowNode = R_INSTR(eInstrName, eParamcoll, checkedType, lastMainJLBL, if (!startIsDefined) true else false)
                        startIsDefined = true
                        rows[lineID] = rowNode
                        result.error?.let {
                            errors.add(it)
                        }
                    }

                } else {
                    errors.add(Error("Couldn't match parameters to any instruction!\nexpected parameters: ${eInstrName.types.joinToString(" or ") { it.paramType.exampleString }}", *lineElements.toTypedArray()))
                }
                elements[lineID] = mutableListOf()
                continue
            }

            result = RowSeqs.instrWithoutParams.exacltyMatches(*lineElements.toTypedArray())
            if (result.matches) {
                var lastMainJLBL: R_JLBL? = null
                for (index in lineID downTo 0) {
                    val rowContent = rows[index]
                    if (rowContent is R_JLBL && rowContent.isMainLabel) {
                        lastMainJLBL = rowContent
                        break
                    }
                }

                val eInstrName = result.matchingTreeNodes[0] as E_INSTRNAME

                // check params
                val checkedType = eInstrName.check()
                if (checkedType != null) {
                    rowNode = R_INSTR(eInstrName, null, checkedType, lastMainJLBL, if (!startIsDefined) true else false)
                    startIsDefined = true
                    rows[lineID] = rowNode
                    result.error?.let {
                        errors.add(it)
                    }
                } else {
                    errors.add(Error("Couldn't match parameters to any instruction!\nexpected parameters: ${eInstrName.types.joinToString(" or ") { it.paramType.exampleString }}", *lineElements.toTypedArray()))
                }
                elements[lineID] = mutableListOf()
                continue
            }

            if (lineElements.isNotEmpty()) {
                errors.add(Syntax.Error("couldn't match Elements to RiscV Row!", *lineElements.toTypedArray()))
            }
        }


        globalStart?.let {
            errors.add(Error("Global start '${it.labelName}' label not found!", it))
        }
        /**
         * FINISH ROW SCAN
         */
        if (DebugTools.RV32_showGrammarScanTiers) {
            console.log("Grammar: ROWS Scan -> ${rows.filterNotNull().joinToString("") { "\n\tline ${rows.indexOf(it) + 1}: ${it.name}" }}")
        }


        /**
         *  -------------------------------------------------------------- SECTION SCAN --------------------------------------------------------------
         *  usage:
         */
        val sections = mutableListOf<TreeNode.SectionNode>()

        val remainingRowList = rows.toMutableList()
        var sectionType = TEXT
        var sectionIdentification: R_SECSTART? = null
        val sectionContent = mutableListOf<TreeNode.RowNode>()

        while (remainingRowList.isNotEmpty()) {
            val firstRow = remainingRowList.first()
            var resolved = false

            when (firstRow) {
                is R_SECSTART -> {
                    when (sectionType) {
                        TEXT -> {
                            sections.add(S_TEXT(sectionIdentification, *sectionContent.toTypedArray()))
                            resolved = true
                        }

                        DATA -> if (sectionIdentification != null) {
                            sections.add(S_DATA(sectionIdentification, *sectionContent.toTypedArray()))
                            resolved = true
                        }

                        RODATA -> if (sectionIdentification != null) {
                            sections.add(S_RODATA(sectionIdentification, *sectionContent.toTypedArray()))
                            resolved = true
                        }

                        BSS -> if (sectionIdentification != null) {
                            sections.add(S_BSS(sectionIdentification, *sectionContent.toTypedArray()))
                            resolved = true
                        }

                        else -> {}
                    }

                    sectionType = firstRow.directive.type
                    sectionIdentification = firstRow
                    sectionContent.clear()
                }

                is R_INSTR -> {
                    if (sectionType == TEXT) {
                        sectionContent.add(firstRow)
                        resolved = true
                    }
                }

                is R_JLBL -> {
                    if (sectionType == TEXT) {
                        sectionContent.add(firstRow)
                        resolved = true
                    }
                }

                is R_ILBL -> {
                    if (sectionType == DATA || sectionType == RODATA) {
                        sectionContent.add(firstRow)
                        resolved = true
                    }
                }

                is R_ULBL -> {
                    if (sectionType == BSS) {
                        sectionContent.add(firstRow)
                        resolved = true
                    }
                }
            }

            if (!resolved && firstRow != null) {
                errors.add(Syntax.Error("Couldn't match row into section!", firstRow))
            }

            remainingRowList.removeFirst()
        }
        // resolve last section
        when (sectionType) {
            TEXT -> {
                sections.add(S_TEXT(sectionIdentification, *sectionContent.toTypedArray()))
            }

            DATA -> if (sectionIdentification != null) {
                sections.add(S_DATA(sectionIdentification, *sectionContent.toTypedArray()))
            }

            RODATA -> if (sectionIdentification != null) {
                sections.add(S_RODATA(sectionIdentification, *sectionContent.toTypedArray()))
            }

            BSS -> if (sectionIdentification != null) {
                sections.add(S_BSS(sectionIdentification, *sectionContent.toTypedArray()))
            }

            else -> {}
        }

        /**
         * FINISH SECTION SCAN
         */
        if (DebugTools.RV32_showGrammarScanTiers) {
            console.log("Grammar: SECTIONS Scan -> ${sections.joinToString("") { "\n\tsection ${sections.indexOf(it) + 1}: ${it.name}" }}")
        }
        /**
         *  -------------------------------------------------------------- CONTAINER SCAN --------------------------------------------------------------
         *  usage:
         */
        c_pres = C_PRES(*pres.toTypedArray())
        c_sections = C_SECTIONS(*sections.toTypedArray(), *imports.toTypedArray())

        /**
         * FINISH CONTAINER SCAN
         */

        /**
         * Pass Instructions and JLabels to Transcript
         */
        val compiledTSRows = mutableListOf<RVCompiledRow>()
        var address: Variable.Value = Hex("0", Bit32())
        var compiledRow = RVCompiledRow(address.toHex())
        for (section in sections + imports) {
            when (section) {
                is S_TEXT -> {
                    for (row in section.sectionContent) {
                        when (row) {
                            is R_JLBL -> {
                                compiledRow.addLabel(row)
                            }

                            is R_INSTR -> {
                                compiledRow.addInstr(row)
                                when (row.instrType.memWords) {
                                    2 -> {
                                        compiledRow.addAddresses(address + Hex("0x04", Bit8()))
                                    }
                                }
                                compiledRow.changeHeight(row.instrType.memWords)
                                compiledTSRows.add(compiledRow)
                                address += Hex((row.instrType.memWords * 4).toString(16), Bit8())
                                compiledRow = RVCompiledRow(address.toHex())
                            }
                        }

                    }
                }

                else -> {}
            }
        }
        compiledTSRows.add(compiledRow)
        transcript.addContent(Transcript.Type.COMPILED, compiledTSRows)

        val rootNode = TreeNode.RootNode(errors, warnings, c_sections, c_pres)

        return SyntaxTree(rootNode)
    }

    /* -------------------------------------------------------------- TREE COMPONENTS -------------------------------------------------------------- */
    object SyntaxRegex {
        val pre_macro_start = Regex("""^\s*(\.macro\s+([a-zA-Z0-9_]+)\s+((?:[-'"a-zA-Z0-9_]+\s*,\s*)*[-'"a-zA-Z0-9_]+))\s*?""")
        val pre_macro_arg_def = Regex("""^\s*([a-zA-Z0-9_]+)\s*?""")
        val pre_macro_line = Regex("""^\s*([a-zA-Z0-9_]+)\s+((?:[-\s'"a-zA-Z0-9_]+\s*,\s*)*[-\s'"a-zA-Z0-9_]+)\s*?""")
        val pre_macro_arg_link = Regex("""\\([a-zA-Z0-9_]+)""")
        val pre_macro_end = Regex("""^\s*(\.endm)\s*?""")
        val pre_import = Regex("""^\s*(#import\s+"(.+)")\s*?""")
        val pre_comment = Regex("""^\s*#.*?""")
        val pre_equ_def = Regex("""^\s*(\.equ\s+(\S+)\s*,\s*(\S+))\s*?""")
        val pre_option = Regex("""^\s*(\.option\s+.+)\s*?""")
        val pre_attribute = Regex("""^\s*(\.attribute\s+.+)\s*?""")
        val pre_globalStart = listOf(Regex("""^\s*(\.global\s+(?<labelName>\S+))\s*?"""), Regex("""^\s*(\.globl\s+(?<labelName>\S+))\s*?"""))
        val pre_globalStart_contentgroup = "labelName"

        val pre_unresolvedList = listOf(Regex("""^\s*(fence\.i)\s*?"""))
    }

    object RowSeqs {
        val jlabel = RowSeq(RowSeq.Component(REFS.REF_E_LABEL))
        val ulabel = RowSeq(RowSeq.Component(REFS.REF_E_LABEL), RowSeq.Component(REFS.REF_E_DIRECTIVE))
        val ilabel = RowSeq(RowSeq.Component(REFS.REF_E_LABEL), RowSeq.Component(REFS.REF_E_DIRECTIVE), RowSeq.Component(REFS.REF_E_PARAMCOLL))

        val directive = RowSeq(RowSeq.Component(REFS.REF_E_DIRECTIVE))

        val instrWithoutParams = RowSeq(RowSeq.Component(REFS.REF_E_INSTRNAME))
        val instrWithParams = RowSeq(RowSeq.Component(REFS.REF_E_INSTRNAME), RowSeq.Component(REFS.REF_E_PARAMCOLL))
    }

    object SyntaxInfo {
        val pre_map = mapOf<String, String>(
            ".equ" to ".equ [name], [value]", ".macro" to "\n.macro [name] [attr1, ..., attrn]\n\t[macro usage referencing attribs with starting '\\']\n\t...\n.endm\n"
        )
    }

    object REFS {
        val REF_PRE_IMPORT = "PRE_import"
        val REF_PRE_COMMENT = "PRE_comment"
        val REF_PRE_UNRESOLVED = "PRE_unresolved"
        val REF_PRE_OPTION = "PRE_option"
        val REF_PRE_GLOBAL = "PRE_global"
        val REF_PRE_ATTRIBUTE = "PRE_attribute"
        val REF_PRE_MACRO = "PRE_macro"
        val REF_PRE_EQU = "PRE_equ"

        val REF_E_INSTRNAME = "E_instrname"
        val REF_E_PARAMCOLL = "E_paramcoll"
        val REF_E_PARAM_OFFSET = "E_offset"
        val REF_E_PARAM_CONSTANT = "E_constant"
        val REF_E_PARAM_REGISTER = "E_register"
        val REF_E_PARAM_LABELLINK = "E_labellink"
        val REF_E_PARAM_SPLITSYMBOL = "E_splitsymbol"
        val REF_E_LABEL = "E_label"
        val REF_E_DIRECTIVE = "E_directive"

        val REF_R_SECSTART = "R_secstart"
        val REF_R_INSTR = "R_instr"
        val REF_R_JLBL = "R_jlbl"
        val REF_R_ILBL = "R_ilbl"
        val REF_R_ULBL = "R_ulbl"

        val REF_S_TEXT = "S_text"
        val REF_S_DATA = "S_data"
        val REF_S_RODATA = "S_rodata"
        val REF_S_BSS = "S_bss"

        val REF_C_SECTIONS = "C_sections"
        val REF_C_PRES = "C_pres"
    }

    /* -------------------------------------------------------------- PRES -------------------------------------------------------------- */

    class Pre_IMPORT(vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RV32Flags.pre_import), REFS.REF_PRE_IMPORT, *tokens)
    class Pre_COMMENT(vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RV32Flags.comment), REFS.REF_PRE_COMMENT, *tokens)
    class Pre_OPTION(vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RV32Flags.pre_option), REFS.REF_PRE_OPTION, *tokens)
    class Pre_ATTRIBUTE(vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RV32Flags.pre_attribute), REFS.REF_PRE_ATTRIBUTE, *tokens)
    class Pre_GLOBAL(connectedHL: ConnectedHL = ConnectedHL(RV32Flags.pre_global), vararg tokens: Compiler.Token, val labelName: String) : TreeNode.ElementNode(connectedHL, REFS.REF_PRE_GLOBAL, *tokens)
    class Pre_MACRO(connectedHL: ConnectedHL = ConnectedHL(RV32Flags.pre_macro), vararg tokens: Compiler.Token) : TreeNode.ElementNode(connectedHL, REFS.REF_PRE_MACRO, *tokens)
    class Pre_EQU(connectedHL: ConnectedHL = ConnectedHL(RV32Flags.pre_equ), vararg tokens: Compiler.Token) : TreeNode.ElementNode(connectedHL, REFS.REF_PRE_EQU, *tokens)
    class Pre_UNRESOLVED(vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RV32Flags.pre_unresolved), REFS.REF_PRE_UNRESOLVED, *tokens)

    /* -------------------------------------------------------------- ELEMENTS -------------------------------------------------------------- */
    class E_INSTRNAME(val insToken: Compiler.Token.Word, vararg val types: R_INSTR.InstrType) : TreeNode.ElementNode(ConnectedHL(RV32Flags.instruction), REFS.REF_E_INSTRNAME, insToken) {

        fun matchesSize(paramcoll: E_PARAMCOLL = E_PARAMCOLL(), instrType: R_INSTR.InstrType): MatchSizeResult {
            try {
                val exceedingSize: Int = when (instrType.paramType) {
                    R_INSTR.ParamType.RD_I20 -> {
                        val length = paramcoll.getValues()[1].getRawBinaryStr().trimStart('0').length
                        if (length > 20) length else null
                    }

                    R_INSTR.ParamType.RD_Off12 -> {
                        val length = paramcoll.getValues()[1].getRawBinaryStr().trimStart('0').length
                        if (length > 12) length else null
                    }

                    R_INSTR.ParamType.RS2_Off5 -> {
                        val length = paramcoll.getValues()[1].getRawBinaryStr().trimStart('0').length
                        if (length > 5) length else null
                    }

                    R_INSTR.ParamType.RD_RS1_RS1 -> null
                    R_INSTR.ParamType.RD_RS1_I12 -> {
                        val length = paramcoll.getValues()[2].getRawBinaryStr().trimStart('0').length
                        if (length > 12) length else null
                    }

                    R_INSTR.ParamType.RD_RS1_I5 -> {
                        val length = paramcoll.getValues()[2].getRawBinaryStr().trimStart('0').length
                        if (length > 5) length else null
                    }

                    R_INSTR.ParamType.RS1_RS2_I12 -> {
                        val length = paramcoll.getValues()[2].getRawBinaryStr().trimStart('0').length
                        if (length > 12) length else null
                    }

                    R_INSTR.ParamType.PS_RS1_RS2_Jlbl -> null
                    R_INSTR.ParamType.PS_RD_I32 -> {
                        val length = paramcoll.getValues()[1].getRawBinaryStr().trimStart('0').length
                        if (length > 32) length else null
                    }

                    R_INSTR.ParamType.PS_RS1_Jlbl -> null
                    R_INSTR.ParamType.PS_RD_Albl -> null
                    R_INSTR.ParamType.PS_Jlbl -> null
                    R_INSTR.ParamType.PS_RD_RS1 -> null
                    R_INSTR.ParamType.PS_RS1 -> null
                    R_INSTR.ParamType.NONE -> null
                    R_INSTR.ParamType.PS_NONE -> null
                } ?: return MatchSizeResult(false)
                return MatchSizeResult(true, valBitLength = exceedingSize)

            } catch (e: IndexOutOfBoundsException) {
                console.error("RV32Syntax: Index out of bounds exception! Compare value indices with check()! Bug needs to be fixed! ($e)")
                return MatchSizeResult(false)
            }
        }

        fun check(paramcoll: E_PARAMCOLL = E_PARAMCOLL()): R_INSTR.InstrType? {
            val params = paramcoll.paramsWithOutSplitSymbols
            var type: R_INSTR.InstrType
            types.forEach {
                val matches: Boolean
                type = it
                when (it.paramType) {
                    R_INSTR.ParamType.RD_I20 -> {
                        matches = if (params.size == 2) {
                            params[0] is E_PARAM.Register && params[1] is E_PARAM.Constant
                        } else {
                            false
                        }
                    }

                    R_INSTR.ParamType.RD_Off12 -> {
                        matches = if (params.size == 2) {
                            params[0] is E_PARAM.Register && params[1] is E_PARAM.Offset
                        } else {
                            false
                        }
                    }

                    R_INSTR.ParamType.RS2_Off5 -> {
                        matches = if (params.size == 2) {
                            params[0] is E_PARAM.Register && params[1] is E_PARAM.Offset
                        } else {
                            false
                        }
                    }

                    R_INSTR.ParamType.RD_RS1_RS1 -> {
                        matches = if (params.size == 3) {
                            params[0] is E_PARAM.Register && params[1] is E_PARAM.Register && params[2] is E_PARAM.Register
                        } else {
                            false
                        }
                    }

                    R_INSTR.ParamType.RD_RS1_I12 -> {
                        matches = if (params.size == 3) {
                            params[0] is E_PARAM.Register && params[1] is E_PARAM.Register && params[2] is E_PARAM.Constant
                        } else {
                            false
                        }
                    }

                    R_INSTR.ParamType.RD_RS1_I5 -> {
                        matches = if (params.size == 3) {
                            params[0] is E_PARAM.Register && params[1] is E_PARAM.Register && params[2] is E_PARAM.Constant
                        } else {
                            false
                        }
                    }

                    R_INSTR.ParamType.RS1_RS2_I12 -> {
                        matches = if (params.size == 3) {
                            params[0] is E_PARAM.Register && params[1] is E_PARAM.Register && params[2] is E_PARAM.Constant
                        } else {
                            false
                        }
                    }

                    R_INSTR.ParamType.PS_RS1_RS2_Jlbl -> {
                        matches = if (params.size == 3) {
                            params[0] is E_PARAM.Register && params[1] is E_PARAM.Register && (params[2] is E_PARAM.Link && params[2].ifLinkGetLinkType() == E_PARAM.Link.LinkTypes.JLBL)
                        } else {
                            false
                        }
                    }

                    R_INSTR.ParamType.PS_RD_I32 -> matches = if (params.size == 2) {
                        params[0] is E_PARAM.Register && params[1] is E_PARAM.Constant
                    } else {
                        false
                    }

                    R_INSTR.ParamType.PS_RS1_Jlbl -> matches = if (params.size == 2) {
                        params[0] is E_PARAM.Register && (params[1] is E_PARAM.Link && params[1].ifLinkGetLinkType() == E_PARAM.Link.LinkTypes.JLBL)
                    } else {
                        false
                    }

                    R_INSTR.ParamType.PS_RD_Albl -> matches = if (params.size == 2) {
                        params[0] is E_PARAM.Register && (params[1] is E_PARAM.Link && (params[1].ifLinkGetLinkType() == E_PARAM.Link.LinkTypes.ILBL || params[1].ifLinkGetLinkType() == E_PARAM.Link.LinkTypes.ULBL))
                    } else {
                        false
                    }

                    R_INSTR.ParamType.PS_Jlbl -> matches = if (params.size == 1) {
                        params[0] is E_PARAM.Link && params[0].ifLinkGetLinkType() == E_PARAM.Link.LinkTypes.JLBL
                    } else {
                        false
                    }

                    R_INSTR.ParamType.PS_RD_RS1 -> matches = if (params.size == 2) {
                        params[0] is E_PARAM.Register && params[1] is E_PARAM.Register
                    } else {
                        false
                    }

                    R_INSTR.ParamType.PS_RS1 -> matches = if (params.size == 1) {
                        params[0] is E_PARAM.Register
                    } else {
                        false
                    }


                    R_INSTR.ParamType.PS_NONE -> {
                        matches = params.size == 0
                    }

                    R_INSTR.ParamType.NONE -> {
                        matches = params.size == 0
                    }


                }
                if (matches) {
                    return type
                }
            }

            return null
        }

        data class MatchSizeResult(val toBig: Boolean, val valBitLength: Int = 0)
    }

    class E_PARAMCOLL(vararg val params: E_PARAM) : TreeNode.ElementNode(ConnectedHL(*params.map { it.paramHLFlag to it.paramTokens.toList() }.toTypedArray(), global = false, applyNothing = false), REFS.REF_E_PARAMCOLL, *params.flatMap { param -> param.paramTokens.toList() }.toTypedArray()) {
        val paramsWithOutSplitSymbols: Array<E_PARAM>

        init {
            val parameterList = mutableListOf<E_PARAM>()
            for (param in params) {
                if (param !is E_PARAM.SplitSymbol) {
                    parameterList.add(param)
                }
            }
            paramsWithOutSplitSymbols = parameterList.toTypedArray()
        }

        fun getValues(): Array<Bin> {
            val values = mutableListOf<Bin>()
            paramsWithOutSplitSymbols.forEach {
                when (it) {
                    is E_PARAM.Constant -> {
                        var value = it.getValue()
                        /*if (value.size.byteCount < Bit32().byteCount) {
                            value = when (it.constant) {
                                is Compiler.Token.Constant.UDec -> {
                                    value.getUResized(Bit32())
                                }

                                else -> {
                                    value.getResized(Bit32())
                                }
                            }
                        }*/
                        values.add(value)
                    }

                    is E_PARAM.Offset -> {
                        values.addAll(it.getValueArray())
                    }

                    is E_PARAM.Register -> {
                        values.add(it.getAddress())
                    }

                    else -> {}
                }
            }
            return values.toTypedArray()
        }

        fun getILabels(): Array<R_ILBL> {
            val labels = mutableListOf<R_ILBL>()
            paramsWithOutSplitSymbols.forEach { param ->
                when (param) {
                    is E_PARAM.Link -> {
                        param.getILBL()?.let {
                            labels.add(it)
                        }
                    }

                    else -> {}
                }
            }
            return labels.toTypedArray()
        }

        fun getULabels(): Array<R_ULBL> {
            val labels = mutableListOf<R_ULBL>()
            paramsWithOutSplitSymbols.forEach { param ->
                when (param) {
                    is E_PARAM.Link -> {
                        param.getULBL()?.let {
                            labels.add(it)
                        }
                    }

                    else -> {}
                }
            }
            return labels.toTypedArray()
        }

        fun getJLabels(): Array<R_JLBL> {
            val labels = mutableListOf<R_JLBL>()
            paramsWithOutSplitSymbols.forEach { param ->
                when (param) {
                    is E_PARAM.Link -> {
                        param.getJLBL()?.let {
                            labels.add(it)
                        }
                    }

                    else -> {}
                }
            }
            return labels.toTypedArray()
        }
    }

    sealed class E_PARAM(val type: String, val paramHLFlag: String, vararg val paramTokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(paramHLFlag), type, *paramTokens) {
        fun ifLinkGetLinkType(): Link.LinkTypes? {
            return if (this is Link) {
                this.getLinkType()
            } else {
                null
            }
        }

        class Offset(val offset: Compiler.Token.Constant, openParan: Compiler.Token, val register: Compiler.Token.Register, closeParan: Compiler.Token) : E_PARAM(REFS.REF_E_PARAM_OFFSET, RV32Flags.offset, offset, openParan, register, closeParan) {
            fun getValueArray(): Array<Bin> {
                return arrayOf(offset.getValue().toBin(), register.reg.address.toBin())
            }

            object Syntax {
                val tokenSeq = TokenSeq(TokenSeq.Component.InSpecific.Constant(), TokenSeq.Component.Specific("("), TokenSeq.Component.InSpecific.Register(), TokenSeq.Component.Specific(")"), ignoreSpaces = true)
            }
        }

        class Constant(val constant: Compiler.Token.Constant) : E_PARAM(REFS.REF_E_PARAM_CONSTANT, RV32Flags.constant, constant) {
            fun getValue(): Bin {
                return constant.getValue().toBin()
            }
        }

        class Register(val register: Compiler.Token.Register) : E_PARAM(REFS.REF_E_PARAM_REGISTER, RV32Flags.register, register) {
            fun getAddress(): Bin {
                return register.reg.address.toBin()
            }
        }

        class SplitSymbol(val splitSymbol: Compiler.Token.Symbol) : E_PARAM(REFS.REF_E_PARAM_SPLITSYMBOL, RV32Flags.instruction, splitSymbol)
        class Link(vararg val labelName: Compiler.Token) : E_PARAM(REFS.REF_E_PARAM_LABELLINK, RV32Flags.label, *labelName) {
            val linkName = labelName.joinToString("") { it.content }

            private var linkedLabel: RowNode? = null
            private var linkType: LinkTypes? = null

            fun linkTo(jlbl: R_JLBL) {
                linkedLabel = jlbl
                linkType = LinkTypes.JLBL
            }

            fun linkTo(ulbl: R_ULBL) {
                linkedLabel = ulbl
                linkType = LinkTypes.ULBL
            }

            fun linkTo(ilbl: R_ILBL) {
                linkedLabel = ilbl
                linkType = LinkTypes.ILBL
            }

            fun getJLBL(): R_JLBL? {
                return if (linkType == LinkTypes.JLBL) {
                    linkedLabel as R_JLBL
                } else {
                    null
                }
            }

            fun getULBL(): R_ULBL? {
                return if (linkType == LinkTypes.ULBL) {
                    linkedLabel as R_ULBL
                } else {
                    null
                }
            }

            fun getILBL(): R_ILBL? {
                return if (linkType == LinkTypes.ILBL) {
                    linkedLabel as R_ILBL
                } else {
                    null
                }
            }

            fun getLinkType(): LinkTypes? {
                return linkType
            }

            enum class LinkTypes {
                JLBL, ULBL, ILBL
            }

        }
    }

    class E_LABEL(vararg val labelName: Compiler.Token, colon: Compiler.Token.Symbol, val sublblFrom: E_LABEL? = null) : TreeNode.ElementNode(ConnectedHL(RV32Flags.label), REFS.REF_E_LABEL, *labelName, colon) {

        val wholeName: String
        val tokenSeq: TokenSeq
        val isSubLabel: Boolean

        init {
            isSubLabel = sublblFrom != null

            wholeName = (if (sublblFrom != null) sublblFrom.wholeName else "") + labelName.joinToString("") { it.content }
            val tokenComponents = mutableListOf<TokenSeq.Component>()
            for (token in labelName) {
                tokenComponents.add(TokenSeq.Component.Specific(token.content))
            }
            tokenSeq = TokenSeq(*tokenComponents.toTypedArray(), ignoreSpaces = false)
        }
    }

    class E_DIRECTIVE(val type: DirType, val dot: Compiler.Token.Symbol, vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RV32Flags.directive), REFS.REF_E_DIRECTIVE, dot, *tokens) {

        fun isDataEmitting(): Boolean {
            return type.majorType == MajorType.DE_ALIGNED || type.majorType == MajorType.DE_UNALIGNED
        }

        enum class MajorType(val docName: String) {
            SECTIONSTART("Section identification"),
            DE_ALIGNED("Data emitting aligned"),
            DE_UNALIGNED("Data emitting unaligned")
        }

        enum class DirType(val dirname: String, val majorType: MajorType) {
            TEXT("text", MajorType.SECTIONSTART), DATA("data", MajorType.SECTIONSTART), RODATA("rodata", MajorType.SECTIONSTART), BSS("bss", MajorType.SECTIONSTART),

            BYTE("byte", MajorType.DE_ALIGNED), HALF("half", MajorType.DE_ALIGNED), WORD("word", MajorType.DE_ALIGNED), DWORD("dword", MajorType.DE_ALIGNED), ASCIZ("asciz", MajorType.DE_ALIGNED), STRING("string", MajorType.DE_ALIGNED),

            BYTE_2("2byte", MajorType.DE_UNALIGNED), BYTE_4("4byte", MajorType.DE_UNALIGNED), BYTE_8("8byte", MajorType.DE_UNALIGNED),
        }
    }

    /* -------------------------------------------------------------- ROWS -------------------------------------------------------------- */
    class R_SECSTART(val directive: E_DIRECTIVE) : TreeNode.RowNode(REFS.REF_R_SECSTART, directive)
    class R_JLBL(val label: E_LABEL, val isMainLabel: Boolean, val isGlobalStart: Boolean = false) : TreeNode.RowNode(REFS.REF_R_JLBL, label)
    class R_ULBL(val label: E_LABEL, val directive: E_DIRECTIVE) : TreeNode.RowNode(REFS.REF_R_ULBL, label, directive)
    class R_ILBL(val label: E_LABEL, val directive: E_DIRECTIVE, val paramcoll: E_PARAMCOLL) : TreeNode.RowNode(REFS.REF_R_ILBL, label, directive, paramcoll)
    class R_INSTR(val instrname: E_INSTRNAME, val paramcoll: E_PARAMCOLL?, val instrType: InstrType, val lastMainLabel: R_JLBL? = null, val globlStart: Boolean = false) : TreeNode.RowNode(REFS.REF_R_INSTR, instrname, paramcoll) {
        enum class ParamType(val pseudo: Boolean, val exampleString: String, val immMaxSize: Variable.Size? = null) {
            // NORMAL INSTRUCTIONS
            RD_I20(false, "rd, imm20", Bit20()) {
                override fun getTSParamString(regContainer: RegContainer, paramMap: MutableMap<RV32BinMapper.MaskLabel, Bin>, labelName: String): String {
                    val rd = paramMap.get(RD)
                    return if (rd != null) {
                        paramMap.remove(RD)
                        val immString = if (labelName.isEmpty()) "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString(" ") { it.toHex().getRawHexStr() }}" else labelName
                        "${regContainer.getReg(rd)?.aliases?.first()},\t$immString"
                    } else {
                        "param missing"
                    }
                }
            }, // rd, imm
            RD_Off12(false, "rd, imm12(rs)", Bit12()) {
                override fun getTSParamString(regContainer: RegContainer, paramMap: MutableMap<RV32BinMapper.MaskLabel, Bin>, labelName: String): String {
                    val rd = paramMap.get(RD)
                    val rs1 = paramMap.get(RS1)
                    return if (rd != null && rs1 != null) {
                        paramMap.remove(RD)
                        paramMap.remove(RS1)
                        val immString = if (labelName.isEmpty()) "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString(" ") { it.toHex().getRawHexStr() }}" else labelName
                        "${regContainer.getReg(rd)?.aliases?.first()},\t$immString(${regContainer.getReg(rs1)?.aliases?.first()})"
                    } else {
                        "param missing"
                    }
                }
            }, // rd, imm12(rs)
            RS2_Off5(false, "rs2, imm5(rs1)", Bit5()) {
                override fun getTSParamString(regContainer: RegContainer, paramMap: MutableMap<RV32BinMapper.MaskLabel, Bin>, labelName: String): String {
                    val rs2 = paramMap.get(RS2)
                    val rs1 = paramMap.get(RS1)
                    return if (rs2 != null && rs1 != null) {
                        paramMap.remove(RS2)
                        paramMap.remove(RS1)
                        val immString = if (labelName.isEmpty()) "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString(" ") { it.toHex().getRawHexStr() }}" else labelName
                        "${regContainer.getReg(rs2)?.aliases?.first()},\t$immString(${regContainer.getReg(rs1)?.aliases?.first()})"
                    } else {
                        "param missing"
                    }
                }
            }, // rs2, imm5(rs1)
            RD_RS1_RS1(false, "rd, rs1, rs2") {
                override fun getTSParamString(regContainer: RegContainer, paramMap: MutableMap<RV32BinMapper.MaskLabel, Bin>, labelName: String): String {
                    val rd = paramMap.get(RD)
                    val rs1 = paramMap.get(RS1)
                    val rs2 = paramMap.get(RS2)
                    return if (rd != null && rs2 != null && rs1 != null) {
                        paramMap.remove(RD)
                        paramMap.remove(RS2)
                        paramMap.remove(RS1)
                        "${regContainer.getReg(rd)?.aliases?.first()},\t${regContainer.getReg(rs1)?.aliases?.first()},\t${regContainer.getReg(rs2)?.aliases?.first()}"
                    } else {
                        "param missing"
                    }
                }
            }, // rd, rs1, rs2
            RD_RS1_I12(false, "rd, rs1, imm12", Bit12()) {
                override fun getTSParamString(regContainer: RegContainer, paramMap: MutableMap<RV32BinMapper.MaskLabel, Bin>, labelName: String): String {
                    val rd = paramMap.get(RD)
                    val rs1 = paramMap.get(RS1)
                    return if (rd != null && rs1 != null) {
                        paramMap.remove(RD)
                        paramMap.remove(RS1)
                        val immString = if (labelName.isEmpty()) "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString(" ") { it.toHex().getRawHexStr() }}" else labelName
                        "${regContainer.getReg(rd)?.aliases?.first()},\t${regContainer.getReg(rs1)?.aliases?.first()},\t$immString"
                    } else {
                        "param missing"
                    }
                }
            }, // rd, rs, imm
            RD_RS1_I5(false, "rd, rs1, shamt5", Bit5()) {
                override fun getTSParamString(regContainer: RegContainer, paramMap: MutableMap<RV32BinMapper.MaskLabel, Bin>, labelName: String): String {
                    val rd = paramMap.get(RD)
                    val rs1 = paramMap.get(RS1)
                    return if (rd != null && rs1 != null) {
                        paramMap.remove(RD)
                        paramMap.remove(RS1)
                        val immString = if (labelName.isEmpty()) "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString(" ") { it.toHex().getRawHexStr() }}" else labelName
                        "${regContainer.getReg(rd)?.aliases?.first()},\t${regContainer.getReg(rs1)?.aliases?.first()},\t$immString"
                    } else {
                        "param missing"
                    }
                }
            }, // rd, rs, shamt
            RS1_RS2_I12(false, "rs1, rs2, imm12", Bit12()) {
                override fun getTSParamString(regContainer: RegContainer, paramMap: MutableMap<RV32BinMapper.MaskLabel, Bin>, labelName: String): String {
                    val rs2 = paramMap.get(RS2)
                    val rs1 = paramMap.get(RS1)
                    return if (rs2 != null && rs1 != null) {
                        paramMap.remove(RS2)
                        paramMap.remove(RS1)
                        val immString = if (labelName.isEmpty()) "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString(" ") { it.toHex().getRawHexStr() }}" else labelName
                        "${regContainer.getReg(rs1)?.aliases?.first()},\t${regContainer.getReg(rs2)?.aliases?.first()},\t$immString"
                    } else {
                        "param missing"
                    }
                }
            }, // rs1, rs2, imm

            // PSEUDO INSTRUCTIONS
            PS_RS1_RS2_Jlbl(true, "rs1, rs2, jlabel"),
            PS_RD_I32(true, "rd, imm32", Bit32()), // rd, imm
            PS_RS1_Jlbl(true, "rs, jlabel"), // rs, label
            PS_RD_Albl(true, "rd, alabel"), // rd, label
            PS_Jlbl(true, "jlabel"),  // label
            PS_RD_RS1(true, "rd, rs"), // rd, rs
            PS_RS1(true, "rs1"),

            // NONE PARAM INSTR
            NONE(false, "none"), PS_NONE(true, "none");

            open fun getTSParamString(regContainer: RegContainer, paramMap: MutableMap<RV32BinMapper.MaskLabel, Bin>, labelName: String): String {
                return "pseudo param type"
            }
        }

        enum class InstrType(
            val id: String,
            val pseudo: Boolean,
            val paramType: ParamType,
            val opCode: RV32BinMapper.OpCode? = null,
            val memWords: Int = 1,
            val relative: InstrType? = null
        ) {
            LUI("LUI", false, ParamType.RD_I20, RV32BinMapper.OpCode("00000000000000000000 00000 0110111", arrayOf(IMM20, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap) // only for console information
                    // get relevant parameters from binary map
                    val rdAddr = paramMap[RD]
                    val imm20 = paramMap[IMM20]
                    if (rdAddr == null || imm20 == null) return

                    // get relevant registers
                    val rd = arch.getRegContainer().getReg(rdAddr)
                    val pc = arch.getRegContainer().pc
                    if (rd == null) return

                    // calculate
                    val shiftedIMM32 = imm20.getUResized(Bit32()) shl 12 // from imm20 to imm32

                    // change states
                    rd.set(shiftedIMM32)    // set register to imm32 value
                    pc.set(pc.get() + Hex("4"))
                }
            },
            AUIPC("AUIPC", false, ParamType.RD_I20, RV32BinMapper.OpCode("00000000000000000000 00000 0010111", arrayOf(IMM20, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    if (rdAddr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val imm20 = paramMap.get(IMM20)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && imm20 != null) {
                            val shiftedIMM32 = imm20.getUResized(Bit32()) shl 12
                            val sum = pc.get() + shiftedIMM32
                            rd.set(sum)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            JAL("JAL", false, ParamType.RD_I20, RV32BinMapper.OpCode("00000000000000000000 00000 1101111", arrayOf(IMM20, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    if (rdAddr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val imm20 = paramMap.get(IMM20)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && imm20 != null) {
                            val imm20str = imm20.getRawBinaryStr()

                            /**
                             *      RV32IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                             *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                             *        Location       20 [      10 : 1               ] 11 [ 19 : 12             ]
                             */

                            val shiftedImm = Bin(imm20str[0].toString() + imm20str.substring(12) + imm20str[11] + imm20str.substring(1, 11), Bit20()).getResized(Bit32()) shl 1

                            rd.set(pc.get() + Hex("4"))
                            pc.set(pc.get() + shiftedImm)
                        }
                    }
                }
            },
            JALR("JALR", false, ParamType.RD_Off12, RV32BinMapper.OpCode("000000000000 00000 000 00000 1100111", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val imm12 = paramMap.get(IMM12)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val jumpAddr = rs1.get() + imm12.getResized(Bit32())
                            rd.set(pc.get() + Hex("4"))
                            pc.set(jumpAddr)
                        }
                    }
                }
            },
            ECALL("ECALL", false, ParamType.NONE, RV32BinMapper.OpCode("000000000000 00000 000 00000 1110011", arrayOf(NONE, NONE, NONE, NONE, OPCODE))),
            EBREAK("EBREAK", false, ParamType.NONE, RV32BinMapper.OpCode("000000000001 00000 000 00000 1110011", arrayOf(NONE, NONE, NONE, NONE, OPCODE))),
            BEQ(
                "BEQ",
                false,
                ParamType.RS1_RS2_I12,
                RV32BinMapper.OpCode("0000000 00000 00000 000 00000 1100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rs1Addr = paramMap.get(RS1)
                    val rs2Addr = paramMap.get(RS2)
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = arch.getRegContainer().getReg(rs2Addr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val imm7 = paramMap.get(IMM7)
                        val imm5 = paramMap.get(IMM5)
                        val pc = arch.getRegContainer().pc
                        if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                            val imm7str = imm7.getResized(Bit7()).getRawBinaryStr()
                            val imm5str = imm5.getResized(Bit5()).getRawBinaryStr()
                            val imm12 = Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Bit12())

                            val offset = imm12.toBin().getResized(Bit32()) shl 1
                            if (rs1.get().toBin() == rs2.get().toBin()) {
                                pc.set(pc.get() + offset)
                            } else {
                                pc.set(pc.get() + Hex("4"))
                            }
                        }
                    }
                }
            },
            BNE(
                "BNE",
                false,
                ParamType.RS1_RS2_I12,
                RV32BinMapper.OpCode("0000000 00000 00000 001 00000 1100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rs1Addr = paramMap.get(RS1)
                    val rs2Addr = paramMap.get(RS2)
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = arch.getRegContainer().getReg(rs2Addr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val imm7 = paramMap.get(IMM7)
                        val imm5 = paramMap.get(IMM5)
                        val pc = arch.getRegContainer().pc
                        if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                            val imm7str = imm7.getResized(Bit7()).getRawBinaryStr()
                            val imm5str = imm5.getResized(Bit5()).getRawBinaryStr()
                            val imm12 = Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Bit12())
                            val offset = imm12.toBin().getResized(Bit32()) shl 1
                            if (rs1.get().toBin() != rs2.get().toBin()) {
                                pc.set(pc.get() + offset)
                            } else {
                                pc.set(pc.get() + Hex("4"))
                            }
                        }
                    }
                }
            },
            BLT(
                "BLT",
                false,
                ParamType.RS1_RS2_I12,
                RV32BinMapper.OpCode("0000000 00000 00000 100 00000 1100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rs1Addr = paramMap.get(RS1)
                    val rs2Addr = paramMap.get(RS2)
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = arch.getRegContainer().getReg(rs2Addr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val imm7 = paramMap.get(IMM7)
                        val imm5 = paramMap.get(IMM5)
                        val pc = arch.getRegContainer().pc
                        if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                            val imm7str = imm7.getResized(Bit7()).getRawBinaryStr()
                            val imm5str = imm5.getResized(Bit5()).getRawBinaryStr()
                            val imm12 = Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Bit12())
                            val offset = imm12.toBin().getResized(Bit32()) shl 1
                            if (rs1.get().toDec() < rs2.get().toDec()) {
                                pc.set(pc.get() + offset)
                            } else {
                                pc.set(pc.get() + Hex("4"))
                            }
                        }
                    }
                }
            },
            BGE(
                "BGE",
                false,
                ParamType.RS1_RS2_I12,
                RV32BinMapper.OpCode("0000000 00000 00000 101 00000 1100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rs1Addr = paramMap.get(RS1)
                    val rs2Addr = paramMap.get(RS2)
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = arch.getRegContainer().getReg(rs2Addr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val imm7 = paramMap.get(IMM7)
                        val imm5 = paramMap.get(IMM5)
                        val pc = arch.getRegContainer().pc
                        if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                            val imm7str = imm7.getResized(Bit7()).getRawBinaryStr()
                            val imm5str = imm5.getResized(Bit5()).getRawBinaryStr()
                            val imm12 = Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Bit12())
                            val offset = imm12.toBin().getResized(Bit32()) shl 1
                            if (rs1.get().toDec() >= rs2.get().toDec()) {
                                pc.set(pc.get() + offset)
                            } else {
                                pc.set(pc.get() + Hex("4"))
                            }
                        }
                    }
                }
            },
            BLTU(
                "BLTU",
                false,
                ParamType.RS1_RS2_I12,
                RV32BinMapper.OpCode("0000000 00000 00000 110 00000 1100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rs1Addr = paramMap.get(RS1)
                    val rs2Addr = paramMap.get(RS2)
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = arch.getRegContainer().getReg(rs2Addr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val imm7 = paramMap.get(IMM7)
                        val imm5 = paramMap.get(IMM5)
                        val pc = arch.getRegContainer().pc
                        if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                            val imm7str = imm7.getResized(Bit7()).getRawBinaryStr()
                            val imm5str = imm5.getResized(Bit5()).getRawBinaryStr()
                            val imm12 = Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Bit12())
                            val offset = imm12.toBin().getResized(Bit32()) shl 1
                            if (rs1.get().toUDec() < rs2.get().toUDec()) {
                                pc.set(pc.get() + offset)
                            } else {
                                pc.set(pc.get() + Hex("4"))
                            }
                        }
                    }
                }
            },
            BGEU(
                "BGEU",
                false,
                ParamType.RS1_RS2_I12,
                RV32BinMapper.OpCode("0000000 00000 00000 111 00000 1100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rs1Addr = paramMap.get(RS1)
                    val rs2Addr = paramMap.get(RS2)
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = arch.getRegContainer().getReg(rs2Addr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val imm7 = paramMap.get(IMM7)
                        val imm5 = paramMap.get(IMM5)
                        val pc = arch.getRegContainer().pc
                        if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                            val imm7str = imm7.getResized(Bit7()).getRawBinaryStr()
                            val imm5str = imm5.getResized(Bit5()).getRawBinaryStr()
                            val imm12 = Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Bit12())
                            val offset = imm12.toBin().getResized(Bit32()) shl 1
                            if (rs1.get().toUDec() >= rs2.get().toUDec()) {
                                pc.set(pc.get() + offset)
                            } else {
                                pc.set(pc.get() + Hex("4"))
                            }
                        }
                    }
                }
            },
            BEQ1("BEQ", true, ParamType.PS_RS1_RS2_Jlbl, relative = BEQ), BNE1("BNE", true, ParamType.PS_RS1_RS2_Jlbl, relative = BNE), BLT1("BLT", true, ParamType.PS_RS1_RS2_Jlbl, relative = BLT), BGE1("BGE", true, ParamType.PS_RS1_RS2_Jlbl, relative = BGE), BLTU1(
                "BLTU", true, ParamType.PS_RS1_RS2_Jlbl, relative = BLTU
            ),
            BGEU1("BGEU", true, ParamType.PS_RS1_RS2_Jlbl, relative = BGEU),
            LB(
                "LB", false, ParamType.RD_Off12, RV32BinMapper.OpCode("000000000000 00000 000 00000 0000011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    val imm12 = paramMap.get(IMM12)
                    if (rdAddr != null && rs1Addr != null && imm12 != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null) {
                            val memAddr = rs1.get().toBin() + imm12.getResized(Bit32())
                            val loadedByte = arch.getMemory().load(memAddr).toBin().getResized(Bit32())
                            rd.set(loadedByte)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            LH(
                "LH", false, ParamType.RD_Off12, RV32BinMapper.OpCode("000000000000 00000 001 00000 0000011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    val imm12 = paramMap.get(IMM12)
                    if (rdAddr != null && rs1Addr != null && imm12 != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null) {
                            val memAddr = rs1.get().toBin() + imm12.getResized(Bit32())
                            val loadedByte = arch.getMemory().load(memAddr, 2).toBin().getResized(Bit32())
                            rd.set(loadedByte)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            LW(
                "LW", false, ParamType.RD_Off12, RV32BinMapper.OpCode("000000000000 00000 010 00000 0000011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    val imm12 = paramMap.get(IMM12)
                    if (rdAddr != null && rs1Addr != null && imm12 != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null) {
                            val memAddr = rs1.get().toBin() + imm12.getResized(Bit32())
                            val loadedByte = arch.getMemory().load(memAddr, 4)
                            rd.set(loadedByte)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            LBU(
                "LBU", false, ParamType.RD_Off12, RV32BinMapper.OpCode("000000000000 00000 100 00000 0000011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    val imm12 = paramMap.get(IMM12)
                    if (rdAddr != null && rs1Addr != null && imm12 != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null) {
                            val memAddr = rs1.get().toBin() + imm12.getResized(Bit32())
                            val loadedByte = arch.getMemory().load(memAddr)
                            rd.set(Bin(rd.get().toBin().getRawBinaryStr().substring(0, 24) + loadedByte.toBin().getRawBinaryStr(), Bit32()))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            LHU(
                "LHU", false, ParamType.RD_Off12, RV32BinMapper.OpCode("000000000000 00000 101 00000 0000011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    val imm12 = paramMap.get(IMM12)
                    if (rdAddr != null && rs1Addr != null && imm12 != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null) {
                            val memAddr = rs1.get().toBin() + imm12.getResized(Bit32())
                            val loadedByte = arch.getMemory().load(memAddr, 2)
                            rd.set(Bin(rd.get().toBin().getRawBinaryStr().substring(0, 16) + loadedByte.toBin().getRawBinaryStr(), Bit32()))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SB(
                "SB",
                false,
                ParamType.RS2_Off5,
                RV32BinMapper.OpCode("0000000 00000 00000 000 00000 0100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rs1Addr = paramMap.get(RS1)
                    val rs2Addr = paramMap.get(RS2)
                    val imm5 = paramMap.get(IMM5)
                    val imm7 = paramMap.get(IMM7)
                    if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val rs2 = arch.getRegContainer().getReg(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rs1 != null && rs2 != null) {
                            val off32 = (imm7.getResized(Bit32()) shl 5) + imm5
                            val memAddr = rs1.get().toBin().getResized(Bit32()) + off32
                            arch.getMemory().store(memAddr, rs2.get().toBin().getResized(Bit8()))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SH(
                "SH",
                false,
                ParamType.RS2_Off5,
                RV32BinMapper.OpCode("0000000 00000 00000 001 00000 0100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rs1Addr = paramMap.get(RS1)
                    val rs2Addr = paramMap.get(RS2)
                    val imm5 = paramMap.get(IMM5)
                    val imm7 = paramMap.get(IMM7)
                    if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val rs2 = arch.getRegContainer().getReg(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rs1 != null && rs2 != null) {
                            val off32 = (imm7.getResized(Bit32()) shl 5) + imm5
                            val memAddr = rs1.get().toBin().getResized(Bit32()) + off32
                            arch.getMemory().store(memAddr, rs2.get().toBin().getResized(Bit16()))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SW(
                "SW",
                false,
                ParamType.RS2_Off5,
                RV32BinMapper.OpCode("0000000 00000 00000 010 00000 0100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rs1Addr = paramMap.get(RS1)
                    val rs2Addr = paramMap.get(RS2)
                    val imm5 = paramMap.get(IMM5)
                    val imm7 = paramMap.get(IMM7)
                    if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val rs2 = arch.getRegContainer().getReg(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rs1 != null && rs2 != null) {
                            val off32 = (imm7.getResized(Bit32()) shl 5) + imm5
                            val memAddr = rs1.variable.get().toBin().getResized(Bit32()) + off32
                            arch.getMemory().store(memAddr, rs2.get().toBin().getResized(Bit32()))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            ADDI(
                "ADDI", false, ParamType.RD_RS1_I12, RV32BinMapper.OpCode("000000000000 00000 000 00000 0010011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val imm12 = paramMap.get(IMM12)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm32 = imm12.getResized(Bit32())
                            val sum = rs1.get().toBin() + paddedImm32
                            rd.set(sum)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SLTI(
                "SLTI", false, ParamType.RD_RS1_I12, RV32BinMapper.OpCode("000000000000 00000 010 00000 0010011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val imm12 = paramMap.get(IMM12)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm32 = imm12.getResized(Bit32())
                            rd.set(if (rs1.get().toDec() < paddedImm32.toDec()) Bin("1", Bit32()) else Bin("0", Bit32()))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SLTIU(
                "SLTIU", false, ParamType.RD_RS1_I12, RV32BinMapper.OpCode("000000000000 00000 011 00000 0010011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val imm12 = paramMap.get(IMM12)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm32 = imm12.getUResized(Bit32())
                            rd.set(if (rs1.get().toBin() < paddedImm32) Bin("1", Bit32()) else Bin("0", Bit32()))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            XORI(
                "XORI", false, ParamType.RD_RS1_I12, RV32BinMapper.OpCode("000000000000 00000 100 00000 0010011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val imm12 = paramMap.get(IMM12)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm32 = imm12.getUResized(Bit32())
                            rd.set(rs1.get().toBin() xor paddedImm32)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            ORI(
                "ORI", false, ParamType.RD_RS1_I12, RV32BinMapper.OpCode("000000000000 00000 110 00000 0010011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val imm12 = paramMap.get(IMM12)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm32 = imm12.getUResized(Bit32())
                            rd.set(rs1.get().toBin() or paddedImm32)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            ANDI(
                "ANDI", false, ParamType.RD_RS1_I12, RV32BinMapper.OpCode("000000000000 00000 111 00000 0010011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val imm12 = paramMap.get(IMM12)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm32 = imm12.getUResized(Bit32())
                            rd.set(rs1.get().toBin() and paddedImm32)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SLLI(
                "SLLI",
                false,
                ParamType.RD_RS1_I5,
                RV32BinMapper.OpCode("0000000 00000 00000 001 00000 0010011", arrayOf(FUNCT7, SHAMT, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val shamt = paramMap.get(SHAMT)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && shamt != null && rs1 != null) {
                            rd.set(rs1.get().toBin() ushl shamt.getRawBinaryStr().toInt(2))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SRLI(
                "SRLI",
                false,
                ParamType.RD_RS1_I5,
                RV32BinMapper.OpCode("0000000 00000 00000 101 00000 0010011", arrayOf(FUNCT7, SHAMT, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val shamt = paramMap.get(SHAMT)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && shamt != null && rs1 != null) {
                            rd.set(rs1.get().toBin() ushr shamt.getRawBinaryStr().toInt(2))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SRAI(
                "SRAI",
                false,
                ParamType.RD_RS1_I5,
                RV32BinMapper.OpCode("0100000 00000 00000 101 00000 0010011", arrayOf(FUNCT7, SHAMT, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val shamt = paramMap.get(SHAMT)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && shamt != null && rs1 != null) {
                            rd.set(rs1.get().toBin() shr shamt.getRawBinaryStr().toInt(2))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            ADD(
                "ADD",
                false,
                ParamType.RD_RS1_RS1,
                RV32BinMapper.OpCode("0000000 00000 00000 000 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    val rs2Addr = paramMap.get(RS2)
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val rs2 = arch.getRegContainer().getReg(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() + rs2.get().toBin())
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SUB(
                "SUB",
                false,
                ParamType.RD_RS1_RS1,
                RV32BinMapper.OpCode("0100000 00000 00000 000 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    val rs2Addr = paramMap.get(RS2)
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val rs2 = arch.getRegContainer().getReg(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() - rs2.get().toBin())
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SLL(
                "SLL",
                false,
                ParamType.RD_RS1_RS1,
                RV32BinMapper.OpCode("0000000 00000 00000 001 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    val rs2Addr = paramMap.get(RS2)
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val rs2 = arch.getRegContainer().getReg(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() ushl rs2.get().toBin().getRawBinaryStr().toInt(2))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SLT(
                "SLT",
                false,
                ParamType.RD_RS1_RS1,
                RV32BinMapper.OpCode("0000000 00000 00000 010 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    val rs2Addr = paramMap.get(RS2)
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val rs2 = arch.getRegContainer().getReg(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(if (rs1.get().toDec() < rs2.get().toDec()) Bin("1", Bit32()) else Bin("0", Bit32()))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SLTU(
                "SLTU",
                false,
                ParamType.RD_RS1_RS1,
                RV32BinMapper.OpCode("0000000 00000 00000 011 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    val rs2Addr = paramMap.get(RS2)
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val rs2 = arch.getRegContainer().getReg(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(if (rs1.get().toBin() < rs2.get().toBin()) Bin("1", Bit32()) else Bin("0", Bit32()))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            XOR(
                "XOR",
                false,
                ParamType.RD_RS1_RS1,
                RV32BinMapper.OpCode("0000000 00000 00000 100 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    val rs2Addr = paramMap.get(RS2)
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val rs2 = arch.getRegContainer().getReg(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() xor rs2.get().toBin())
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SRL(
                "SRL",
                false,
                ParamType.RD_RS1_RS1,
                RV32BinMapper.OpCode("0000000 00000 00000 101 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    val rs2Addr = paramMap.get(RS2)
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val rs2 = arch.getRegContainer().getReg(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() ushr rs2.get().toBin().getRawBinaryStr().toInt(2))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SRA(
                "SRA",
                false,
                ParamType.RD_RS1_RS1,
                RV32BinMapper.OpCode("0100000 00000 00000 101 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    val rs2Addr = paramMap.get(RS2)
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val rs2 = arch.getRegContainer().getReg(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() shr rs2.get().toBin().getRawBinaryStr().toInt(2))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            OR(
                "OR",
                false,
                ParamType.RD_RS1_RS1,
                RV32BinMapper.OpCode("0000000 00000 00000 110 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    val rs2Addr = paramMap.get(RS2)
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val rs2 = arch.getRegContainer().getReg(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() or rs2.get().toBin())
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            AND(
                "AND",
                false,
                ParamType.RD_RS1_RS1,
                RV32BinMapper.OpCode("0000000 00000 00000 111 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))
            ) {
                override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap.get(RD)
                    val rs1Addr = paramMap.get(RS1)
                    val rs2Addr = paramMap.get(RS2)
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegContainer().getReg(rdAddr)
                        val rs1 = arch.getRegContainer().getReg(rs1Addr)
                        val rs2 = arch.getRegContainer().getReg(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() and rs2.get().toBin())
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            Nop("NOP", true, ParamType.PS_NONE), Mv("MV", true, ParamType.PS_RD_RS1), Li("LI", true, ParamType.PS_RD_I32, memWords = 2), La("LA", true, ParamType.PS_RD_Albl, memWords = 2), Not("NOT", true, ParamType.PS_RD_RS1), Neg("NEG", true, ParamType.PS_RD_RS1), Seqz(
                "SEQZ", true, ParamType.PS_RD_RS1
            ),
            Snez("SNEZ", true, ParamType.PS_RD_RS1), Sltz("SLTZ", true, ParamType.PS_RD_RS1), Sgtz("SGTZ", true, ParamType.PS_RD_RS1), Beqz("BEQZ", true, ParamType.PS_RS1_Jlbl), Bnez("BNEZ", true, ParamType.PS_RS1_Jlbl), Blez("BLEZ", true, ParamType.PS_RS1_Jlbl), Bgez(
                "BGEZ", true, ParamType.PS_RS1_Jlbl
            ),
            Bltz("BLTZ", true, ParamType.PS_RS1_Jlbl), BGTZ("BGTZ", true, ParamType.PS_RS1_Jlbl), Bgt("BGT", true, ParamType.PS_RS1_RS2_Jlbl), Ble("BLE", true, ParamType.PS_RS1_RS2_Jlbl), Bgtu("BGTU", true, ParamType.PS_RS1_RS2_Jlbl), Bleu("BLEU", true, ParamType.PS_RS1_RS2_Jlbl), J(
                "J", true, ParamType.PS_Jlbl
            ),
            JAL1("JAL", true, ParamType.PS_RS1_Jlbl, relative = JAL),
            JAL2("JAL", true, ParamType.PS_Jlbl, relative = JAL),
            Jr("JR", true, ParamType.PS_RS1),
            JALR1("JALR", true, ParamType.PS_RS1, relative = JALR),
            Ret("RET", true, ParamType.PS_NONE),
            Call("CALL", true, ParamType.PS_Jlbl, memWords = 2),
            Tail("TAIL", true, ParamType.PS_Jlbl, memWords = 2);

            open fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                arch.getConsole().info("executing $id ...")
            }
        }
    }

    /* -------------------------------------------------------------- SECTIONS -------------------------------------------------------------- */
    class S_TEXT(val secStart: R_SECSTART?, vararg val sectionContent: RowNode) : TreeNode.SectionNode(REFS.REF_S_TEXT, collNodes = if (secStart != null) sectionContent + secStart else sectionContent)
    class S_DATA(val secStart: R_SECSTART, vararg val sectionContent: RowNode) : TreeNode.SectionNode(REFS.REF_S_DATA, secStart, *sectionContent)
    class S_RODATA(val secStart: R_SECSTART, vararg val sectionContent: RowNode) : TreeNode.SectionNode(REFS.REF_S_RODATA, secStart, *sectionContent)
    class S_BSS(val secStart: R_SECSTART, vararg val sectionContent: RowNode) : TreeNode.SectionNode(REFS.REF_S_BSS, secStart, *sectionContent)

    /* -------------------------------------------------------------- CONTAINER -------------------------------------------------------------- */
    class C_SECTIONS(vararg val sections: SectionNode) : TreeNode.ContainerNode(REFS.REF_C_SECTIONS, *sections)
    class C_PRES(vararg val preElements: ElementNode) : TreeNode.ContainerNode(REFS.REF_C_PRES, *preElements)

    /* -------------------------------------------------------------- PRE DATA CLASSES -------------------------------------------------------------- */
    data class EquDefinition(val name: List<Compiler.Token>, val constant: Compiler.Token) {
        val nameSequence: TokenSeq

        init {
            val sequenceComponents = name.map { TokenSeq.Component.Specific(it.content) }
            nameSequence = TokenSeq(*sequenceComponents.toTypedArray())
        }
    }

    data class MacroDefinition(val name: String, val arguments: List<String>, val replacementLines: List<String>) {
        init {
            console.log("$name $arguments\n$replacementLines")
        }
    }

    /**
     * FOR TRANSCRIPT
     */
    class RVCompiledRow(addr: Hex) : Transcript.Row(addr) {
        val content = RV32.TS_COMPILED_HEADERS.entries.associateWith { Entry(Orientation.CENTER, "") }.toMutableMap()

        init {
            content[RV32.TS_COMPILED_HEADERS.Address] = Entry(Orientation.CENTER, getAddresses().first().toHex().getRawHexStr())
        }

        fun addLabel(label: R_JLBL) {
            content[RV32.TS_COMPILED_HEADERS.Label] = Entry(Orientation.LEFT, label.label.wholeName)
        }

        fun addInstr(instr: R_INSTR) {
            content[RV32.TS_COMPILED_HEADERS.Instruction] = Entry(Orientation.LEFT, "${instr.instrType.id}${if (instr.instrType.pseudo && instr.instrType.relative == null) "\t(pseudo)" else ""}")
            content[RV32.TS_COMPILED_HEADERS.Parameters] = Entry(Orientation.LEFT, instr.paramcoll?.paramsWithOutSplitSymbols?.joinToString(",\t") { it.paramTokens.joinToString("") { it.content } } ?: "")
        }

        override fun getContent(): List<Entry> {
            return RV32.TS_COMPILED_HEADERS.entries.map { content[it] ?: Entry(Orientation.CENTER, "") }
        }
    }

}