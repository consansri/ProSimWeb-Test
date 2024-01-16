package emulator.archs.riscv64

import debug.DebugTools
import emulator.archs.riscv64.RV64Syntax.E_DIRECTIVE.DirType.*
import emulator.kit.Architecture
import emulator.kit.assembly.Compiler
import emulator.kit.assembly.Syntax
import emulator.kit.common.FileHandler
import emulator.kit.common.Transcript
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*
import emulator.kit.types.Variable.Size.*
import emulator.archs.riscv64.RV64BinMapper.MaskLabel.*
import emulator.archs.riscv64.RV64BinMapper.OpCode
import emulator.kit.optional.Feature

class RV64Syntax : Syntax() {

    override val applyStandardHLForRest: Boolean = false

    override fun clear() {

    }

    override fun check(arch: Architecture, compiler: Compiler, tokens: List<Compiler.Token>,tokenLines: List<List<Compiler.Token>>, others: List<FileHandler.File>, transcript: Transcript): SyntaxTree {

        /**
         *  -------------------------------------------------------------- GLOBAL LISTS --------------------------------------------------------------
         *  usage:
         */

        // List which holds the actual state after the actuall scans
        val remainingLines = tokenLines.toMutableList()

        // For Root Node
        val errors: MutableList<Error> = mutableListOf()
        val warnings: MutableList<Warning> = mutableListOf()

        val imports: MutableList<TreeNode.SectionNode> = mutableListOf()
        var globalStart: Pre_GLOBAL? = null

        val cSections: C_SECTIONS
        val cPres: C_PRES

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
                            errors.add(Error("File {filename: $filename} has errors!", *remainingLines[lineID].toTypedArray()))
                        } else {
                            val linkedcSection = root.containers.filterIsInstance<C_SECTIONS>()
                            val linkedSections: List<TreeNode.SectionNode> = linkedcSection.flatMap { it.nodes.toList() }.filterIsInstance<TreeNode.SectionNode>()

                            imports.addAll(linkedSections)
                            pres.add(Pre_IMPORT(*remainingLines[lineID].toTypedArray()))
                        }
                    }
                } else {
                    errors.add(Error("File {filename: $filename} not ${if (linkedFile != null) "compiled" else "found"}!", *remainingLines[lineID].toTypedArray()))
                }
                remainingLines[lineID] = emptyList()
            }
        }

        if (DebugTools.RV64_showGrammarScanTiers) {
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
                    val labelName = it.groups[SyntaxRegex.PRE_GLOBALSTART_CONTENTGROUP]?.value ?: ""
                    if (globalStart == null && labelName.isNotEmpty()) {
                        val directives = remainingLines[lineID].filter { it.content == "." || it.content == "global" || it.content == "globl" }
                        val elseTokens = remainingLines[lineID] - directives.toSet()
                        globalStart = Pre_GLOBAL(ConnectedHL(Pair(RV64Flags.directive, directives), Pair(RV64Flags.pre_global, elseTokens)), *remainingLines[lineID].toTypedArray(), labelName = labelName)
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
                val name = compiler.pseudoTokenize(it.groupValues[2])
                val const = compiler.pseudoTokenize(it.groupValues[3])
                try {
                    val constMatch = (const.size == 1 && const.first() is Compiler.Token.Constant)
                    if (constMatch) {
                        equs.add(EquDefinition(name, const.first()))
                        val directiveTokens = remainingLines[lineID].filter { it.content == "." || it.content == "equ" }
                        val constant = remainingLines[lineID].filter { it.content != "." && it.content != "equ" && it is Compiler.Token.Constant }
                        val elseTokens = remainingLines[lineID].filter { it.content != "," && !directiveTokens.contains(it) && !constant.contains(it) }
                        pres.add(Pre_EQU(ConnectedHL(Pair(RV64Flags.directive, directiveTokens), Pair(RV64Flags.constant, constant), Pair(RV64Flags.pre_equ, elseTokens)), *remainingLines[lineID].toTypedArray()))
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
                        val newLineTokens = compiler.pseudoTokenize(newLineContent, tokenLineID)
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
                    if (macros.map { it.name }.contains(name)) {
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
                    val constants = macroTokens.filterIsInstance<Compiler.Token.Constant>()
                    val registers = macroTokens.filterIsInstance<Compiler.Token.Register>()
                    val instructions = macroTokens.filter { R_INSTR.InstrType.entries.map { it.id.uppercase() }.contains(it.content.uppercase()) }
                    val elseTokens = (macroTokens - directiveTokens.toSet() - constants.toSet() - registers.toSet() - instructions.toSet()).filter { it.content != "," }

                    pres.add(Pre_MACRO(ConnectedHL(Pair(RV64Flags.directive, directiveTokens), Pair(RV64Flags.constant, constants), Pair(RV64Flags.register, registers), Pair(RV64Flags.instruction, instructions), Pair(RV64Flags.pre_macro, elseTokens)), *macroTokens.toTypedArray()))

                    for (argusage in useMap) {
                        if (!argusage.value) {
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
                        val registers = remainingLines[macroLineID].filterIsInstance<Compiler.Token.Register>()
                        val constants = remainingLines[macroLineID].filterIsInstance<Compiler.Token.Constant>()
                        val elseTokens = (remainingLines[macroLineID] - registers.toSet() - constants.toSet()).filter { it.content != "," }

                        pres.add(Pre_MACRO(ConnectedHL(Pair(RV64Flags.register, registers), Pair(RV64Flags.constant, constants), Pair(RV64Flags.pre_macro, elseTokens)), *remainingLines[macroLineID].toTypedArray()))
                        remainingLines.removeAt(macroLineID)

                        for (macroLine in macro.replacementLines.reversed()) {
                            var replacedLine = macroLine
                            for (attrID in macro.arguments.indices) {
                                replacedLine = replacedLine.replace("""\""" + macro.arguments[attrID], argumentContent[attrID])
                            }
                            if (DebugTools.RV64_showGrammarScanTiers) {
                                console.log("\tmacro insert line ${macroLineID + 1}: $replacedLine")
                            }
                            remainingLines.add(macroLineID, compiler.pseudoTokenize(replacedLine))
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
        if (DebugTools.RV64_showGrammarScanTiers) {
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
                                when (val prevToken = remainingTokens[previous]) {
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
                                        remainingLines[superLabelsToCheck.last().getAllTokens().first().lineLoc.lineID].forEach { if (it.content == "equ") remainingLineElements.add(it) }
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
                                if (label.wholeName == (sublabelFrom?.wholeName ?: "") + labelNameString) {
                                    alreadyDefined = true
                                }
                            }
                            for (label in imports.flatMap { it.collNodes.toList() }.flatMap { it.elementNodes.toList() }.filterIsInstance<E_LABEL>().toList()) {
                                if (label.wholeName == (sublabelFrom?.wholeName ?: "") + labelNameString) {
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
                val ordinal = typeMap[dirName.lowercase()]
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
                                val missingFeatures = mutableListOf<Feature>()
                                for (neededFeatureID in type.needFeatures) {
                                    val feature = arch.getAllFeatures().firstOrNull { it.id == neededFeatureID }
                                    if (feature != null && !feature.isActive()) {
                                        missingFeatures.add(feature)
                                    }
                                }
                                if (missingFeatures.isEmpty()) {
                                    validTypes.add(type)
                                } else {
                                    arch.getConsole().missingFeature("$type can only be used with ${missingFeatures.joinToString(", ") { it.name }} active!")
                                }
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
                        val constant = offsetResult.sequenceMap[0]
                        val lParan = offsetResult.sequenceMap[1]
                        val reg = offsetResult.sequenceMap[2]
                        val rParan = offsetResult.sequenceMap[3]
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

        if (DebugTools.RV64_showGrammarScanTiers) {
            console.log("Grammar: ELEMENTS Scan -> ${elements.filter { it.isNotEmpty() }.joinToString("") {element -> "\n\tline ${elements.indexOf(element) + 1}: " + element.joinToString(" ") { it.name } }}")
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
                    if (eDir.isDataEmitting() && eParamcoll.paramsWithOutSplitSymbols.all { it is E_PARAM.Constant }) {
                        rowNode = R_ILBL(eLabel, eDir, eParamcoll)
                        rows[lineID] = rowNode
                    } else {
                        errors.add(Error((if (!eDir.isDataEmitting()) "Not a data emitting directive for" else "Invalid parameter count for") + " initialized label!", nodes = lineElements.toTypedArray()))
                    }
                    result.error?.let {
                        errors.add(it)
                    }
                    elements[lineID] = mutableListOf()
                    continue
                }
            }

            result = RowSeqs.dataemitting.exacltyMatches(*lineElements.toTypedArray())
            if (result.matches) {
                if (result.matchingTreeNodes.size == 2) {
                    val eDir = result.matchingTreeNodes[0] as E_DIRECTIVE
                    val eParamcoll = result.matchingTreeNodes[1] as E_PARAMCOLL
                    if (eDir.isDataEmitting() && eParamcoll.paramsWithOutSplitSymbols.all { it is E_PARAM.Constant }) {
                        rowNode = R_DATAEMITTING(eDir, eParamcoll)
                        rows[lineID] = rowNode
                    } else {
                        errors.add(Error(if (!eDir.isDataEmitting()) "Not a data emitting directive!" else "Invalid parameter count!", nodes = lineElements.toTypedArray()))
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
                        errors.add(Error("Not a data emitting directive for uninitialized label!", nodes = lineElements.toTypedArray()))
                    }
                    result.error?.let {
                        errors.add(it)
                    }
                    elements[lineID] = mutableListOf()
                    continue
                }
            }

            result = RowSeqs.jlabel.matches(*lineElements.toTypedArray())
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
                    elements[lineID] = result.remainingTreeNodes?.toMutableList() ?: mutableListOf()
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
                    errors.add(Error("Found directive type which shouldn't indicate a section start!", *lineElements.toTypedArray()))
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
                            for (importRowNode in imports.flatMap { it.collNodes.toList() }.toList()) {
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
                            for (importRowNode in imports.flatMap { it.collNodes.toList() }.toList()) {
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
                val checkedType = eInstrName.check(arch, eParamcoll)
                if (checkedType != null) {
                    rowNode = R_INSTR(eInstrName, eParamcoll, checkedType)
                    val rowContent = rows.getOrNull(lineID)
                    if (rowContent != null) {
                        if (rowContent is R_JLBL) {
                            rowContent.addInlineInstr(rowNode)
                        } else {
                            errors.add(Error("${rowNode.name} can't be after ${rowContent.name}!", rowNode))
                        }
                    } else {
                        rows[lineID] = rowNode
                    }
                    result.error?.let {
                        errors.add(it)
                    }
                } else {
                    errors.add(Error("Couldn't match parameters to any instruction!\nexpected parameters: ${eInstrName.types.joinToString(" or ") { it.paramType.exampleString }}", *lineElements.toTypedArray()))
                }
                elements[lineID] = mutableListOf()
                continue
            }

            result = RowSeqs.instrWithoutParams.exacltyMatches(*lineElements.toTypedArray())
            if (result.matches) {
                val eInstrName = result.matchingTreeNodes[0] as E_INSTRNAME

                // check params
                val checkedType = eInstrName.check(arch)
                if (checkedType != null) {
                    rowNode = R_INSTR(eInstrName, null, checkedType)
                    val rowContent = rows.getOrNull(lineID)
                    if (rowContent != null) {
                        if (rowContent is R_JLBL) {
                            rowContent.addInlineInstr(rowNode)
                        } else {
                            errors.add(Error("${rowNode.name} can't be after ${rowContent.name}!", rowNode))
                        }
                    } else {
                        rows[lineID] = rowNode
                    }
                } else {
                    errors.add(Error("Couldn't match parameters to any instruction!\nexpected parameters: ${eInstrName.types.joinToString(" or ") { it.paramType.exampleString }}", *lineElements.toTypedArray()))
                }
                elements[lineID] = mutableListOf()
                continue
            }

            if (lineElements.isNotEmpty()) {
                errors.add(Error("couldn't match Elements to RiscV Row!", *lineElements.toTypedArray()))
            }
        }


        globalStart?.let {
            errors.add(Error("Global start '${it.labelName}' label not found!", it))
        }
        /**
         * FINISH ROW SCAN
         */
        if (DebugTools.RV64_showGrammarScanTiers) {
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

                is R_DATAEMITTING -> {
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
                errors.add(Error("Couldn't match row into section!", firstRow))
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
        if (DebugTools.RV64_showGrammarScanTiers) {
            console.log("Grammar: SECTIONS Scan -> ${sections.joinToString("") { "\n\tsection ${sections.indexOf(it) + 1}: ${it.name}" }}")
        }
        /**
         *  -------------------------------------------------------------- CONTAINER SCAN --------------------------------------------------------------
         *  usage:
         */
        cPres = C_PRES(*pres.toTypedArray())
        cSections = C_SECTIONS(*sections.toTypedArray(), *imports.toTypedArray())

        /**
         * FINISH CONTAINER SCAN
         */

        /**
         * Pass Instructions and JLabels to Transcript
         */
        val compiledTSRows = mutableListOf<RVCompiledRow>()
        var address: Variable.Value = Hex("0", RV64.MEM_ADDRESS_WIDTH)
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

        val rootNode = TreeNode.RootNode(errors, warnings, cSections, cPres)

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
        val pre_equ_def = Regex("""^\s*(\.equ\s+(\S+)\s*,\s*(\S+))\s*?""")
        val pre_option = Regex("""^\s*(\.option\s+.+)\s*?""")
        val pre_attribute = Regex("""^\s*(\.attribute\s+.+)\s*?""")
        val pre_globalStart = listOf(Regex("""^\s*(\.global\s+(?<labelName>\S+))\s*?"""), Regex("""^\s*(\.globl\s+(?<labelName>\S+))\s*?"""))
        const val PRE_GLOBALSTART_CONTENTGROUP = "labelName"

        val pre_unresolvedList = listOf(Regex("""^\s*(fence\.i)\s*?"""))
    }

    object RowSeqs {
        val jlabel = RowSeq(RowSeq.Component(REFS.REF_E_LABEL))
        val ulabel = RowSeq(RowSeq.Component(REFS.REF_E_LABEL), RowSeq.Component(REFS.REF_E_DIRECTIVE))
        val ilabel = RowSeq(RowSeq.Component(REFS.REF_E_LABEL), RowSeq.Component(REFS.REF_E_DIRECTIVE), RowSeq.Component(REFS.REF_E_PARAMCOLL))
        val dataemitting = RowSeq(RowSeq.Component(REFS.REF_E_DIRECTIVE), RowSeq.Component(REFS.REF_E_PARAMCOLL))

        val directive = RowSeq(RowSeq.Component(REFS.REF_E_DIRECTIVE))

        val instrWithoutParams = RowSeq(RowSeq.Component(REFS.REF_E_INSTRNAME))
        val instrWithParams = RowSeq(RowSeq.Component(REFS.REF_E_INSTRNAME), RowSeq.Component(REFS.REF_E_PARAMCOLL))
    }

    object SyntaxInfo {
        val pre_map = mapOf(
            ".equ" to ".equ [name], [value]", ".macro" to "\n.macro [name] [attr1, ..., attrn]\n\t[macro usage referencing attribs with starting '\\']\n\t...\n.endm\n"
        )
    }

    data object REFS {
        const val REF_PRE_IMPORT = "PRE_import"
        const val REF_PRE_COMMENT = "PRE_comment"
        const val REF_PRE_UNRESOLVED = "PRE_unresolved"
        const val REF_PRE_OPTION = "PRE_option"
        const val REF_PRE_GLOBAL = "PRE_global"
        const val REF_PRE_ATTRIBUTE = "PRE_attribute"
        const val REF_PRE_MACRO = "PRE_macro"
        const val REF_PRE_EQU = "PRE_equ"

        const val REF_E_INSTRNAME = "E_instrname"
        const val REF_E_PARAMCOLL = "E_paramcoll"
        const val REF_E_PARAM_OFFSET = "E_offset"
        const val REF_E_PARAM_CONSTANT = "E_constant"
        const val REF_E_PARAM_REGISTER = "E_register"
        const val REF_E_PARAM_LABELLINK = "E_labellink"
        const val REF_E_PARAM_SPLITSYMBOL = "E_splitsymbol"
        const val REF_E_LABEL = "E_label"
        const val REF_E_DIRECTIVE = "E_directive"

        const val REF_R_SECSTART = "R_secstart"
        const val REF_R_INSTR = "R_instr"
        const val REF_R_JLBL = "R_jlbl"
        const val REF_R_ILBL = "R_ilbl"
        const val REF_R_ULBL = "R_ulbl"
        const val REF_R_DATAEMITTING = "R_dataemitting"

        const val REF_S_TEXT = "S_text"
        const val REF_S_DATA = "S_data"
        const val REF_S_RODATA = "S_rodata"
        const val REF_S_BSS = "S_bss"

        const val REF_C_SECTIONS = "C_sections"
        const val REF_C_PRES = "C_pres"
    }

    /* -------------------------------------------------------------- PRES -------------------------------------------------------------- */

    class Pre_IMPORT(vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RV64Flags.pre_import), REFS.REF_PRE_IMPORT, *tokens)
    class Pre_COMMENT(vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RV64Flags.comment), REFS.REF_PRE_COMMENT, *tokens)
    class Pre_OPTION(vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RV64Flags.pre_option), REFS.REF_PRE_OPTION, *tokens)
    class Pre_ATTRIBUTE(vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RV64Flags.pre_attribute), REFS.REF_PRE_ATTRIBUTE, *tokens)
    class Pre_GLOBAL(connectedHL: ConnectedHL = ConnectedHL(RV64Flags.pre_global), vararg tokens: Compiler.Token, val labelName: String) : TreeNode.ElementNode(connectedHL, REFS.REF_PRE_GLOBAL, *tokens)
    class Pre_MACRO(connectedHL: ConnectedHL = ConnectedHL(RV64Flags.pre_macro), vararg tokens: Compiler.Token) : TreeNode.ElementNode(connectedHL, REFS.REF_PRE_MACRO, *tokens)
    class Pre_EQU(connectedHL: ConnectedHL = ConnectedHL(RV64Flags.pre_equ), vararg tokens: Compiler.Token) : TreeNode.ElementNode(connectedHL, REFS.REF_PRE_EQU, *tokens)
    class Pre_UNRESOLVED(vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RV64Flags.pre_unresolved), REFS.REF_PRE_UNRESOLVED, *tokens)

    /* -------------------------------------------------------------- ELEMENTS -------------------------------------------------------------- */
    class E_INSTRNAME(val insToken: Compiler.Token.Word, vararg val types: R_INSTR.InstrType) : TreeNode.ElementNode(ConnectedHL(RV64Flags.instruction), REFS.REF_E_INSTRNAME, insToken) {
        private fun matchesSize(paramcoll: E_PARAMCOLL = E_PARAMCOLL(), instrType: R_INSTR.InstrType): Variable.CheckResult? {
            try {
                val immMax = instrType.paramType.immMaxSize ?: return null
                return when (instrType.paramType) {
                    R_INSTR.ParamType.RD_I20 -> {
                        paramcoll.getValues(immMax)[1].checkResult
                    }

                    R_INSTR.ParamType.RD_Off12 -> {
                        paramcoll.getValues(immMax)[1].checkResult
                    }

                    R_INSTR.ParamType.RS2_Off5 -> {
                        paramcoll.getValues(immMax)[1].checkResult
                    }

                    R_INSTR.ParamType.RD_RS1_I12 -> {
                        paramcoll.getValues(immMax)[2].checkResult
                    }

                    R_INSTR.ParamType.RD_RS1_I6 -> {
                        paramcoll.getValues(immMax)[2].checkResult
                    }

                    R_INSTR.ParamType.RS1_RS2_I12 -> {
                        paramcoll.getValues(immMax)[2].checkResult
                    }

                    R_INSTR.ParamType.PS_RD_LI_I28Unsigned, R_INSTR.ParamType.PS_RD_LI_I32Signed, R_INSTR.ParamType.PS_RD_LI_I40Unsigned, R_INSTR.ParamType.PS_RD_LI_I52Unsigned, R_INSTR.ParamType.PS_RD_LI_I64 -> {
                        val value = paramcoll.getValues(immMax)[1]
                        if (value.isSigned() == instrType.paramType.immSigned || instrType.paramType.immSigned == null) {
                            value.checkResult
                        } else {
                            Variable.CheckResult(false, "", "Is signed but shouldn't be signed for this type!")
                        }
                    }

                    R_INSTR.ParamType.CSR_RD_OFF12_RS1 -> {
                        val value = paramcoll.getValues(Bit12())[1]
                        value.checkResult
                    }

                    R_INSTR.ParamType.CSR_RD_OFF12_UIMM5 -> {
                        val csrCheck = paramcoll.getValues(Bit12())[1].checkResult
                        val uimm5Check = paramcoll.getValues(immMax)[2].checkResult
                        Variable.CheckResult(csrCheck.valid && uimm5Check.valid, "", "${csrCheck.message} | ${uimm5Check.message}")
                    }

                    else -> null
                }

            } catch (e: IndexOutOfBoundsException) {
                console.error("RV64Syntax: Index out of bounds exception! Compare value indices with check()! Bug needs to be fixed! ($e)")
                return null
            }
        }

        fun check(arch: Architecture, paramcoll: E_PARAMCOLL = E_PARAMCOLL()): R_INSTR.InstrType? {
            val params = paramcoll.paramsWithOutSplitSymbols
            var type: R_INSTR.InstrType
            types.forEach {
                var matches: Boolean
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

                    R_INSTR.ParamType.RD_RS1_RS2 -> {
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

                    R_INSTR.ParamType.RD_RS1_I6 -> {
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

                    R_INSTR.ParamType.PS_RD_LI_I28Unsigned, R_INSTR.ParamType.PS_RD_LI_I32Signed, R_INSTR.ParamType.PS_RD_LI_I40Unsigned, R_INSTR.ParamType.PS_RD_LI_I52Unsigned, R_INSTR.ParamType.PS_RD_LI_I64 -> {
                        matches = if (params.size == 2) {
                            params[0] is E_PARAM.Register && params[1] is E_PARAM.Constant
                        } else {
                            false
                        }
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
                        matches = params.isEmpty()
                    }

                    R_INSTR.ParamType.NONE -> {
                        matches = params.isEmpty()
                    }

                    R_INSTR.ParamType.CSR_RD_OFF12_RS1 -> matches = if (params.size == 3) {
                        val first = params[0]
                        val second = params[1]
                        val third = params[2]

                        first is E_PARAM.Register && (second is E_PARAM.Constant && arch.getRegByAddr(second.getValue(Bit12()), RV64.CSR_REGFILE_NAME) != null) && third is E_PARAM.Register
                    } else {
                        false
                    }

                    R_INSTR.ParamType.CSR_RD_OFF12_UIMM5 -> matches = if (params.size == 3) {
                        val first = params[0]
                        val second = params[1]
                        val third = params[2]
                        first is E_PARAM.Register && (second is E_PARAM.Constant && arch.getRegByAddr(second.getValue(Bit12()), RV64.CSR_REGFILE_NAME) != null) && third is E_PARAM.Constant
                    } else {
                        false
                    }
                }

                if (matches) {
                    val checkResult = matchesSize(paramcoll, type)
                    checkResult?.let { checkRes ->
                        matches = checkRes.valid
                    }
                }

                if (matches) {
                    return type
                }
            }

            return null
        }
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

        fun getValues(immSize: Variable.Size?): Array<Variable.Value> {
            val values = mutableListOf<Variable.Value>()
            paramsWithOutSplitSymbols.forEach {
                when (it) {
                    is E_PARAM.Constant -> {
                        val value = it.getValue(immSize)
                        values.add(value)
                    }

                    is E_PARAM.Offset -> {
                        values.addAll(it.getValueArray(immSize))
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

        class Offset(val offset: Compiler.Token.Constant, openParan: Compiler.Token, val register: Compiler.Token.Register, closeParan: Compiler.Token) : E_PARAM(REFS.REF_E_PARAM_OFFSET, RV64Flags.offset, offset, openParan, register, closeParan) {
            fun getValueArray(offsetSize: Variable.Size?): Array<Variable.Value> {
                return arrayOf(offset.getValue(offsetSize), register.reg.address.toBin())
            }

            object Syntax {
                val tokenSeq = TokenSeq(TokenSeq.Component.InSpecific.Constant, TokenSeq.Component.Specific("("), TokenSeq.Component.InSpecific.Register, TokenSeq.Component.Specific(")"), ignoreSpaces = true)
            }
        }

        class Constant(val constant: Compiler.Token.Constant) : E_PARAM(REFS.REF_E_PARAM_CONSTANT, RV64Flags.constant, constant) {
            fun getValue(size: Variable.Size?): Variable.Value {
                return constant.getValue(size)
            }
        }

        class Register(val register: Compiler.Token.Register) : E_PARAM(REFS.REF_E_PARAM_REGISTER, RV64Flags.register, register) {
            fun getAddress(): Bin {
                return register.reg.address.toBin()
            }
        }

        class SplitSymbol(splitSymbol: Compiler.Token.Symbol) : E_PARAM(REFS.REF_E_PARAM_SPLITSYMBOL, RV64Flags.instruction, splitSymbol)
        class Link(vararg val labelName: Compiler.Token) : E_PARAM(REFS.REF_E_PARAM_LABELLINK, RV64Flags.label, *labelName) {
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

    class E_LABEL(vararg labelName: Compiler.Token, colon: Compiler.Token.Symbol, val sublblFrom: E_LABEL? = null) : TreeNode.ElementNode(ConnectedHL(RV64Flags.label), REFS.REF_E_LABEL, *labelName, colon) {

        val wholeName: String
        val tokenSeq: TokenSeq
        val isSubLabel: Boolean = sublblFrom != null

        init {

            wholeName = (sublblFrom?.wholeName ?: "") + labelName.joinToString("") { it.content }
            val tokenComponents = mutableListOf<TokenSeq.Component>()
            for (token in labelName) {
                tokenComponents.add(TokenSeq.Component.Specific(token.content))
            }
            tokenSeq = TokenSeq(*tokenComponents.toTypedArray(), ignoreSpaces = false)
        }
    }

    class E_DIRECTIVE(val type: DirType, dot: Compiler.Token.Symbol, vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RV64Flags.directive), REFS.REF_E_DIRECTIVE, dot, *tokens) {

        fun isDataEmitting(): Boolean {
            return type.majorType == MajorType.DE_ALIGNED || type.majorType == MajorType.DE_UNALIGNED
        }

        enum class MajorType(val docName: String) {
            SECTIONSTART("Section identification"), DE_ALIGNED("Data emitting aligned"), DE_UNALIGNED("Data emitting unaligned")
        }

        enum class DirType(val dirname: String, val majorType: MajorType, val deSize: Variable.Size? = null) {
            TEXT("text", MajorType.SECTIONSTART), DATA("data", MajorType.SECTIONSTART), RODATA("rodata", MajorType.SECTIONSTART), BSS("bss", MajorType.SECTIONSTART),

            BYTE("byte", MajorType.DE_ALIGNED, Bit8()),
            HALF("half", MajorType.DE_ALIGNED, Bit16()),
            WORD("word", MajorType.DE_ALIGNED, Bit32()),
            DWORD("dword", MajorType.DE_ALIGNED, Bit64()),
            ASCIZ("asciz", MajorType.DE_ALIGNED),
            STRING("string", MajorType.DE_ALIGNED),

            BYTE_2("2byte", MajorType.DE_UNALIGNED, Bit16()),
            BYTE_4("4byte", MajorType.DE_UNALIGNED, Bit32()),
            BYTE_8("8byte", MajorType.DE_UNALIGNED, Bit64()),
        }
    }

    /* -------------------------------------------------------------- ROWS -------------------------------------------------------------- */
    class R_SECSTART(val directive: E_DIRECTIVE) : TreeNode.RowNode(REFS.REF_R_SECSTART, directive)
    class R_JLBL(val label: E_LABEL, val isMainLabel: Boolean, val isGlobalStart: Boolean = false) : TreeNode.RowNode(REFS.REF_R_JLBL, label) {
        var inlineInstr: R_INSTR? = null
        fun addInlineInstr(instr: R_INSTR) {
            inlineInstr = instr
            this.elementNodes = arrayOf(*this.elementNodes, *instr.elementNodes)
        }

    }
    class R_ULBL(val label: E_LABEL, val directive: E_DIRECTIVE) : TreeNode.RowNode(REFS.REF_R_ULBL, label, directive)
    class R_ILBL(val label: E_LABEL, val directive: E_DIRECTIVE, val paramcoll: E_PARAMCOLL) : TreeNode.RowNode(REFS.REF_R_ILBL, label, directive, paramcoll)
    class R_DATAEMITTING(val directive: E_DIRECTIVE, val paramcoll: E_PARAMCOLL) : TreeNode.RowNode(REFS.REF_R_DATAEMITTING, directive, paramcoll)
    class R_INSTR(val instrname: E_INSTRNAME, val paramcoll: E_PARAMCOLL?, val instrType: InstrType) : TreeNode.RowNode(REFS.REF_R_INSTR, instrname, paramcoll) {
        enum class ParamType(val pseudo: Boolean, val exampleString: String, val immMaxSize: Variable.Size? = null, val immSigned: Boolean? = null) {
            // NORMAL INSTRUCTIONS
            RD_I20(false, "rd, imm20", Bit20()) {
                override fun getTSParamString(arch: Architecture, paramMap: MutableMap<RV64BinMapper.MaskLabel, Bin>, labelName: String): String {
                    val rd = paramMap[RD]
                    return if (rd != null) {
                        paramMap.remove(RD)
                        val immString = labelName.ifEmpty { "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString(" ") { it.toHex().getRawHexStr() }}" }
                        "${arch.getRegByAddr(rd)?.aliases?.first()},\t$immString"
                    } else {
                        "param missing"
                    }
                }
            }, // rd, imm
            RD_Off12(false, "rd, imm12(rs)", Bit12()) {
                override fun getTSParamString(arch: Architecture, paramMap: MutableMap<RV64BinMapper.MaskLabel, Bin>, labelName: String): String {
                    val rd = paramMap[RD]
                    val rs1 = paramMap[RS1]
                    return if (rd != null && rs1 != null) {
                        paramMap.remove(RD)
                        paramMap.remove(RS1)
                        val immString = labelName.ifEmpty { "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString(" ") { it.toHex().getRawHexStr() }}" }
                        "${arch.getRegByAddr(rd)?.aliases?.first()},\t$immString(${arch.getRegByAddr(rs1)?.aliases?.first()})"
                    } else {
                        "param missing"
                    }
                }
            }, // rd, imm12(rs)
            RS2_Off5(false, "rs2, imm5(rs1)", Bit5()) {
                override fun getTSParamString(arch: Architecture, paramMap: MutableMap<RV64BinMapper.MaskLabel, Bin>, labelName: String): String {
                    val rs2 = paramMap[RS2]
                    val rs1 = paramMap[RS1]
                    return if (rs2 != null && rs1 != null) {
                        paramMap.remove(RS2)
                        paramMap.remove(RS1)
                        val immString = labelName.ifEmpty { "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString(" ") { it.toHex().getRawHexStr() }}" }
                        "${arch.getRegByAddr(rs2)?.aliases?.first()},\t$immString(${arch.getRegByAddr(rs1)?.aliases?.first()})"
                    } else {
                        "param missing"
                    }
                }
            }, // rs2, imm5(rs1)
            RD_RS1_RS2(false, "rd, rs1, rs2") {
                override fun getTSParamString(arch: Architecture, paramMap: MutableMap<RV64BinMapper.MaskLabel, Bin>, labelName: String): String {
                    val rd = paramMap[RD]
                    val rs1 = paramMap[RS1]
                    val rs2 = paramMap[RS2]
                    return if (rd != null && rs2 != null && rs1 != null) {
                        paramMap.remove(RD)
                        paramMap.remove(RS2)
                        paramMap.remove(RS1)
                        "${arch.getRegByAddr(rd)?.aliases?.first()},\t${arch.getRegByAddr(rs1)?.aliases?.first()},\t${arch.getRegByAddr(rs2)?.aliases?.first()}"
                    } else {
                        "param missing"
                    }
                }
            }, // rd, rs1, rs2
            RD_RS1_I12(false, "rd, rs1, imm12", Bit12()) {
                override fun getTSParamString(arch: Architecture, paramMap: MutableMap<RV64BinMapper.MaskLabel, Bin>, labelName: String): String {
                    val rd = paramMap[RD]
                    val rs1 = paramMap[RS1]
                    return if (rd != null && rs1 != null) {
                        paramMap.remove(RD)
                        paramMap.remove(RS1)
                        val immString = labelName.ifEmpty { "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString(" ") { it.toHex().getRawHexStr() }}" }
                        "${arch.getRegByAddr(rd)?.aliases?.first()},\t${arch.getRegByAddr(rs1)?.aliases?.first()},\t$immString"
                    } else {
                        "param missing"
                    }
                }
            }, // rd, rs, imm
            RD_RS1_I6(false, "rd, rs1, shamt6", Bit6()) {
                override fun getTSParamString(arch: Architecture, paramMap: MutableMap<RV64BinMapper.MaskLabel, Bin>, labelName: String): String {
                    val rd = paramMap[RD]
                    val rs1 = paramMap[RS1]
                    return if (rd != null && rs1 != null) {
                        paramMap.remove(RD)
                        paramMap.remove(RS1)
                        val immString = labelName.ifEmpty { "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString(" ") { it.toHex().getRawHexStr() }}" }
                        "${arch.getRegByAddr(rd)?.aliases?.first()},\t${arch.getRegByAddr(rs1)?.aliases?.first()},\t$immString"
                    } else {
                        "param missing"
                    }
                }
            }, // rd, rs, shamt
            RS1_RS2_I12(false, "rs1, rs2, imm12", Bit12()) {
                override fun getTSParamString(arch: Architecture, paramMap: MutableMap<RV64BinMapper.MaskLabel, Bin>, labelName: String): String {
                    val rs2 = paramMap[RS2]
                    val rs1 = paramMap[RS1]
                    return if (rs2 != null && rs1 != null) {
                        paramMap.remove(RS2)
                        paramMap.remove(RS1)
                        val immString = labelName.ifEmpty { "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString(" ") { it.toHex().getRawHexStr() }}" }
                        "${arch.getRegByAddr(rs1)?.aliases?.first()},\t${arch.getRegByAddr(rs2)?.aliases?.first()},\t$immString"
                    } else {
                        "param missing"
                    }
                }
            }, // rs1, rs2, imm
            CSR_RD_OFF12_RS1(false, "rd, csr12, rs1") {
                override fun getTSParamString(arch: Architecture, paramMap: MutableMap<RV64BinMapper.MaskLabel, Bin>, labelName: String): String {
                    val rd = paramMap[RD]
                    val csr = paramMap[CSR]
                    val rs1 = paramMap[RS1]
                    return if (rd != null && csr != null && rs1 != null) {
                        paramMap.remove(RD)
                        paramMap.remove(CSR)
                        paramMap.remove(RS1)
                        "${arch.getRegByAddr(rd)?.aliases?.first()},\t${arch.getRegByAddr(csr)?.aliases?.first()},\t${arch.getRegByAddr(rs1)?.aliases?.first()}"
                    } else {
                        "param missing"
                    }
                }
            },
            CSR_RD_OFF12_UIMM5(false, "rd, offset, uimm5", Bit5()) {
                override fun getTSParamString(arch: Architecture, paramMap: MutableMap<RV64BinMapper.MaskLabel, Bin>, labelName: String): String {
                    val rd = paramMap[RD]
                    val csr = paramMap[CSR]
                    return if (rd != null && csr != null) {
                        paramMap.remove(RD)
                        paramMap.remove(CSR)
                        val immString = labelName.ifEmpty { paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString(" ") { it.toBin().toString() } }
                        "${arch.getRegByAddr(rd)?.aliases?.first()},\t${arch.getRegByAddr(csr)?.aliases?.first()},\t$immString"
                    } else {
                        "param missing"
                    }
                }
            },

            // PSEUDO INSTRUCTIONS
            PS_RS1_RS2_Jlbl(true, "rs1, rs2, jlabel"),
            PS_RD_LI_I28Unsigned(true, "rd, imm28u", Bit28(), false), // rd, imm28 unsigned
            PS_RD_LI_I32Signed(true, "rd, imm32s", Bit32(), true), // rd, imm32
            PS_RD_LI_I40Unsigned(true, "rd, imm40u", Bit40(), false),
            PS_RD_LI_I52Unsigned(true, "rd, imm52u", Bit52(), false),
            PS_RD_LI_I64(true, "rd, imm64", Bit64(), null), // rd, imm64
            PS_RS1_Jlbl(true, "rs, jlabel"), // rs, label
            PS_RD_Albl(true, "rd, alabel"), // rd, label
            PS_Jlbl(true, "jlabel"),  // label
            PS_RD_RS1(true, "rd, rs"), // rd, rs
            PS_RS1(true, "rs1"),

            // NONE PARAM INSTR
            NONE(false, "none"), PS_NONE(true, "none");

            open fun getTSParamString(arch: Architecture, paramMap: MutableMap<RV64BinMapper.MaskLabel, Bin>, labelName: String): String {
                return "pseudo param type"
            }
        }

        enum class InstrType(val id: String, val pseudo: Boolean, val paramType: ParamType, val opCode: OpCode? = null, val memWords: Int = 1, val relative: InstrType? = null, val needFeatures: List<Int> = emptyList()) {
            LUI("LUI", false, ParamType.RD_I20, OpCode("00000000000000000000 00000 0110111", arrayOf(IMM20, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap) // only for console information
                    // get relevant parameters from binary map
                    val rdAddr = paramMap[RD]
                    val imm20 = paramMap[IMM20]
                    if (rdAddr == null || imm20 == null) return

                    // get relevant registers
                    val rd = arch.getRegByAddr(rdAddr)
                    val pc = arch.getRegContainer().pc
                    if (rd == null) return

                    // calculate
                    val shiftedIMM = imm20.getResized(RV64.XLEN) shl 12 // from imm20 to imm32
                    // change states
                    rd.set(shiftedIMM)    // set register to imm32 value
                    pc.set(pc.get() + Hex("4"))
                }
            },
            AUIPC("AUIPC", false, ParamType.RD_I20, OpCode("00000000000000000000 00000 0010111", arrayOf(IMM20, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    if (rdAddr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val imm20 = paramMap[IMM20]
                        val pc = arch.getRegContainer().pc
                        if (rd != null && imm20 != null) {
                            val shiftedIMM = imm20.getUResized(RV64.XLEN) shl 12
                            val sum = pc.get() + shiftedIMM
                            rd.set(sum)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            JAL("JAL", false, ParamType.RD_I20, OpCode("00000000000000000000 00000 1101111", arrayOf(IMM20, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    if (rdAddr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val imm20 = paramMap[IMM20]
                        val pc = arch.getRegContainer().pc
                        if (rd != null && imm20 != null) {
                            val imm20str = imm20.getRawBinaryStr()

                            /**
                             *      RV64IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                             *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                             *        Location       20 [      10 : 1               ] 11 [ 19 : 12             ]
                             */

                            val shiftedImm = Bin(imm20str[0].toString() + imm20str.substring(12) + imm20str[11] + imm20str.substring(1, 11), Bit20()).getResized(RV64.XLEN) shl 1

                            rd.set(pc.get() + Hex("4"))
                            pc.set(pc.get() + shiftedImm)
                        }
                    }
                }
            },
            JALR("JALR", false, ParamType.RD_RS1_I12, OpCode("000000000000 00000 000 00000 1100111", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val imm12 = paramMap[IMM12]
                        val pc = arch.getRegContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val jumpAddr = rs1.get() + imm12.getResized(RV64.XLEN)
                            rd.set(pc.get() + Hex("4"))
                            pc.set(jumpAddr)
                        }
                    }
                }
            },
            ECALL("ECALL", false, ParamType.NONE, OpCode("000000000000 00000 000 00000 1110011", arrayOf(NONE, NONE, NONE, NONE, OPCODE))),
            EBREAK("EBREAK", false, ParamType.NONE, OpCode("000000000001 00000 000 00000 1110011", arrayOf(NONE, NONE, NONE, NONE, OPCODE))),
            BEQ("BEQ", false, ParamType.RS1_RS2_I12, OpCode("0000000 00000 00000 000 00000 1100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val imm7 = paramMap[IMM7]
                        val imm5 = paramMap[IMM5]
                        val pc = arch.getRegContainer().pc
                        if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                            val imm7str = imm7.getResized(Bit7()).getRawBinaryStr()
                            val imm5str = imm5.getResized(Bit5()).getRawBinaryStr()
                            val imm12 = Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Bit12())

                            val offset = imm12.toBin().getResized(RV64.XLEN) shl 1
                            if (rs1.get().toBin() == rs2.get().toBin()) {
                                pc.set(pc.get() + offset)
                            } else {
                                pc.set(pc.get() + Hex("4"))
                            }
                        }
                    }
                }
            },
            BNE("BNE", false, ParamType.RS1_RS2_I12, OpCode("0000000 00000 00000 001 00000 1100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val imm7 = paramMap[IMM7]
                        val imm5 = paramMap[IMM5]
                        val pc = arch.getRegContainer().pc
                        if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                            val imm7str = imm7.getResized(Bit7()).getRawBinaryStr()
                            val imm5str = imm5.getResized(Bit5()).getRawBinaryStr()
                            val imm12 = Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Bit12())
                            val offset = imm12.toBin().getResized(RV64.XLEN) shl 1
                            if (rs1.get().toBin() != rs2.get().toBin()) {
                                pc.set(pc.get() + offset)
                            } else {
                                pc.set(pc.get() + Hex("4"))
                            }
                        }
                    }
                }
            },
            BLT("BLT", false, ParamType.RS1_RS2_I12, OpCode("0000000 00000 00000 100 00000 1100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val imm7 = paramMap[IMM7]
                        val imm5 = paramMap[IMM5]
                        val pc = arch.getRegContainer().pc
                        if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                            val imm7str = imm7.getResized(Bit7()).getRawBinaryStr()
                            val imm5str = imm5.getResized(Bit5()).getRawBinaryStr()
                            val imm12 = Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Bit12())
                            val offset = imm12.toBin().getResized(RV64.XLEN) shl 1
                            if (rs1.get().toDec() < rs2.get().toDec()) {
                                pc.set(pc.get() + offset)
                            } else {
                                pc.set(pc.get() + Hex("4"))
                            }
                        }
                    }
                }
            },
            BGE("BGE", false, ParamType.RS1_RS2_I12, OpCode("0000000 00000 00000 101 00000 1100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val imm7 = paramMap[IMM7]
                        val imm5 = paramMap[IMM5]
                        val pc = arch.getRegContainer().pc
                        if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                            val imm7str = imm7.getResized(Bit7()).getRawBinaryStr()
                            val imm5str = imm5.getResized(Bit5()).getRawBinaryStr()
                            val imm12 = Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Bit12())
                            val offset = imm12.toBin().getResized(RV64.XLEN) shl 1
                            if (rs1.get().toDec() >= rs2.get().toDec()) {
                                pc.set(pc.get() + offset)
                            } else {
                                pc.set(pc.get() + Hex("4"))
                            }
                        }
                    }
                }
            },
            BLTU("BLTU", false, ParamType.RS1_RS2_I12, OpCode("0000000 00000 00000 110 00000 1100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val imm7 = paramMap[IMM7]
                        val imm5 = paramMap[IMM5]
                        val pc = arch.getRegContainer().pc
                        if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                            val imm7str = imm7.getResized(Bit7()).getRawBinaryStr()
                            val imm5str = imm5.getResized(Bit5()).getRawBinaryStr()
                            val imm12 = Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Bit12())
                            val offset = imm12.toBin().getResized(RV64.XLEN) shl 1
                            if (rs1.get().toUDec() < rs2.get().toUDec()) {
                                pc.set(pc.get() + offset)
                            } else {
                                pc.set(pc.get() + Hex("4"))
                            }
                        }
                    }
                }
            },
            BGEU("BGEU", false, ParamType.RS1_RS2_I12, OpCode("0000000 00000 00000 111 00000 1100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val imm7 = paramMap[IMM7]
                        val imm5 = paramMap[IMM5]
                        val pc = arch.getRegContainer().pc
                        if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                            val imm7str = imm7.getResized(Bit7()).getRawBinaryStr()
                            val imm5str = imm5.getResized(Bit5()).getRawBinaryStr()
                            val imm12 = Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Bit12())
                            val offset = imm12.toBin().getResized(RV64.XLEN) shl 1
                            if (rs1.get().toUDec() >= rs2.get().toUDec()) {
                                pc.set(pc.get() + offset)
                            } else {
                                pc.set(pc.get() + Hex("4"))
                            }
                        }
                    }
                }
            },
            BEQ1("BEQ", true, ParamType.PS_RS1_RS2_Jlbl, relative = BEQ),
            BNE1("BNE", true, ParamType.PS_RS1_RS2_Jlbl, relative = BNE),
            BLT1("BLT", true, ParamType.PS_RS1_RS2_Jlbl, relative = BLT),
            BGE1("BGE", true, ParamType.PS_RS1_RS2_Jlbl, relative = BGE),
            BLTU1("BLTU", true, ParamType.PS_RS1_RS2_Jlbl, relative = BLTU),
            BGEU1("BGEU", true, ParamType.PS_RS1_RS2_Jlbl, relative = BGEU),
            LB("LB", false, ParamType.RD_Off12, OpCode("000000000000 00000 000 00000 0000011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val imm12 = paramMap[IMM12]
                    if (rdAddr != null && rs1Addr != null && imm12 != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null) {
                            val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                            val loadedByte = arch.getMemory().load(memAddr.toHex()).toBin().getResized(RV64.XLEN)
                            rd.set(loadedByte)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            LH("LH", false, ParamType.RD_Off12, OpCode("000000000000 00000 001 00000 0000011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val imm12 = paramMap[IMM12]
                    if (rdAddr != null && rs1Addr != null && imm12 != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null) {
                            val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                            val loadedHalfWord = arch.getMemory().load(memAddr.toHex(), 2).toBin().getResized(RV64.XLEN)
                            rd.set(loadedHalfWord)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            LW("LW", false, ParamType.RD_Off12, OpCode("000000000000 00000 010 00000 0000011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val imm12 = paramMap[IMM12]
                    if (rdAddr != null && rs1Addr != null && imm12 != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null) {
                            val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                            val loadedWord = arch.getMemory().load(memAddr.toHex(), 4).toBin().getResized(RV64.XLEN)
                            rd.set(loadedWord)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            LD("LD", false, ParamType.RD_Off12, OpCode("000000000000 00000 011 00000 0000011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val imm12 = paramMap[IMM12]
                    if (rdAddr != null && rs1Addr != null && imm12 != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null) {
                            val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                            val loadedWord = arch.getMemory().load(memAddr.toHex(), 8).toBin().getResized(RV64.XLEN)
                            rd.set(loadedWord)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            LBU("LBU", false, ParamType.RD_Off12, OpCode("000000000000 00000 100 00000 0000011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val imm12 = paramMap[IMM12]
                    if (rdAddr != null && rs1Addr != null && imm12 != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null) {
                            val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                            val loadedByte = arch.getMemory().load(memAddr.toHex())
                            rd.set(Bin(rd.get().toBin().getRawBinaryStr().substring(0, RV64.XLEN.bitWidth - 8) + loadedByte.toBin().getRawBinaryStr(), RV64.XLEN))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            LHU("LHU", false, ParamType.RD_Off12, OpCode("000000000000 00000 101 00000 0000011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val imm12 = paramMap[IMM12]
                    if (rdAddr != null && rs1Addr != null && imm12 != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null) {
                            val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                            val loadedByte = arch.getMemory().load(memAddr.toHex(), 2)
                            rd.set(Bin(rd.get().toBin().getRawBinaryStr().substring(0, RV64.XLEN.bitWidth - 16) + loadedByte.toBin().getRawBinaryStr(), RV64.XLEN))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            LWU("LWU", false, ParamType.RD_Off12, OpCode("000000000000 00000 110 00000 0000011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val imm12 = paramMap[IMM12]
                    if (rdAddr != null && rs1Addr != null && imm12 != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null) {
                            val memAddr = rs1.get().toBin() + imm12.getResized(RV64.XLEN)
                            val loadedWord = arch.getMemory().load(memAddr.toHex(), 4).toBin().getUResized(RV64.XLEN)
                            rd.set(loadedWord)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SB("SB", false, ParamType.RS2_Off5, OpCode("0000000 00000 00000 000 00000 0100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    val imm5 = paramMap[IMM5]
                    val imm7 = paramMap[IMM7]
                    if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rs1 != null && rs2 != null) {
                            val off64 = (imm7.getResized(RV64.XLEN) shl 5) + imm5
                            val memAddr = rs1.get().toBin().getResized(RV64.XLEN) + off64
                            arch.getMemory().store(memAddr, rs2.get().toBin().getResized(Bit8()))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SH("SH", false, ParamType.RS2_Off5, OpCode("0000000 00000 00000 001 00000 0100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    val imm5 = paramMap[IMM5]
                    val imm7 = paramMap[IMM7]
                    if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rs1 != null && rs2 != null) {
                            val off64 = (imm7.getResized(RV64.XLEN) shl 5) + imm5
                            val memAddr = rs1.get().toBin().getResized(RV64.XLEN) + off64
                            arch.getMemory().store(memAddr, rs2.get().toBin().getResized(Bit16()))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SW("SW", false, ParamType.RS2_Off5, OpCode("0000000 00000 00000 010 00000 0100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    val imm5 = paramMap[IMM5]
                    val imm7 = paramMap[IMM7]
                    if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rs1 != null && rs2 != null) {
                            val off64 = (imm7.getResized(RV64.XLEN) shl 5) + imm5
                            val memAddr = rs1.variable.get().toBin().getResized(RV64.XLEN) + off64
                            arch.getMemory().store(memAddr, rs2.get().toBin().getResized(Bit32()))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SD("SD", false, ParamType.RS2_Off5, OpCode("0000000 00000 00000 011 00000 0100011", arrayOf(IMM7, RS2, RS1, FUNCT3, IMM5, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    val imm5 = paramMap[IMM5]
                    val imm7 = paramMap[IMM7]
                    if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rs1 != null && rs2 != null) {
                            val off64 = (imm7.getResized(RV64.XLEN) shl 5) + imm5
                            val memAddr = rs1.variable.get().toBin().getResized(RV64.XLEN) + off64
                            arch.getMemory().store(memAddr, rs2.get().toBin().getResized(RV64.XLEN))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            ADDI("ADDI", false, ParamType.RD_RS1_I12, OpCode("000000000000 00000 000 00000 0010011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val imm12 = paramMap[IMM12]
                        val pc = arch.getRegContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm64 = imm12.getResized(RV64.XLEN)
                            val sum = rs1.get().toBin() + paddedImm64
                            rd.set(sum)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            ADDIW("ADDIW", false, ParamType.RD_RS1_I12, OpCode("000000000000 00000 000 00000 0011011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val imm12 = paramMap[IMM12]
                        val pc = arch.getRegContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm32 = imm12.getResized(Bit32())
                            val sum = rs1.get().toBin().getResized(Bit32()) + paddedImm32
                            rd.set(sum.toBin().getResized(RV64.XLEN))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SLTI("SLTI", false, ParamType.RD_RS1_I12, OpCode("000000000000 00000 010 00000 0010011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val imm12 = paramMap[IMM12]
                        val pc = arch.getRegContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm64 = imm12.getResized(RV64.XLEN)
                            rd.set(if (rs1.get().toDec() < paddedImm64.toDec()) Bin("1", RV64.XLEN) else Bin("0", RV64.XLEN))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SLTIU("SLTIU", false, ParamType.RD_RS1_I12, OpCode("000000000000 00000 011 00000 0010011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val imm12 = paramMap[IMM12]
                        val pc = arch.getRegContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm64 = imm12.getUResized(RV64.XLEN)
                            rd.set(if (rs1.get().toBin() < paddedImm64) Bin("1", RV64.XLEN) else Bin("0", RV64.XLEN))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            XORI("XORI", false, ParamType.RD_RS1_I12, OpCode("000000000000 00000 100 00000 0010011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val imm12 = paramMap[IMM12]
                        val pc = arch.getRegContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm64 = imm12.getUResized(RV64.XLEN)
                            rd.set(rs1.get().toBin() xor paddedImm64)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            ORI("ORI", false, ParamType.RD_RS1_I12, OpCode("000000000000 00000 110 00000 0010011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val imm12 = paramMap[IMM12]
                        val pc = arch.getRegContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm64 = imm12.getUResized(RV64.XLEN)
                            rd.set(rs1.get().toBin() or paddedImm64)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            ANDI("ANDI", false, ParamType.RD_RS1_I12, OpCode("000000000000 00000 111 00000 0010011", arrayOf(IMM12, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val imm12 = paramMap[IMM12]
                        val pc = arch.getRegContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm64 = imm12.getUResized(RV64.XLEN)
                            rd.set(rs1.get().toBin() and paddedImm64)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SLLI("SLLI", false, ParamType.RD_RS1_I6, OpCode("000000 000000 00000 001 00000 0010011", arrayOf(FUNCT6, SHAMT6, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val shamt6 = paramMap[SHAMT6]
                        val pc = arch.getRegContainer().pc
                        if (rd != null && shamt6 != null && rs1 != null) {
                            rd.set(rs1.get().toBin() ushl shamt6.getRawBinaryStr().toInt(2))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SLLIW("SLLIW", false, ParamType.RD_RS1_I6, OpCode("000000 000000 00000 001 00000 0011011", arrayOf(FUNCT6, SHAMT6, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val shamt6 = paramMap[SHAMT6]
                        val pc = arch.getRegContainer().pc
                        if (rd != null && shamt6 != null && rs1 != null) {
                            rd.set((rs1.get().toBin().getUResized(Bit32()) ushl shamt6.getUResized(Bit5()).getRawBinaryStr().toInt(2)).getResized(RV64.XLEN))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SRLI("SRLI", false, ParamType.RD_RS1_I6, OpCode("000000 000000 00000 101 00000 0010011", arrayOf(FUNCT6, SHAMT6, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val shamt6 = paramMap[SHAMT6]
                        val pc = arch.getRegContainer().pc
                        if (rd != null && shamt6 != null && rs1 != null) {
                            rd.set(rs1.get().toBin() ushr shamt6.getRawBinaryStr().toInt(2))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SRLIW("SRLIW", false, ParamType.RD_RS1_I6, OpCode("000000 000000 00000 101 00000 0011011", arrayOf(FUNCT6, SHAMT6, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val shamt6 = paramMap[SHAMT6]
                        val pc = arch.getRegContainer().pc
                        if (rd != null && shamt6 != null && rs1 != null) {
                            rd.set((rs1.get().toBin().getUResized(Bit32()) ushr shamt6.getUResized(Bit5()).getRawBinaryStr().toInt(2)).getResized(RV64.XLEN))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SRAI("SRAI", false, ParamType.RD_RS1_I6, OpCode("010000 000000 00000 101 00000 0010011", arrayOf(FUNCT6, SHAMT6, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val shamt6 = paramMap[SHAMT6]
                        val pc = arch.getRegContainer().pc
                        if (rd != null && shamt6 != null && rs1 != null) {
                            rd.set(rs1.get().toBin() shr shamt6.getRawBinaryStr().toInt(2))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SRAIW("SRAIW", false, ParamType.RD_RS1_I6, OpCode("010000 000000 00000 101 00000 0011011", arrayOf(FUNCT6, SHAMT6, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val shamt6 = paramMap[SHAMT6]
                        val pc = arch.getRegContainer().pc
                        if (rd != null && shamt6 != null && rs1 != null) {
                            rd.set((rs1.get().toBin().getUResized(Bit32()) shr shamt6.getUResized(Bit5()).getRawBinaryStr().toInt(2)).getResized(RV64.XLEN))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            ADD("ADD", false, ParamType.RD_RS1_RS2, OpCode("0000000 00000 00000 000 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() + rs2.get().toBin())
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            ADDW("ADDW", false, ParamType.RD_RS1_RS2, OpCode("0000000 00000 00000 000 00000 0111011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set((rs1.get().toBin().getResized(Bit32()) + rs2.get().toBin().getResized(Bit32())).toBin().getResized(RV64.XLEN))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SUB("SUB", false, ParamType.RD_RS1_RS2, OpCode("0100000 00000 00000 000 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() - rs2.get().toBin())
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SUBW("SUBW", false, ParamType.RD_RS1_RS2, OpCode("0100000 00000 00000 000 00000 0111011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set((rs1.get().toBin().getResized(Bit32()) - rs2.get().toBin().getResized(Bit32())).toBin().getResized(RV64.XLEN))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SLL("SLL", false, ParamType.RD_RS1_RS2, OpCode("0000000 00000 00000 001 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() ushl rs2.get().toBin().getUResized(Bit6()).getRawBinaryStr().toInt(2))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SLLW("SLLW", false, ParamType.RD_RS1_RS2, OpCode("0000000 00000 00000 001 00000 0111011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin().getUResized(Bit32()) ushl rs2.get().toBin().getUResized(Bit5()).getRawBinaryStr().toInt(2))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SLT("SLT", false, ParamType.RD_RS1_RS2, OpCode("0000000 00000 00000 010 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(if (rs1.get().toDec() < rs2.get().toDec()) Bin("1", Bit32()) else Bin("0", Bit32()))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SLTU("SLTU", false, ParamType.RD_RS1_RS2, OpCode("0000000 00000 00000 011 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(if (rs1.get().toBin() < rs2.get().toBin()) Bin("1", Bit32()) else Bin("0", Bit32()))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            XOR("XOR", false, ParamType.RD_RS1_RS2, OpCode("0000000 00000 00000 100 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() xor rs2.get().toBin())
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SRL("SRL", false, ParamType.RD_RS1_RS2, OpCode("0000000 00000 00000 101 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() ushr rs2.get().toBin().getUResized(Bit6()).getRawBinaryStr().toInt(2))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SRLW("SRLW", false, ParamType.RD_RS1_RS2, OpCode("0000000 00000 00000 101 00000 0111011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin().getUResized(Bit32()) ushr rs2.get().toBin().getUResized(Bit5()).getRawBinaryStr().toInt(2))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SRA("SRA", false, ParamType.RD_RS1_RS2, OpCode("0100000 00000 00000 101 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() shr rs2.get().toBin().getUResized(Bit6()).getRawBinaryStr().toInt(2))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            SRAW("SRAW", false, ParamType.RD_RS1_RS2, OpCode("0100000 00000 00000 101 00000 0111011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin().getUResized(Bit32()) shr rs2.get().toBin().getUResized(Bit5()).getRawBinaryStr().toInt(2))
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            OR("OR", false, ParamType.RD_RS1_RS2, OpCode("0000000 00000 00000 110 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() or rs2.get().toBin())
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            AND("AND", false, ParamType.RD_RS1_RS2, OpCode("0000000 00000 00000 111 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE))) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]
                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            rd.set(rs1.get().toBin() and rs2.get().toBin())
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },

            // CSR Extension
            CSRRW("CSRRW", false, ParamType.CSR_RD_OFF12_RS1, OpCode("000000000000 00000 001 00000 1110011", arrayOf(CSR, RS1, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val csrAddr = paramMap[CSR]
                    if (rdAddr != null && rs1Addr != null && csrAddr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val csr = arch.getRegByAddr(csrAddr, RV64.CSR_REGFILE_NAME)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && csr != null) {
                            if (rd.address.toHex().getRawHexStr() != "00000") {
                                val t = csr.get().toBin().getUResized(RV64.XLEN)
                                rd.set(t)
                            }

                            csr.set(rs1.get())

                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            CSRRS("CSRRS", false, ParamType.CSR_RD_OFF12_RS1, OpCode("000000000000 00000 010 00000 1110011", arrayOf(CSR, RS1, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val csrAddr = paramMap[CSR]
                    if (rdAddr != null && rs1Addr != null && csrAddr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val csr = arch.getRegByAddr(csrAddr, RV64.CSR_REGFILE_NAME)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && csr != null) {
                            if (rd.address.toHex().getRawHexStr() != "00000") {
                                val t = csr.get().toBin().getUResized(RV64.XLEN)
                                rd.set(t)
                            }

                            csr.set(rs1.get().toBin() or csr.get().toBin())

                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            CSRRC("CSRRC", false, ParamType.CSR_RD_OFF12_RS1, OpCode("000000000000 00000 011 00000 1110011", arrayOf(CSR, RS1, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val csrAddr = paramMap[CSR]
                    if (rdAddr != null && rs1Addr != null && csrAddr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val csr = arch.getRegByAddr(csrAddr, RV64.CSR_REGFILE_NAME)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && csr != null) {
                            if (rd.address.toHex().getRawHexStr() != "00000") {
                                val t = csr.get().toBin().getUResized(RV64.XLEN)
                                rd.set(t)
                            }

                            csr.set(csr.get().toBin() and rs1.get().toBin().inv())

                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            CSRRWI("CSRRWI", false, ParamType.CSR_RD_OFF12_UIMM5, OpCode("000000000000 00000 101 00000 1110011", arrayOf(CSR, UIMM5, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val uimm5 = paramMap[UIMM5]
                    val csrAddr = paramMap[CSR]
                    if (rdAddr != null && uimm5 != null && csrAddr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val csr = arch.getRegByAddr(csrAddr, RV64.CSR_REGFILE_NAME)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && csr != null) {
                            if (rd.address.toHex().getRawHexStr() != "00000") {
                                val t = csr.get().toBin().getUResized(RV64.XLEN)
                                rd.set(t)
                            }

                            csr.set(uimm5.getUResized(RV64.XLEN))

                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            CSRRSI("CSRRSI", false, ParamType.CSR_RD_OFF12_UIMM5, OpCode("000000000000 00000 110 00000 1110011", arrayOf(CSR, UIMM5, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val uimm5 = paramMap[UIMM5]
                    val csrAddr = paramMap[CSR]
                    if (rdAddr != null && uimm5 != null && csrAddr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val csr = arch.getRegByAddr(csrAddr, RV64.CSR_REGFILE_NAME)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && csr != null) {
                            if (rd.address.toHex().getRawHexStr() != "00000") {
                                val t = csr.get().toBin().getUResized(RV64.XLEN)
                                rd.set(t)
                            }

                            csr.set(csr.get().toBin() or uimm5.getUResized(RV64.XLEN))

                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            CSRRCI("CSRRCI", false, ParamType.CSR_RD_OFF12_UIMM5, OpCode("000000000000 00000 111 00000 1110011", arrayOf(CSR, UIMM5, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV64.EXTENSION.CSR.ordinal)) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val uimm5 = paramMap[UIMM5]
                    val csrAddr = paramMap[CSR]
                    if (rdAddr != null && uimm5 != null && csrAddr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val csr = arch.getRegByAddr(csrAddr, RV64.CSR_REGFILE_NAME)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && csr != null) {
                            if (rd.address.toHex().getRawHexStr() != "00000") {
                                val t = csr.get().toBin().getUResized(RV64.XLEN)
                                rd.set(t)
                            }

                            csr.set(csr.get().toBin() and uimm5.getUResized(RV64.XLEN).inv())

                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },

            // M Extension
            MUL("MUL", false, ParamType.RD_RS1_RS2, OpCode("0000001 00000 00000 000 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV64.EXTENSION.M.ordinal)) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]

                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            val factor1 = rs1.get().toBin()
                            val factor2 = rs2.get().toBin()
                            val result = factor1.flexTimesSigned(factor2)
                            rd.set(result)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            MULH("MULH", false, ParamType.RD_RS1_RS2, OpCode("0000001 00000 00000 001 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV64.EXTENSION.M.ordinal)) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]

                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            val factor1 = rs1.get().toBin()
                            val factor2 = rs2.get().toBin()
                            val result = factor1.flexTimesSigned(factor2, false).ushr(RV64.XLEN.bitWidth).getResized(RV64.XLEN)
                            rd.set(result)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            MULHSU("MULHSU", false, ParamType.RD_RS1_RS2, OpCode("0000001 00000 00000 010 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV64.EXTENSION.M.ordinal)) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]

                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            val factor1 = rs1.get().toBin()
                            val factor2 = rs2.get().toBin()
                            val result = factor1.flexTimesSigned(factor2, resizeToLargestParamSize = false, true).ushr(RV64.XLEN.bitWidth).getResized(RV64.XLEN)
                            rd.set(result)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            MULHU("MULHU", false, ParamType.RD_RS1_RS2, OpCode("0000001 00000 00000 011 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV64.EXTENSION.M.ordinal)) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]

                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            val factor1 = rs1.get().toBin()
                            val factor2 = rs2.get().toBin()
                            val result = (factor1 * factor2).toBin().ushr(RV64.XLEN.bitWidth).getUResized(RV64.XLEN)
                            rd.set(result)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            DIV("DIV", false, ParamType.RD_RS1_RS2, OpCode("0000001 00000 00000 100 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV64.EXTENSION.M.ordinal)) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]

                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            val factor1 = rs1.get().toBin()
                            val factor2 = rs2.get().toBin()
                            val result = factor1.flexDivSigned(factor2, dividendIsUnsigned = true)
                            rd.set(result)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            DIVU("DIVU", false, ParamType.RD_RS1_RS2, OpCode("0000001 00000 00000 101 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV64.EXTENSION.M.ordinal)) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]

                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            val factor1 = rs1.get().toBin()
                            val factor2 = rs2.get().toBin()
                            val result = factor1 / factor2
                            rd.set(result)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            REM("REM", false, ParamType.RD_RS1_RS2, OpCode("0000001 00000 00000 110 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV64.EXTENSION.M.ordinal)) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]

                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            val factor1 = rs1.get().toBin()
                            val factor2 = rs2.get().toBin()
                            val result = factor1.flexRemSigned(factor2)
                            rd.set(result)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            REMU("REMU", false, ParamType.RD_RS1_RS2, OpCode("0000001 00000 00000 111 00000 0110011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV64.EXTENSION.M.ordinal)) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]

                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            val factor1 = rs1.get().toBin()
                            val factor2 = rs2.get().toBin()
                            val result = factor1 % factor2
                            rd.set(result)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },

            // RV64 M Extension
            MULW("MULW", false, ParamType.RD_RS1_RS2, OpCode("0000001 00000 00000 000 00000 0111011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV64.EXTENSION.M.ordinal)) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]

                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            val factor1 = rs1.get().toBin()
                            val factor2 = rs2.get().toBin()
                            val result = factor1.flexTimesSigned(factor2).getUResized(Bit32()).getUResized(RV64.XLEN)
                            rd.set(result)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            DIVW("DIVW", false, ParamType.RD_RS1_RS2, OpCode("0000001 00000 00000 100 00000 0111011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV64.EXTENSION.M.ordinal)) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]

                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            val factor1 = rs1.get().toBin().getUResized(Bit32())
                            val factor2 = rs2.get().toBin().getUResized(Bit32())
                            val result = factor1.flexDivSigned(factor2, dividendIsUnsigned = true).getUResized(RV64.XLEN)
                            rd.set(result)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            DIVUW("DIVUW", false, ParamType.RD_RS1_RS2, OpCode("0000001 00000 00000 101 00000 0111011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV64.EXTENSION.M.ordinal)) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]

                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            val factor1 = rs1.get().toBin().getUResized(Bit32())
                            val factor2 = rs2.get().toBin().getUResized(Bit32())
                            val result = (factor1 / factor2).toBin().getUResized(RV64.XLEN)
                            rd.set(result)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            REMW("REMW", false, ParamType.RD_RS1_RS2, OpCode("0000001 00000 00000 110 00000 0111011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV64.EXTENSION.M.ordinal)) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]

                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            val factor1 = rs1.get().toBin().getUResized(Bit32())
                            val factor2 = rs2.get().toBin().getUResized(Bit32())
                            val result = factor1.flexRemSigned(factor2).getUResized(RV64.XLEN)
                            rd.set(result)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },
            REMUW("REMUW", false, ParamType.RD_RS1_RS2, OpCode("0000001 00000 00000 111 00000 0111011", arrayOf(FUNCT7, RS2, RS1, FUNCT3, RD, OPCODE)), needFeatures = listOf(RV64.EXTENSION.M.ordinal)) {
                override fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                    super.execute(arch, paramMap)
                    val rdAddr = paramMap[RD]
                    val rs1Addr = paramMap[RS1]
                    val rs2Addr = paramMap[RS2]

                    if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                        val rd = arch.getRegByAddr(rdAddr)
                        val rs1 = arch.getRegByAddr(rs1Addr)
                        val rs2 = arch.getRegByAddr(rs2Addr)
                        val pc = arch.getRegContainer().pc
                        if (rd != null && rs1 != null && rs2 != null) {
                            val factor1 = rs1.get().toBin().getUResized(Bit32())
                            val factor2 = rs2.get().toBin().getUResized(Bit32())
                            val result = (factor1 % factor2).toBin().getUResized(RV64.XLEN)
                            rd.set(result)
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            },

            // Pseudo
            Nop("NOP", true, ParamType.PS_NONE),
            Mv("MV", true, ParamType.PS_RD_RS1),
            Li28Unsigned("LI", true, ParamType.PS_RD_LI_I28Unsigned, memWords = 2),
            Li32Signed("LI", true, ParamType.PS_RD_LI_I32Signed, memWords = 2),
            Li40Unsigned("LI", true, ParamType.PS_RD_LI_I40Unsigned, memWords = 4),
            Li52Unsigned("LI", true, ParamType.PS_RD_LI_I52Unsigned, memWords = 6),
            Li64("LI", true, ParamType.PS_RD_LI_I64, memWords = 8),
            La64("LA", true, ParamType.PS_RD_Albl, memWords = 8),
            Not("NOT", true, ParamType.PS_RD_RS1),
            Neg("NEG", true, ParamType.PS_RD_RS1),
            Seqz("SEQZ", true, ParamType.PS_RD_RS1),
            Snez("SNEZ", true, ParamType.PS_RD_RS1),
            Sltz("SLTZ", true, ParamType.PS_RD_RS1),
            Sgtz("SGTZ", true, ParamType.PS_RD_RS1),
            Beqz("BEQZ", true, ParamType.PS_RS1_Jlbl),
            Bnez("BNEZ", true, ParamType.PS_RS1_Jlbl),
            Blez("BLEZ", true, ParamType.PS_RS1_Jlbl),
            Bgez("BGEZ", true, ParamType.PS_RS1_Jlbl),
            Bltz("BLTZ", true, ParamType.PS_RS1_Jlbl),
            BGTZ("BGTZ", true, ParamType.PS_RS1_Jlbl),
            Bgt("BGT", true, ParamType.PS_RS1_RS2_Jlbl),
            Ble("BLE", true, ParamType.PS_RS1_RS2_Jlbl),
            Bgtu("BGTU", true, ParamType.PS_RS1_RS2_Jlbl),
            Bleu("BLEU", true, ParamType.PS_RS1_RS2_Jlbl),
            J("J", true, ParamType.PS_Jlbl),
            JAL1("JAL", true, ParamType.PS_RS1_Jlbl, relative = JAL),
            JAL2("JAL", true, ParamType.PS_Jlbl, relative = JAL),
            Jr("JR", true, ParamType.PS_RS1),
            JALR1("JALR", true, ParamType.PS_RS1, relative = JALR),
            JALR2("JALR", true, ParamType.RD_Off12, relative = JALR),
            Ret("RET", true, ParamType.PS_NONE),
            Call("CALL", true, ParamType.PS_Jlbl, memWords = 2),
            Tail("TAIL", true, ParamType.PS_Jlbl, memWords = 2);

            open fun execute(arch: Architecture, paramMap: Map<RV64BinMapper.MaskLabel, Bin>) {
                arch.getConsole().log("executing $id ...")
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
        val content = RV64.TS_COMPILED_HEADERS.entries.associateWith { Entry(Orientation.CENTER, "") }.toMutableMap()

        init {
            content[RV64.TS_COMPILED_HEADERS.Address] = Entry(Orientation.CENTER, getAddresses().first().toHex().getRawHexStr())
        }

        fun addLabel(label: R_JLBL) {
            content[RV64.TS_COMPILED_HEADERS.Label] = Entry(Orientation.LEFT, label.label.wholeName)
        }

        fun addInstr(instr: R_INSTR) {
            content[RV64.TS_COMPILED_HEADERS.Instruction] = Entry(Orientation.LEFT, "${instr.instrType.id}${if (instr.instrType.pseudo && instr.instrType.relative == null) "\t(pseudo)" else ""}")
            content[RV64.TS_COMPILED_HEADERS.Parameters] = Entry(Orientation.LEFT, instr.paramcoll?.paramsWithOutSplitSymbols?.joinToString(",\t") { paramcoll -> paramcoll.paramTokens.joinToString("") { it.content } } ?: "")
        }

        override fun getContent(): List<Entry> {
            return RV64.TS_COMPILED_HEADERS.entries.map { content[it] ?: Entry(Orientation.CENTER, "") }
        }
    }

}