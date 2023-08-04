package extendable.archs.riscv

import extendable.Architecture
import extendable.archs.riscv.RISCVGrammar.E_DIRECTIVE.DirType.*
import extendable.components.assembly.Compiler
import extendable.components.assembly.Grammar
import extendable.components.connected.FileHandler
import extendable.components.types.MutVal
import tools.DebugTools

class RISCVGrammar() : Grammar() {

    override val applyStandardHLForRest: Boolean = false

    override fun clear() {

    }

    override fun check(compiler: Compiler, tokenLines: List<List<Compiler.Token>>, files: List<FileHandler.File>): GrammarTree {

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
        var importHasErrors: Boolean = false
        for (lineID in remainingLines.indices) {
            val lineStr = remainingLines[lineID].joinToString("") { it.content }
            SyntaxRegex.pre_import.matchEntire(lineStr)?.let {
                val filename = it.groupValues[2]
                var linkedFile: FileHandler.File? = null
                var linkedTree: GrammarTree? = null
                for (file in files) {
                    if (file.getName() == filename) {
                        linkedFile = file
                        linkedTree = file.getLinkedTree()
                    }
                }

                if (linkedTree != null) {
                    // Get File Tree
                    linkedTree.rootNode?.let { root ->
                        if (root.allErrors.isNotEmpty()) {
                            errors.add(Grammar.Error("File {filename: $filename} has errors!", *remainingLines[lineID].toTypedArray()))
                        } else {
                            val linked_c_section = root.containers.filter { it is C_SECTIONS }
                            val linked_sections: List<TreeNode.SectionNode> = linked_c_section.flatMap { it.nodes.toList() }.filterIsInstance<TreeNode.SectionNode>()

                            imports.addAll(linked_sections)
                            pres.add(Pre_IMPORT(*remainingLines[lineID].toTypedArray()))
                        }
                    }
                } else {
                    errors.add(Grammar.Error("File {filename: $filename} not ${if (linkedFile != null) "compiled" else "found"}!", *remainingLines[lineID].toTypedArray()))
                }
                remainingLines[lineID] = emptyList()
            }
        }

        if (DebugTools.RISCV_showGrammarScanTiers) {
            console.log("Grammar: IMPORTS -> ${
                imports.flatMap { it.collNodes.toList() }.filter { it is R_JLBL || it is R_ILBL || it is R_ULBL }.joinToString { "\n\t ${it.name}" }
            }")
        }

        // ----------------- remove Comments
        for (lineID in remainingLines.indices) {
            val lineStr = remainingLines[lineID].joinToString("") { it.content }
            SyntaxRegex.pre_comment.matchEntire(lineStr)?.let {
                pres.add(Pre_COMMENT(*remainingLines[lineID].toTypedArray()))
                remainingLines[lineID] = emptyList()
            }
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
        // ----------------- replace equ constants
        // find definitions
        val equs = mutableListOf<EquDefinition>()
        for (lineID in remainingLines.indices) {
            val lineStr = remainingLines[lineID].joinToString("") { it.content }
            SyntaxRegex.pre_equ_def.matchEntire(lineStr)?.let {
                val name = compiler.pseudoAnalyze(it.groupValues[2])
                val const = compiler.pseudoAnalyze(it.groupValues[3])

                val nameMatch = (name.size == 1 && (name.first() is Compiler.Token.Word || name.first() is Compiler.Token.AlphaNum))
                val constMatch = (const.size == 1 && const.first() is Compiler.Token.Constant)

                if (nameMatch && constMatch) {
                    equs.add(EquDefinition(name.first(), const.first()))
                    pres.add(Pre_EQU(*remainingLines[lineID].toTypedArray()))
                } else {
                    var message = ""
                    if (!nameMatch) {
                        message += "{name: ${it.groupValues[2]}} is not a valid name for an equ definition! "
                    }
                    if (!constMatch) {
                        message += "{constant: ${it.groupValues[3]}} is not a valid constant for an equ definition! "
                    }
                    errors.add(Grammar.Error(message, *remainingLines[lineID].toTypedArray()))
                }
                remainingLines[lineID] = emptyList()
            }
        }
        // resolve definitions
        for (lineID in remainingLines.indices) {
            val tokenList = remainingLines[lineID].toMutableList()
            for (tokenID in tokenList.indices) {
                val token = tokenList[tokenID]
                for (equ in equs) {
                    if (token.content == equ.name.content) {
                        pres.add(Pre_EQU(token))
                        tokenList[tokenID] = equ.constant
                    }
                }
            }
            remainingLines[lineID] = tokenList
        }

        // ----------------- replace macros
        // define macros
        val macros = mutableListOf<MacroDefinition>()
        var foundStart = false
        var name = ""
        val arguments = mutableListOf<String>()
        val replacementLines = mutableListOf<String>()
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

                    macroTokens.addAll(remainingLines[lineID])

                    if (validArgs) {
                        foundStart = true
                    } else {
                        errors.add(Grammar.Error("Macro arguments {${arguments.joinToString(",") { it }}} not alpha numeric!", *macroTokens.toTypedArray()))
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
                    pres.add(Pre_MACRO(*macroTokens.toTypedArray()))

                    for (argusage in useMap) {
                        if (argusage.value == false) {
                            warnings.add(Warning("Unused argument (${argusage.key}) in macro definition!", *macroTokens.toTypedArray()))
                        }
                        if (!arguments.contains(argusage.key)) {
                            warnings.add(Warning("Found usage of argument (${argusage.key}) which isn't defined in macro!", *macroTokens.toTypedArray()))
                        }
                    }
                    macroTokens.clear()
                } else {
                    replacementLines.add(lineStr)
                    macroTokens.addAll(remainingLines[lineID])
                }
                remainingLines[lineID] = emptyList()
            }
        }
        if (foundStart) {
            errors.add(Grammar.Error("Macro definition has no ending (.endm)!", *macroTokens.toTypedArray()))
        }

        // resolve macros
        for (lineID in remainingLines.indices) {
            val lineStr = remainingLines[lineID].joinToString("") { it.content }
            SyntaxRegex.pre_macro_line.matchEntire(lineStr)?.let { matchResult ->
                val resultName = matchResult.groupValues[1]
                val argumentContent = mutableListOf<String>()
                matchResult.groupValues[2].split(",").forEach {
                    argumentContent.add(it.trimStart().trimEnd())
                }

                for (macro in macros) {
                    if (resultName == macro.name && argumentContent.size == macro.arguments.size) {
                        pres.add(Pre_MACRO(*remainingLines[lineID].toTypedArray()))
                        remainingLines.removeAt(lineID)

                        // actual replacement
                        for (macroLine in macro.replacementLines.reversed()) {
                            var replacedLine = macroLine
                            for (attrID in macro.arguments.indices) {
                                replacedLine = replacedLine.replace("""\""" + macro.arguments[attrID], argumentContent[attrID])
                            }
                            if (DebugTools.RISCV_showGrammarScanTiers) {
                                console.log("\tmacro insert line ${lineID + 1}: $replacedLine")
                            }
                            remainingLines.add(lineID, compiler.pseudoAnalyze(replacedLine))
                        }
                    }
                }
            }
        }

        /**
         * FIND UNRESOLVED
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
        c_pres = C_PRES(*pres.toTypedArray())
        if (DebugTools.RISCV_showGrammarScanTiers) {
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
                    val offsetResult = E_PARAM.Offset.Syntax.tokenSequence.matches(*paramBuffer.toTypedArray())
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
                    var link: E_PARAM.Link? = null
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

        if (DebugTools.RISCV_showGrammarScanTiers) {
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
                        errors.add(Grammar.Error((if (!eDir.isDataEmitting()) "Not a data emitting directive for" else "Invalid parameter count for") + " initialized label!", nodes = lineElements.toTypedArray()))
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
                        errors.add(Grammar.Error("Not a data emitting directive for uninitialized label!", nodes = lineElements.toTypedArray()))
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
                    rowNode = R_JLBL(eLabel, !eLabel.isSubLabel)
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
                    errors.add(Grammar.Error("Found directive type which shouldn't indicate a section start!", *lineElements.toTypedArray()))
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
                    rowNode = R_INSTR(eInstrName, eParamcoll, checkedType, lastMainJLBL, if (!startIsDefined) true else false)
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
                errors.add(Grammar.Error("couldn't match Elements to RiscV Row!", *lineElements.toTypedArray()))
            }
        }

        /**
         * FINISH ROW SCAN
         */
        if (DebugTools.RISCV_showGrammarScanTiers) {
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
                errors.add(Grammar.Error("Couldn't match row into section!", firstRow))
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
        if (DebugTools.RISCV_showGrammarScanTiers) {
            console.log("Grammar: SECTIONS Scan -> ${sections.joinToString("") { "\n\tsection ${sections.indexOf(it) + 1}: ${it.name}" }}")
        }
        /**
         *  -------------------------------------------------------------- CONTAINER SCAN --------------------------------------------------------------
         *  usage:
         */

        c_sections = C_SECTIONS(*sections.toTypedArray(), *imports.toTypedArray())

        /**
         * FINISH CONTAINER SCAN
         */

        val rootNode = TreeNode.RootNode(errors, warnings, c_sections, c_pres)

        return GrammarTree(rootNode)
    }

    /* -------------------------------------------------------------- TREE COMPONENTS -------------------------------------------------------------- */
    object SyntaxRegex {
        val pre_macro_start = Regex("""^\s*(\.macro\s+([a-zA-Z0-9_]+)\s+((?:[-'"a-zA-Z0-9_]+\s*,\s*)*[-'"a-zA-Z0-9_]+))\s*?""")
        val pre_macro_arg_def = Regex("""^\s*([a-zA-Z0-9_]+)\s*?""")
        val pre_macro_line = Regex("""^\s*([a-zA-Z0-9_]+)\s+((?:[a-zA-Z0-9_]+\s*,\s*)*[a-zA-Z0-9_]+)\s*?""")
        val pre_macro_arg_link = Regex("""\\([a-zA-Z0-9_]+)""")
        val pre_macro_end = Regex("""\.endm""")
        val pre_import = Regex("""^\s*(#import\s+"(.+)")\s*?""")
        val pre_comment = Regex("""^\s*#.*?""")
        val pre_equ_def = Regex("""^\s*(\.equ\s+(.+)\s*,\s*(.+))\s*?""")
        val pre_option = Regex("""^\s*(\.option\s+.+)\s*?""")
        val pre_attribute = Regex("""^\s*(\.attribute\s+.+)\s*?""")
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
            ".equ" to ".equ [name], [value]",
            ".macro" to "\n.macro [name] [attr1, ..., attrn]\n\t[macro usage referencing attribs with starting '\\']\n\t...\n.endm\n"
        )
    }

    object REFS {
        val REF_PRE_IMPORT = "PRE_import"
        val REF_PRE_COMMENT = "PRE_comment"
        val REF_PRE_OPTION = "PRE_option"
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

    class Pre_IMPORT(vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RISCVFlags.pre_import), REFS.REF_PRE_IMPORT, *tokens)
    class Pre_COMMENT(vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RISCVFlags.comment), REFS.REF_PRE_COMMENT, *tokens)
    class Pre_OPTION(vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RISCVFlags.pre_option), REFS.REF_PRE_OPTION, *tokens)
    class Pre_ATTRIBUTE(vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RISCVFlags.pre_attribute), REFS.REF_PRE_ATTRIBUTE, *tokens)
    class Pre_MACRO(vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RISCVFlags.pre_macro), REFS.REF_PRE_MACRO, *tokens)
    class Pre_EQU(vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RISCVFlags.pre_equ), REFS.REF_PRE_EQU, *tokens)

    /* -------------------------------------------------------------- ELEMENTS -------------------------------------------------------------- */
    class E_INSTRNAME(val insToken: Compiler.Token.Word, vararg val types: R_INSTR.InstrType) : TreeNode.ElementNode(ConnectedHL(RISCVFlags.instruction), REFS.REF_E_INSTRNAME, insToken) {
        fun check(paramcoll: E_PARAMCOLL = E_PARAMCOLL()): R_INSTR.InstrType? {
            val params = paramcoll.paramsWithOutSplitSymbols
            var type: R_INSTR.InstrType = types.first()
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
                            params[0] is E_PARAM.Register &&
                                    params[1] is E_PARAM.Offset
                        } else {
                            false
                        }
                    }

                    R_INSTR.ParamType.RS2_Off5 -> {
                        matches = if (params.size == 2) {
                            params[0] is E_PARAM.Register &&
                                    params[1] is E_PARAM.Offset
                        } else {
                            false
                        }
                    }

                    R_INSTR.ParamType.RD_RS1_RS1 -> {
                        matches = if (params.size == 3) {
                            params[0] is E_PARAM.Register &&
                                    params[1] is E_PARAM.Register &&
                                    params[2] is E_PARAM.Register
                        } else {
                            false
                        }
                    }

                    R_INSTR.ParamType.RD_RS1_I12 -> {
                        matches = if (params.size == 3) {
                            params[0] is E_PARAM.Register &&
                                    params[1] is E_PARAM.Register &&
                                    params[2] is E_PARAM.Constant
                        } else {
                            false
                        }
                    }

                    R_INSTR.ParamType.RD_RS1_I5 -> {
                        matches = if (params.size == 3) {
                            params[0] is E_PARAM.Register &&
                                    params[1] is E_PARAM.Register &&
                                    params[2] is E_PARAM.Constant
                        } else {
                            false
                        }
                    }

                    R_INSTR.ParamType.RS1_RS2_I12 -> {
                        matches = if (params.size == 3) {
                            params[0] is E_PARAM.Register &&
                                    params[1] is E_PARAM.Register &&
                                    params[2] is E_PARAM.Constant
                        } else {
                            false
                        }
                    }

                    R_INSTR.ParamType.PS_RS1_RS2_Jlbl -> {
                        matches = if (params.size == 3) {
                            params[0] is E_PARAM.Register &&
                                    params[1] is E_PARAM.Register &&
                                    (params[2] is E_PARAM.Link && params[2].ifLinkGetLinkType() == E_PARAM.Link.LinkTypes.JLBL)
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

        fun getValues(): Array<MutVal.Value.Binary> {
            val values = mutableListOf<MutVal.Value.Binary>()
            paramsWithOutSplitSymbols.forEach {
                when (it) {
                    is E_PARAM.Constant -> {
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

        class Offset(val offset: Compiler.Token.Constant, openParan: Compiler.Token, val register: Compiler.Token.Register, closeParan: Compiler.Token) : E_PARAM(REFS.REF_E_PARAM_OFFSET, RISCVFlags.offset, offset, openParan, register, closeParan) {
            fun getValueArray(): Array<MutVal.Value.Binary> {
                return arrayOf(offset.getValue().toBin(), register.reg.address.toBin())
            }

            object Syntax {
                val tokenSequence = TokenSequence(TokenSequence.Component.InSpecific.Constant(), TokenSequence.Component.Specific("("), TokenSequence.Component.InSpecific.Register(), TokenSequence.Component.Specific(")"), ignoreSpaces = true)
            }
        }

        class Constant(val constant: Compiler.Token.Constant) : E_PARAM(REFS.REF_E_PARAM_CONSTANT, RISCVFlags.constant, constant) {
            fun getValue(): MutVal.Value.Binary {
                return constant.getValue().toBin()
            }
        }

        class Register(val register: Compiler.Token.Register) : E_PARAM(REFS.REF_E_PARAM_REGISTER, RISCVFlags.register, register) {
            fun getAddress(): MutVal.Value.Binary {
                return register.reg.address.toBin()
            }
        }

        class SplitSymbol(val splitSymbol: Compiler.Token.Symbol) : E_PARAM(REFS.REF_E_PARAM_SPLITSYMBOL, RISCVFlags.instruction, splitSymbol)
        class Link(vararg val labelName: Compiler.Token) : E_PARAM(REFS.REF_E_PARAM_LABELLINK, RISCVFlags.label, *labelName) {
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
                JLBL,
                ULBL,
                ILBL
            }

        }
    }

    class E_LABEL(vararg val labelName: Compiler.Token, colon: Compiler.Token.Symbol, val sublblFrom: E_LABEL? = null) : TreeNode.ElementNode(ConnectedHL(RISCVFlags.label), REFS.REF_E_LABEL, *labelName, colon) {

        val wholeName: String
        val tokenSequence: TokenSequence
        val isSubLabel: Boolean

        init {
            isSubLabel = sublblFrom != null

            wholeName = (if (sublblFrom != null) sublblFrom.wholeName else "") + labelName.joinToString("") { it.content }
            val tokenComponents = mutableListOf<TokenSequence.Component>()
            for (token in labelName) {
                tokenComponents.add(TokenSequence.Component.Specific(token.content))
            }
            tokenSequence = TokenSequence(*tokenComponents.toTypedArray(), ignoreSpaces = false)
        }
    }

    class E_DIRECTIVE(val type: DirType, val dot: Compiler.Token.Symbol, vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RISCVFlags.directive), REFS.REF_E_DIRECTIVE, dot, *tokens) {

        fun isDataEmitting(): Boolean {
            return type.majorType == MajorType.DE_ALIGNED || type.majorType == MajorType.DE_UNALIGNED
        }

        enum class MajorType {
            SECTIONSTART,
            DE_ALIGNED,
            DE_UNALIGNED
        }

        enum class DirType(val dirname: String, val majorType: MajorType) {
            TEXT("text", MajorType.SECTIONSTART),
            DATA("data", MajorType.SECTIONSTART),
            RODATA("rodata", MajorType.SECTIONSTART),
            BSS("bss", MajorType.SECTIONSTART),

            BYTE("byte", MajorType.DE_ALIGNED),
            HALF("half", MajorType.DE_ALIGNED),
            WORD("word", MajorType.DE_ALIGNED),
            DWORD("dword", MajorType.DE_ALIGNED),
            ASCIZ("asciz", MajorType.DE_ALIGNED),
            STRING("string", MajorType.DE_ALIGNED),

            BYTE_2("2byte", MajorType.DE_UNALIGNED),
            BYTE_4("4byte", MajorType.DE_UNALIGNED),
            BYTE_8("8byte", MajorType.DE_UNALIGNED),
        }
    }

    /* -------------------------------------------------------------- ROWS -------------------------------------------------------------- */
    class R_SECSTART(val directive: E_DIRECTIVE) : TreeNode.RowNode(REFS.REF_R_SECSTART, directive)
    class R_JLBL(val label: E_LABEL, val isMainLabel: Boolean) : TreeNode.RowNode(REFS.REF_R_JLBL, label)
    class R_ULBL(val label: E_LABEL, val directive: E_DIRECTIVE) : TreeNode.RowNode(REFS.REF_R_ULBL, label, directive)
    class R_ILBL(val label: E_LABEL, val directive: E_DIRECTIVE, val paramcoll: E_PARAMCOLL) : TreeNode.RowNode(REFS.REF_R_ILBL, label, directive, paramcoll)
    class R_INSTR(val instrname: E_INSTRNAME, val paramcoll: E_PARAMCOLL?, val instrType: InstrType, val lastMainLabel: R_JLBL? = null, val globlStart: Boolean = false) : TreeNode.RowNode(REFS.REF_R_INSTR, instrname, paramcoll) {
        enum class ParamType(val pseudo: Boolean, val exampleString: String) {
            // NORMAL INSTRUCTIONS
            RD_I20(false, "rd, imm20"), // rd, imm
            RD_Off12(false, "rd, imm12(rs)"), // rd, imm12(rs)
            RS2_Off5(false, "rs2, imm5(rs1)"), // rs2, imm5(rs1)
            RD_RS1_RS1(false, "rd, rs1, rs2"), // rd, rs1, rs2
            RD_RS1_I12(false, "rd, rs1, imm12"), // rd, rs, imm
            RD_RS1_I5(false, "rd, rs1, shamt5"), // rd, rs, shamt
            RS1_RS2_I12(false, "rs1, rs2, imm12"), // rs1, rs2, imm

            // PSEUDO INSTRUCTIONS
            PS_RS1_RS2_Jlbl(true, "rs1, rs2, jlabel"),
            PS_RD_I32(true, "rd, imm32"), // rd, imm
            PS_RS1_Jlbl(true, "rs, jlabel"), // rs, label
            PS_RD_Albl(true, "rd, alabel"), // rd, label
            PS_Jlbl(true, "jlabel"),  // label
            PS_RD_RS1(true, "rd, rs"), // rd, rs
            PS_RS1(true, "rs1"),

            // NONE PARAM INSTR
            NONE(false, "none"),
            PS_NONE(true, "none")
        }

        enum class InstrType(val id: String, val pseudo: Boolean, val paramType: ParamType, val opCode: RISCVBinMapper.OpCode? = null, val memWords: Int = 1, val relative: InstrType? = null) {
            LUI("LUI", false, ParamType.RD_I20, RISCVBinMapper.OpCode("00000000000000000000 00000 0110111", arrayOf(RISCVBinMapper.MaskLabel.IMM20, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    if (rdAddr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val imm20 = paramMap.get(RISCVBinMapper.MaskLabel.IMM20)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && imm20 != null) {
                            val shiftedIMM32 = imm20.getUResized(MutVal.Size.Bit32()) shl 12
                            rd.set(shiftedIMM32)
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            AUIPC("AUIPC", false, ParamType.RD_I20, RISCVBinMapper.OpCode("00000000000000000000 00000 0010111", arrayOf(RISCVBinMapper.MaskLabel.IMM20, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    if (rdAddr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val imm20 = paramMap.get(RISCVBinMapper.MaskLabel.IMM20)
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
            JAL("JAL", false, ParamType.RD_I20, RISCVBinMapper.OpCode("00000000000000000000 00000 1101111", arrayOf(RISCVBinMapper.MaskLabel.IMM20, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    if (rdAddr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val imm20 = paramMap.get(RISCVBinMapper.MaskLabel.IMM20)
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
            JALR(
                "JALR",
                false,
                ParamType.RD_Off12,
                RISCVBinMapper.OpCode("000000000000 00000 000 00000 1100111", arrayOf(RISCVBinMapper.MaskLabel.IMM12, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm12 = paramMap.get(RISCVBinMapper.MaskLabel.IMM12)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val jumpAddr = rs1.get() + imm12.getResized(MutVal.Size.Bit32())
                            rd.set(pc.value.get() + MutVal.Value.Hex("4"))
                            pc.value.set(jumpAddr)
                        }
                    }
                }
            },
            ECALL(
                "ECALL", false,
                ParamType.NONE,
                RISCVBinMapper.OpCode("000000000000 00000 000 00000 1110011", arrayOf(RISCVBinMapper.MaskLabel.NONE, RISCVBinMapper.MaskLabel.NONE, RISCVBinMapper.MaskLabel.NONE, RISCVBinMapper.MaskLabel.NONE, RISCVBinMapper.MaskLabel.OPCODE))
            ),
            EBREAK(
                "EBREAK", false,
                ParamType.NONE,
                RISCVBinMapper.OpCode("000000000001 00000 000 00000 1110011", arrayOf(RISCVBinMapper.MaskLabel.NONE, RISCVBinMapper.MaskLabel.NONE, RISCVBinMapper.MaskLabel.NONE, RISCVBinMapper.MaskLabel.NONE, RISCVBinMapper.MaskLabel.OPCODE))
            ),
            BEQ(
                "BEQ", false,
                ParamType.RS1_RS2_I12,
                RISCVBinMapper.OpCode("0000000 00000 00000 000 00000 1100011", arrayOf(RISCVBinMapper.MaskLabel.IMM7, RISCVBinMapper.MaskLabel.RS2, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.IMM5, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val rs2Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS2)
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm7 = paramMap.get(RISCVBinMapper.MaskLabel.IMM7)
                        val imm5 = paramMap.get(RISCVBinMapper.MaskLabel.IMM5)
                        val pc = architecture.getRegisterContainer().pc
                        if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                            val imm7str = imm7.getResized(MutVal.Size.Bit7()).getRawBinaryStr()
                            val imm5str = imm5.getResized(MutVal.Size.Bit5()).getRawBinaryStr()
                            val imm12 = MutVal.Value.Binary(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), MutVal.Size.Bit12())

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
            BNE(
                "BNE", false,
                ParamType.RS1_RS2_I12,
                RISCVBinMapper.OpCode("0000000 00000 00000 001 00000 1100011", arrayOf(RISCVBinMapper.MaskLabel.IMM7, RISCVBinMapper.MaskLabel.RS2, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.IMM5, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val rs2Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS2)
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm7 = paramMap.get(RISCVBinMapper.MaskLabel.IMM7)
                        val imm5 = paramMap.get(RISCVBinMapper.MaskLabel.IMM5)
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
            BLT(
                "BLT", false,
                ParamType.RS1_RS2_I12,
                RISCVBinMapper.OpCode("0000000 00000 00000 100 00000 1100011", arrayOf(RISCVBinMapper.MaskLabel.IMM7, RISCVBinMapper.MaskLabel.RS2, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.IMM5, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val rs2Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS2)
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm7 = paramMap.get(RISCVBinMapper.MaskLabel.IMM7)
                        val imm5 = paramMap.get(RISCVBinMapper.MaskLabel.IMM5)
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
            BGE(
                "BGE", false,
                ParamType.RS1_RS2_I12,
                RISCVBinMapper.OpCode("0000000 00000 00000 101 00000 1100011", arrayOf(RISCVBinMapper.MaskLabel.IMM7, RISCVBinMapper.MaskLabel.RS2, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.IMM5, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val rs2Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS2)
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm7 = paramMap.get(RISCVBinMapper.MaskLabel.IMM7)
                        val imm5 = paramMap.get(RISCVBinMapper.MaskLabel.IMM5)
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
            BLTU(
                "BLTU", false,
                ParamType.RS1_RS2_I12,
                RISCVBinMapper.OpCode("0000000 00000 00000 110 00000 1100011", arrayOf(RISCVBinMapper.MaskLabel.IMM7, RISCVBinMapper.MaskLabel.RS2, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.IMM5, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val rs2Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS2)
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm7 = paramMap.get(RISCVBinMapper.MaskLabel.IMM7)
                        val imm5 = paramMap.get(RISCVBinMapper.MaskLabel.IMM5)
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
            BGEU(
                "BGEU", false,
                ParamType.RS1_RS2_I12,
                RISCVBinMapper.OpCode("0000000 00000 00000 111 00000 1100011", arrayOf(RISCVBinMapper.MaskLabel.IMM7, RISCVBinMapper.MaskLabel.RS2, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.IMM5, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val rs2Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS2)
                    if (rs2Addr != null && rs1Addr != null) {
                        val rs2 = architecture.getRegisterContainer().getRegister(rs2Addr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm7 = paramMap.get(RISCVBinMapper.MaskLabel.IMM7)
                        val imm5 = paramMap.get(RISCVBinMapper.MaskLabel.IMM5)
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
            BEQ1("BEQ", true, ParamType.PS_RS1_RS2_Jlbl, relative = BEQ),
            BNE1("BNE", true, ParamType.PS_RS1_RS2_Jlbl, relative = BNE),
            BLT1("BLT", true, ParamType.PS_RS1_RS2_Jlbl, relative = BLT),
            BGE1("BGE", true, ParamType.PS_RS1_RS2_Jlbl, relative = BGE),
            BLTU1("BLTU", true, ParamType.PS_RS1_RS2_Jlbl, relative = BLTU),
            BGEU1("BGEU", true, ParamType.PS_RS1_RS2_Jlbl, relative = BGEU),
            LB(
                "LB", false,
                ParamType.RD_Off12,
                RISCVBinMapper.OpCode("000000000000 00000 000 00000 0000011", arrayOf(RISCVBinMapper.MaskLabel.IMM12, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val imm12 = paramMap.get(RISCVBinMapper.MaskLabel.IMM12)
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
            LH(
                "LH", false,
                ParamType.RD_Off12,
                RISCVBinMapper.OpCode("000000000000 00000 001 00000 0000011", arrayOf(RISCVBinMapper.MaskLabel.IMM12, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val imm12 = paramMap.get(RISCVBinMapper.MaskLabel.IMM12)
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
            LW(
                "LW", false,
                ParamType.RD_Off12,
                RISCVBinMapper.OpCode("000000000000 00000 010 00000 0000011", arrayOf(RISCVBinMapper.MaskLabel.IMM12, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val imm12 = paramMap.get(RISCVBinMapper.MaskLabel.IMM12)
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
            LBU(
                "LBU", false,
                ParamType.RD_Off12,
                RISCVBinMapper.OpCode("000000000000 00000 100 00000 0000011", arrayOf(RISCVBinMapper.MaskLabel.IMM12, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val imm12 = paramMap.get(RISCVBinMapper.MaskLabel.IMM12)
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
            LHU(
                "LHU", false,
                ParamType.RD_Off12,
                RISCVBinMapper.OpCode("000000000000 00000 101 00000 0000011", arrayOf(RISCVBinMapper.MaskLabel.IMM12, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val imm12 = paramMap.get(RISCVBinMapper.MaskLabel.IMM12)
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
            SB(
                "SB", false,
                ParamType.RS2_Off5,
                RISCVBinMapper.OpCode("0000000 00000 00000 000 00000 0100011", arrayOf(RISCVBinMapper.MaskLabel.IMM7, RISCVBinMapper.MaskLabel.RS2, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.IMM5, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val rs2Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS2)
                    val imm5 = paramMap.get(RISCVBinMapper.MaskLabel.IMM5)
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
            SH(
                "SH", false,
                ParamType.RS2_Off5,
                RISCVBinMapper.OpCode("0000000 00000 00000 001 00000 0100011", arrayOf(RISCVBinMapper.MaskLabel.IMM7, RISCVBinMapper.MaskLabel.RS2, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.IMM5, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val rs2Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS2)
                    val imm5 = paramMap.get(RISCVBinMapper.MaskLabel.IMM5)
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
            SW(
                "SW", false,
                ParamType.RS2_Off5,
                RISCVBinMapper.OpCode("0000000 00000 00000 010 00000 0100011", arrayOf(RISCVBinMapper.MaskLabel.IMM7, RISCVBinMapper.MaskLabel.RS2, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.IMM5, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val rs2Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS2)
                    val imm5 = paramMap.get(RISCVBinMapper.MaskLabel.IMM5)
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
            ADDI(
                "ADDI", false,
                ParamType.RD_RS1_I12,
                RISCVBinMapper.OpCode("000000000000 00000 000 00000 0010011", arrayOf(RISCVBinMapper.MaskLabel.IMM12, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm12 = paramMap.get(RISCVBinMapper.MaskLabel.IMM12)
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
            SLTI(
                "SLTI", false,
                ParamType.RD_RS1_I12,
                RISCVBinMapper.OpCode("000000000000 00000 010 00000 0010011", arrayOf(RISCVBinMapper.MaskLabel.IMM12, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm12 = paramMap.get(RISCVBinMapper.MaskLabel.IMM12)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm32 = imm12.getResized(MutVal.Size.Bit32())
                            rd.set(if (rs1.get().toDec() < paddedImm32.toDec()) MutVal.Value.Binary("1", MutVal.Size.Bit32()) else MutVal.Value.Binary("0", MutVal.Size.Bit32()))
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            SLTIU(
                "SLTIU", false,
                ParamType.RD_RS1_I12,
                RISCVBinMapper.OpCode("000000000000 00000 011 00000 0010011", arrayOf(RISCVBinMapper.MaskLabel.IMM12, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm12 = paramMap.get(RISCVBinMapper.MaskLabel.IMM12)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm32 = imm12.getUResized(MutVal.Size.Bit32())
                            rd.set(if (rs1.get().toBin() < paddedImm32) MutVal.Value.Binary("1", MutVal.Size.Bit32()) else MutVal.Value.Binary("0", MutVal.Size.Bit32()))
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            XORI(
                "XORI", false,
                ParamType.RD_RS1_I12,
                RISCVBinMapper.OpCode("000000000000 00000 100 00000 0010011", arrayOf(RISCVBinMapper.MaskLabel.IMM12, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm12 = paramMap.get(RISCVBinMapper.MaskLabel.IMM12)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm32 = imm12.getUResized(MutVal.Size.Bit32())
                            rd.set(rs1.get().toBin() xor paddedImm32)
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            ORI(
                "ORI", false,
                ParamType.RD_RS1_I12,
                RISCVBinMapper.OpCode("000000000000 00000 110 00000 0010011", arrayOf(RISCVBinMapper.MaskLabel.IMM12, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm12 = paramMap.get(RISCVBinMapper.MaskLabel.IMM12)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm32 = imm12.getUResized(MutVal.Size.Bit32())
                            rd.set(rs1.get().toBin() or paddedImm32)
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            ANDI(
                "ANDI", false,
                ParamType.RD_RS1_I12,
                RISCVBinMapper.OpCode("000000000000 00000 111 00000 0010011", arrayOf(RISCVBinMapper.MaskLabel.IMM12, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val imm12 = paramMap.get(RISCVBinMapper.MaskLabel.IMM12)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && imm12 != null && rs1 != null) {
                            val paddedImm32 = imm12.getUResized(MutVal.Size.Bit32())
                            rd.set(rs1.get().toBin() and paddedImm32)
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            SLLI(
                "SLLI", false,
                ParamType.RD_RS1_I5,
                RISCVBinMapper.OpCode("0000000 00000 00000 001 00000 0010011", arrayOf(RISCVBinMapper.MaskLabel.FUNCT7, RISCVBinMapper.MaskLabel.SHAMT, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val shamt = paramMap.get(RISCVBinMapper.MaskLabel.SHAMT)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && shamt != null && rs1 != null) {
                            rd.set(rs1.get().toBin() ushl shamt.getRawBinaryStr().toInt(2))
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            SRLI(
                "SRLI", false,
                ParamType.RD_RS1_I5,
                RISCVBinMapper.OpCode("0000000 00000 00000 101 00000 0010011", arrayOf(RISCVBinMapper.MaskLabel.FUNCT7, RISCVBinMapper.MaskLabel.SHAMT, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val shamt = paramMap.get(RISCVBinMapper.MaskLabel.SHAMT)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && shamt != null && rs1 != null) {
                            rd.set(rs1.get().toBin() ushr shamt.getRawBinaryStr().toInt(2))
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            SRAI(
                "SRAI", false,
                ParamType.RD_RS1_I5,
                RISCVBinMapper.OpCode("0100000 00000 00000 101 00000 0010011", arrayOf(RISCVBinMapper.MaskLabel.FUNCT7, RISCVBinMapper.MaskLabel.SHAMT, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    if (rdAddr != null && rs1Addr != null) {
                        val rd = architecture.getRegisterContainer().getRegister(rdAddr)
                        val rs1 = architecture.getRegisterContainer().getRegister(rs1Addr)
                        val shamt = paramMap.get(RISCVBinMapper.MaskLabel.SHAMT)
                        val pc = architecture.getRegisterContainer().pc
                        if (rd != null && shamt != null && rs1 != null) {
                            rd.set(rs1.get().toBin() shr shamt.getRawBinaryStr().toInt(2))
                            pc.value.set(pc.value.get() + MutVal.Value.Hex("4"))
                        }
                    }
                }
            },
            ADD(
                "ADD", false,
                ParamType.RD_RS1_RS1,
                RISCVBinMapper.OpCode("0000000 00000 00000 000 00000 0110011", arrayOf(RISCVBinMapper.MaskLabel.FUNCT7, RISCVBinMapper.MaskLabel.RS2, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val rs2Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS2)
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
            SUB(
                "SUB", false,
                ParamType.RD_RS1_RS1,
                RISCVBinMapper.OpCode("0100000 00000 00000 000 00000 0110011", arrayOf(RISCVBinMapper.MaskLabel.FUNCT7, RISCVBinMapper.MaskLabel.RS2, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val rs2Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS2)
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
            SLL(
                "SLL", false,
                ParamType.RD_RS1_RS1,
                RISCVBinMapper.OpCode("0000000 00000 00000 001 00000 0110011", arrayOf(RISCVBinMapper.MaskLabel.FUNCT7, RISCVBinMapper.MaskLabel.RS2, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val rs2Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS2)
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
            SLT(
                "SLT", false,
                ParamType.RD_RS1_RS1,
                RISCVBinMapper.OpCode("0000000 00000 00000 010 00000 0110011", arrayOf(RISCVBinMapper.MaskLabel.FUNCT7, RISCVBinMapper.MaskLabel.RS2, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val rs2Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS2)
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
            SLTU(
                "SLTU", false,
                ParamType.RD_RS1_RS1,
                RISCVBinMapper.OpCode("0000000 00000 00000 011 00000 0110011", arrayOf(RISCVBinMapper.MaskLabel.FUNCT7, RISCVBinMapper.MaskLabel.RS2, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val rs2Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS2)
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
            XOR(
                "XOR", false,
                ParamType.RD_RS1_RS1,
                RISCVBinMapper.OpCode("0000000 00000 00000 100 00000 0110011", arrayOf(RISCVBinMapper.MaskLabel.FUNCT7, RISCVBinMapper.MaskLabel.RS2, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val rs2Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS2)
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
            SRL(
                "SRL", false,
                ParamType.RD_RS1_RS1,
                RISCVBinMapper.OpCode("0000000 00000 00000 101 00000 0110011", arrayOf(RISCVBinMapper.MaskLabel.FUNCT7, RISCVBinMapper.MaskLabel.RS2, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val rs2Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS2)
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
            SRA(
                "SRA", false,
                ParamType.RD_RS1_RS1,
                RISCVBinMapper.OpCode("0100000 00000 00000 101 00000 0110011", arrayOf(RISCVBinMapper.MaskLabel.FUNCT7, RISCVBinMapper.MaskLabel.RS2, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val rs2Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS2)
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
            OR(
                "OR", false,
                ParamType.RD_RS1_RS1,
                RISCVBinMapper.OpCode("0000000 00000 00000 110 00000 0110011", arrayOf(RISCVBinMapper.MaskLabel.FUNCT7, RISCVBinMapper.MaskLabel.RS2, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val rs2Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS2)
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
            AND(
                "AND", false,
                ParamType.RD_RS1_RS1,
                RISCVBinMapper.OpCode("0000000 00000 00000 111 00000 0110011", arrayOf(RISCVBinMapper.MaskLabel.FUNCT7, RISCVBinMapper.MaskLabel.RS2, RISCVBinMapper.MaskLabel.RS1, RISCVBinMapper.MaskLabel.FUNCT3, RISCVBinMapper.MaskLabel.RD, RISCVBinMapper.MaskLabel.OPCODE))
            ) {
                override fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                    super.execute(architecture, paramMap)
                    val rdAddr = paramMap.get(RISCVBinMapper.MaskLabel.RD)
                    val rs1Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS1)
                    val rs2Addr = paramMap.get(RISCVBinMapper.MaskLabel.RS2)
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
            Nop("NOP", true, ParamType.PS_NONE),
            Mv("MV", true, ParamType.PS_RD_RS1),
            Li("LI", true, ParamType.PS_RD_I32, memWords = 2),
            La("LA", true, ParamType.PS_RD_Albl, memWords = 2),
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
            JAL1("JAL", true, ParamType.PS_RS1_Jlbl),
            JAL2("JAL", true, ParamType.PS_Jlbl),
            Jr("JR", true, ParamType.PS_RS1),
            JALR1("JALR", true, ParamType.PS_RS1),
            Ret("RET", true, ParamType.PS_NONE),
            Call("CALL", true, ParamType.PS_Jlbl, memWords = 2),
            Tail("TAIL", true, ParamType.PS_Jlbl, memWords = 2);

            open fun execute(architecture: Architecture, paramMap: Map<RISCVBinMapper.MaskLabel, MutVal.Value.Binary>) {
                architecture.getConsole().info("executing $id ...")
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
    data class EquDefinition(val name: Compiler.Token, val constant: Compiler.Token)
    data class MacroDefinition(val name: String, val arguments: List<String>, val replacementLines: List<String>)

}