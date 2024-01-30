package emulator.archs.riscv32

import emulator.kit.Architecture
import emulator.kit.assembly.Compiler
import emulator.kit.assembly.Syntax
import emulator.kit.common.FileHandler
import emulator.kit.common.Transcript
import emulator.kit.types.Variable.Value.*
import emulator.kit.assembly.Syntax.TokenSeq.Component.*
import emulator.kit.assembly.Syntax.TokenSeq.Component.InSpecific.*
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Size.*


class RV32NewSyntax : Syntax() {

    override val applyStandardHLForRest: Boolean = false
    override fun clear() {}

    override fun check(arch: Architecture, compiler: Compiler, tokens: List<Compiler.Token>, tokenLines: List<List<Compiler.Token>>, others: List<FileHandler.File>, transcript: Transcript): SyntaxTree {
        val remainingTokens = tokens.toMutableList()

        val errors = mutableListOf<Error>()
        val warnings = mutableListOf<Warning>()

        val preElements = mutableListOf<TreeNode.ElementNode>()
        val elements = mutableListOf<TreeNode.ElementNode>()
        val sections = mutableListOf<TreeNode.SectionNode>()

        remainingTokens.resolveImports(preElements, sections, errors, warnings, others)

        remainingTokens.removeComments(preElements)
        remainingTokens.resolveEqus(preElements, errors, warnings, arch)
        remainingTokens.resolveMacros(preElements, errors, warnings, arch)

        var currentLabel: ELabel? = null

        // Resolve Compiler Tokens
        while (remainingTokens.isNotEmpty()) {
            if (remainingTokens.first() is Compiler.Token.Space || remainingTokens.first() is Compiler.Token.NewLine) {
                remainingTokens.removeFirst()
                continue
            }

            val label = remainingTokens.checkLabel(elements, errors, warnings, currentLabel)
            if (label != null) {
                if (!label.spaceSub) currentLabel = label
                continue
            }

            if (remainingTokens.checkSecDir(elements, errors, warnings)) continue

            if (remainingTokens.checkAsmInfo(elements, errors, warnings)) continue

            if (remainingTokens.checkData(elements, errors, warnings)) continue

            if (remainingTokens.checkInstr(elements, errors, warnings, currentLabel)) continue

            //errors.add(Error("Couldn't be resolved!", remainingTokens.first()))
            errors.add(Error("(${remainingTokens.first().content}) couldn't be resolved!", remainingTokens.removeFirst()))
        }

        // Link
        elements.linkLabels(sections, errors)

        elements.bundleSections(sections, errors, warnings)

        return SyntaxTree(TreeNode.RootNode(errors, warnings, TreeNode.ContainerNode("pre", *preElements.toTypedArray()), CSections(*sections.toTypedArray())))
    }

    /**
     * Removes all comments from the compiler tokens and adds them to the preElements
     */
    private fun MutableList<Compiler.Token>.removeComments(preElements: MutableList<TreeNode.ElementNode>): MutableList<Compiler.Token> {
        while (true) {
            val commentStart = this.firstOrNull { it.content == "#" } ?: break
            val startIndex = this.indexOf(commentStart)
            val commentEnd =
                this.firstOrNull { it is Compiler.Token.NewLine && this.indexOf(commentStart) < this.indexOf(it) }
            val endIndex = commentEnd?.let { this.indexOf(it) } ?: this.size

            val commentTokens = this.subList(startIndex, endIndex).toList()
            this.subList(startIndex, endIndex).clear()
            preElements.add(PREComment(*commentTokens.toTypedArray()))
        }
        return this
    }

    /**
     * Resolve EQU definitions
     */
    private fun MutableList<Compiler.Token>.resolveEqus(preElements: MutableList<TreeNode.ElementNode>, errors: MutableList<Error>, warning: MutableList<Warning>, arch: Architecture): MutableList<Compiler.Token> {
        val defs = mutableSetOf<PreEquDef>()
        // 1. Find definitions
        val tokensToCheckForDef = this.toMutableList()
        while (tokensToCheckForDef.isNotEmpty()) {
            val equDirTokenResult = DirType.EQU.tokenSeq.matchStart(*tokensToCheckForDef.toTypedArray())
            if (!equDirTokenResult.matches) {
                tokensToCheckForDef.removeFirst()
                continue
            }

            tokensToCheckForDef.removeAll(equDirTokenResult.sequenceMap.map { it.token })

            val equDefTokenResult = Seqs.SeqAfterEquDir.matchStart(*tokensToCheckForDef.toTypedArray())
            if (!equDefTokenResult.matches || equDefTokenResult.sequenceMap.size != 3) {
                errors.add(Error("Invalid equ syntax! (.equ [name], [constant])", *equDirTokenResult.sequenceMap.map { it.token }.toTypedArray(), *equDefTokenResult.sequenceMap.map { it.token }.toTypedArray()))
                tokensToCheckForDef.removeAll(equDefTokenResult.sequenceMap.map { it.token })
                this.removeAll(equDirTokenResult.sequenceMap.map { it.token })
                this.removeAll(equDefTokenResult.sequenceMap.map { it.token })
                continue
            }

            tokensToCheckForDef.removeAll(equDefTokenResult.sequenceMap.map { it.token })

            val equName = equDefTokenResult.sequenceMap.map { it.token }[0] as Compiler.Token.Word
            val comma = equDefTokenResult.sequenceMap.map { it.token }[1] as Compiler.Token.Symbol
            val constant = equDefTokenResult.sequenceMap.map { it.token }[2] as Compiler.Token.Constant

            val equDef = PreEquDef(equDirTokenResult.sequenceMap.map { it.token }, equName, comma, constant)
            defs.add(equDef)
            this.removeAll(equDef.tokens.toSet())
        }

        preElements.addAll(defs)

        // 2. Resolve definitions
        for (equDef in defs) {
            var foundToken: Compiler.Token? = this.firstOrNull { it.content == equDef.equname.content }
            while (foundToken != null) {
                val pseudoConst = arch.getCompiler().pseudoTokenize(equDef.constant.content).firstOrNull()
                if (pseudoConst == null) {
                    errors.add(Error("Couldn't create pseudo Token!", foundToken))
                    this.remove(foundToken)
                    continue
                }
                val index = this.indexOf(foundToken)
                preElements.add(PreEquRep(foundToken))
                this.remove(foundToken)
                this.add(index, pseudoConst)
                foundToken = this.firstOrNull { it.content == equDef.equname.content }
            }
        }

        return this
    }

    /**
     * Resolves all macro definitions (removes definition and replaces inserts)
     */
    private fun MutableList<Compiler.Token>.resolveMacros(preElements: MutableList<TreeNode.ElementNode>, errors: MutableList<Error>, warnings: MutableList<Warning>, arch: Architecture): MutableList<Compiler.Token> {
        val defs = mutableSetOf<PreMacroDef>()
        // 1. Find definitions
        val tokenBuffer = this.toMutableList()
        while (tokenBuffer.isNotEmpty()) {
            // 1.1 Search macro start directive (.macro)
            val macroStartResult = DirType.MACRO.tokenSeq.matchStart(*tokenBuffer.toTypedArray())
            if (!macroStartResult.matches) {
                tokenBuffer.removeFirst()
                continue
            }
            val macroStartDir = macroStartResult.sequenceMap.map { it.token }

            tokenBuffer.removeAll(macroStartResult.sequenceMap.map { it.token })

            // 1.2 Search macro name sequence (name)
            if (tokenBuffer.first() is Compiler.Token.Space) tokenBuffer.removeFirst() // Remove leading spaces
            if (tokenBuffer.first() !is Compiler.Token.Word || tokenBuffer.first() is Compiler.Token.Word.NumDotsUs) {
                errors.add(Error("Invalid macro syntax! Macro name expected!", *macroStartResult.sequenceMap.map { it.token }.toTypedArray()))
                continue
            }
            val macroName = tokenBuffer.first() as Compiler.Token.Word
            tokenBuffer.remove(macroName)


            // 1.3 Search parameters
            val attributes = mutableListOf<Compiler.Token.Word>()
            val commas = mutableListOf<Compiler.Token>()
            while (true) {
                if (tokenBuffer.first() is Compiler.Token.Space) tokenBuffer.removeFirst() // Remove leading spaces

                when (attributes.size) {
                    commas.size -> { // Expect Attribute
                        if (tokenBuffer.first() !is Compiler.Token.Word || tokenBuffer.first() is Compiler.Token.Word.NumDotsUs) break
                        attributes.add(tokenBuffer.first() as Compiler.Token.Word)
                        tokenBuffer.removeFirst()
                        continue
                    }

                    commas.size + 1 -> { // Expect Colon
                        if (tokenBuffer.first().content != ",") break
                        commas.add(tokenBuffer.first())
                        tokenBuffer.removeFirst()
                        continue
                    }
                }
                break
            }

            if (commas.isNotEmpty()) {
                if (attributes.size == commas.size) warnings.add(Warning("Unnecessary trailing comma!", commas.last()))
            }

            // 1.4 NewLine (Random Sequence)
            if (tokenBuffer.first() !is Compiler.Token.NewLine) {
                val errorMacroTokens = macroStartResult.sequenceMap.map { it.token }.toTypedArray() + macroName + attributes + commas
                errors.add(Error("Invalid macro syntax! New Line expected!", *errorMacroTokens))
                this.removeAll(errorMacroTokens.toSet())
                continue
            }
            tokenBuffer.removeFirst()

            // 1.5 Add All to Macro Content until a .endm token is found
            val macroContent = mutableListOf<Compiler.Token>()
            val macroEndDir = mutableListOf<Compiler.Token>()
            while (tokenBuffer.isNotEmpty()) {
                val endmResult = DirType.ENDM.tokenSeq.matchStart(*tokenBuffer.toTypedArray())
                if (!endmResult.matches) {
                    macroContent.add(tokenBuffer.first())
                    tokenBuffer.removeFirst()
                    continue
                }
                macroEndDir.addAll(endmResult.sequenceMap.map { it.token })
                tokenBuffer.removeAll(macroEndDir)
                break
            }

            if (macroEndDir.isEmpty()) {
                val errorMacroTokens = arrayOf(*macroStartResult.sequenceMap.map { it.token }.toTypedArray(), macroName, *attributes.toTypedArray(), *commas.toTypedArray())
                errors.add(Error("Invalid macro syntax! End macro directive (.${DirType.ENDM.dirname}) missing!", *errorMacroTokens))
                this.removeAll(errorMacroTokens.toSet())
                break
            }

            val macroDef = PreMacroDef((macroStartDir + macroEndDir), macroName, attributes, commas, macroContent)
            defs.add(macroDef)
            this.removeAll(macroDef.tokens.toSet())
        }
        preElements.addAll(defs)

        // 2. Replace Macro Defs
        for (macro in defs) {
            while (true) {
                val macroNameRef = this.firstOrNull { it is Compiler.Token.Word && it.content == macro.macroname.content } ?: break
                val macroNewLine = this.firstOrNull { it is Compiler.Token.NewLine && this.indexOf(macroNameRef) < this.indexOf(it) } ?: this.last()
                val macroRefTokens = this.subList(this.indexOf(macroNameRef), this.indexOf(macroNewLine))
                val indexOfName = this.indexOf(macroNameRef)
                macroRefTokens.remove(macroNameRef)

                val params = mutableListOf<Compiler.Token>()
                val commas = mutableListOf<Compiler.Token>()

                var errorAtParamDetection = false
                for (token in macroRefTokens) {
                    if (token is Compiler.Token.NewLine) {
                        break
                    }
                    if (token is Compiler.Token.Space) {
                        continue
                    }
                    if (params.size == commas.size) {
                        // Expect Param
                        params.add(token)
                        continue
                    }

                    if (params.size - 1 == commas.size) {
                        // Expect Comma
                        if (token.content != ",") {
                            errors.add(Error("Macro Replacement expected comma!", *macroRefTokens.toTypedArray(), macroNameRef))
                            errorAtParamDetection = true
                            break
                        }
                        commas.add(token)
                        continue
                    }
                }

                if (errorAtParamDetection) continue

                if (params.size > 0 && params.size == commas.size) {
                    warnings.add(Warning("Unnecessary trailing comma!", commas.last()))
                }

                if (params.size != macro.attributes.size) {
                    errors.add(Error("Expected ${macro.attributes.size} parameters!", *macroRefTokens.toTypedArray(), macroNameRef))
                    this.remove(macroNameRef)
                    this.removeAll(macroRefTokens)
                    continue
                }

                val preMacroRep = PreMacroRep(macroNameRef, params, commas)
                this.removeAll(macroRefTokens.filter { it !is Compiler.Token.NewLine })
                this.addAll(indexOfName, macro.getMacroReplacement(params, arch) ?: listOf())
                preElements.add(preMacroRep)
            }
        }

        return this
    }

    private fun MutableList<Compiler.Token>.resolveImports(preElements: MutableList<TreeNode.ElementNode>, sections: MutableList<TreeNode.SectionNode>, errors: MutableList<Error>, warnings: MutableList<Warning>, others: List<FileHandler.File>): MutableList<Compiler.Token> {
        val tokenBuffer = this.toMutableList()
        while (tokenBuffer.isNotEmpty()) {
            val result = Seqs.SeqImport.matchStart(*tokenBuffer.toTypedArray())

            if (!result.matches) {
                tokenBuffer.removeFirst()
                continue
            }


            val tokens = result.sequenceMap.map { it.token }
            val symbol = tokens[0]
            val word = tokens[1]
            val space = tokens[2]
            val string = tokens[3] as Compiler.Token.Constant.String

            val file = others.firstOrNull { it.getName() == string.rawString }

            if (file == null) {
                errors.add(Error("File (${string.rawString}) not found!", *tokens.toTypedArray()))
                tokenBuffer.removeAll(tokens)
                this.removeAll(tokens)
                continue
            }

            val fileTree = file.getLinkedTree()
            if (fileTree?.rootNode == null) {
                errors.add(Error("File (${string.rawString}) not build!", *tokens.toTypedArray()))
                tokenBuffer.removeAll(tokens)
                this.removeAll(tokens)
                continue
            }
            val root = fileTree.rootNode

            if (root.allErrors.isNotEmpty()) {
                errors.add(Error("File (${string.rawString}) has errors which first need to be fixed!", *tokens.toTypedArray()))
                tokenBuffer.removeAll(tokens)
                this.removeAll(tokens)
                continue
            }

            root.containers.forEach {
                if (it is CSections) {
                    sections.addAll(it.sections)
                }
            }

            preElements.add(PreImport(symbol, word, space, string))
            tokenBuffer.removeAll(tokens)
            this.removeAll(tokens)
        }


        return this
    }

    private fun MutableList<Compiler.Token>.checkData(elements: MutableList<TreeNode.ElementNode>, errors: MutableList<Error>, warnings: MutableList<Warning>): Boolean {
        for (dirType in DirType.entries.filter { it.dirMajType == DirMajType.DE_ALIGNED || it.dirMajType == DirMajType.DE_UNALIGNED }) {
            val result = dirType.tokenSeq.matchStart(*this.toTypedArray())
            if (!result.matches) continue

            val dirTokens = result.sequenceMap.map { it.token }
            this.removeAll(dirTokens)

            val constants = mutableListOf<Compiler.Token.Constant>()
            val commas = mutableListOf<Compiler.Token>()

            while (this.isNotEmpty()) {
                if (this.first() is Compiler.Token.Space || this.first() is Compiler.Token.NewLine) {
                    this.removeFirst()
                    continue
                }
                if (constants.size == commas.size) {
                    // Expect constant
                    if (this.first() !is Compiler.Token.Constant) break
                    val constant = this.first() as Compiler.Token.Constant
                    if (!constant.getValue(dirType.deSize).checkResult.valid) break
                    constants.add(constant)
                } else {
                    // Expect comma
                    if (this.first().content != ",") break
                    commas.add(this.first())
                }
                this.removeFirst()
            }

            if (constants.isNotEmpty()) {
                elements.add(EInitData(dirType, dirTokens, constants, commas))
            } else {
                elements.add(EUnInitData(dirType, dirTokens))
            }
            this.removeAll(constants)
            this.removeAll(commas)

            return true
        }

        return false
    }

    private fun MutableList<Compiler.Token>.checkLabel(elements: MutableList<TreeNode.ElementNode>, errors: MutableList<Error>, warnings: MutableList<Warning>, currentLabel: ELabel?): ELabel? {

        val labelMatch = Seqs.SeqLabel.matchStart(*this.toTypedArray())
        if (labelMatch.matches) {
            val tokens = labelMatch.sequenceMap.map { it.token }
            if (currentLabel == null && tokens.first().content.startsWith(".")) {
                this.removeAll(tokens)
                errors.add(Error("Can't initiate a sub label without a parent label!", *tokens.toTypedArray()))
                return null
            }

            val label = ELabel(currentLabel, tokens.first(), tokens[1])
            elements.add(label)
            this.removeAll(label.tokens.toSet())
            return label
        }

        return null
    }

    private fun MutableList<Compiler.Token>.checkInstr(elements: MutableList<TreeNode.ElementNode>, errors: MutableList<Error>, warnings: MutableList<Warning>, currentLabel: ELabel?): Boolean {
        for (paramType in ParamType.entries) {
            val result = paramType.tokenSeq.matchStart(*this.toTypedArray())
            if (!result.matches) continue
            val allTokens = result.sequenceMap.map { it.token }
            val nameToken = allTokens.firstOrNull() ?: continue
            val filteredTokens = allTokens.filterNot { it is Compiler.Token.Space || it == nameToken }

            val instrType = InstrType.entries.firstOrNull { it.paramType == paramType && it.id.uppercase() == nameToken.content.uppercase() } ?: continue
            val eInstr = EInstr(instrType, paramType, nameToken, filteredTokens, allTokens.filterIsInstance<Compiler.Token.Space>(), currentLabel)

            elements.add(eInstr)
            this.remove(nameToken)
            this.removeAll(allTokens.filterIsInstance<Compiler.Token.Space>())
            this.removeAll(filteredTokens)
            return true
        }

        return false
    }

    private fun MutableList<Compiler.Token>.checkSecDir(elements: MutableList<TreeNode.ElementNode>, errors: MutableList<Error>, warnings: MutableList<Warning>): Boolean {
        for (dir in DirType.entries.filter { it.dirMajType == DirMajType.SECTIONSTART }) {
            val result = dir.tokenSeq.matchStart(*this.toTypedArray())
            if (result.matches) {
                val eSec = ESecStart(dir, result.sequenceMap.map { it.token })
                this.removeAll(eSec.tokens.toSet())
                elements.add(eSec)
                return true
            }
        }
        return false
    }

    private fun MutableList<Compiler.Token>.checkAsmInfo(elements: MutableList<TreeNode.ElementNode>, errors: MutableList<Error>, warnings: MutableList<Warning>): Boolean {
        val globalResult = DirType.GLOBAL.tokenSeq.matchStart(*this.toTypedArray())
        val globlResult = DirType.GLOBL.tokenSeq.matchStart(*this.toTypedArray())
        if (globlResult.matches || globalResult.matches) {
            val tokens = if (globalResult.matches) globalResult.sequenceMap.map { it.token } else globlResult.sequenceMap.map { it.token }
            val eGlobal = EGlobal(tokens[0], tokens[1], tokens[2])
            this.removeAll(eGlobal.tokens.toSet())
            elements.add(eGlobal)
            return true
        }

        val setPcResult = Seqs.SeqSetPC.matchStart(*this.toTypedArray())
        if (setPcResult.matches) {
            val tokens = setPcResult.sequenceMap.map { it.token }
            val symbols = tokens.filter { it !is Compiler.Token.Constant }
            val constant = tokens.first { it is Compiler.Token.Constant } as Compiler.Token.Constant
            this.removeAll(tokens)
            val eSetPC = ESetPC(symbols, constant)
            elements.add(eSetPC)
            return true
        }

        return false
    }

    private fun MutableList<TreeNode.ElementNode>.linkLabels(sections: MutableList<TreeNode.SectionNode>, errors: MutableList<Error>) {
        val allElements = sections.flatMap { it.collNodes.toList().map { element -> element as TreeNode.ElementNode } } + this.toList()
        val labels = allElements.filterIsInstance<ELabel>().toMutableList()
        val globals = allElements.filterIsInstance<EGlobal>()

        // Check duplicates
        val seenNames = mutableSetOf<String>()
        for (label in labels) {
            if (!seenNames.add(label.nameString)) {
                errors.add(Error("Label (${label.nameString}) already in use!", label))
                this.remove(label)
                labels.remove(label)
            }
        }

        if (globals.isNotEmpty()) {
            val valid = globals.first().link(labels)
            if (!valid) {
                this.remove(globals.first())
            }
            if (globals.size > 1) {
                val rest = globals - globals.first()
                errors.add(Error("Mutliple definitions of a global start not possible!", *rest.toTypedArray()))
                this.removeAll(rest)
            }
        }

        val instrs = this.filterIsInstance<EInstr>()
        for (instr in instrs) {
            val valid = instr.link(labels, errors)
            if (!valid) {
                this.remove(instr)
            }
        }
    }

    private fun MutableList<TreeNode.ElementNode>.bundleSections(sections: MutableList<TreeNode.SectionNode>, errors: MutableList<Error>, warnings: MutableList<Warning>): MutableList<TreeNode.ElementNode> {
        var currSecStart: ESecStart? = null
        val content = mutableListOf<TreeNode.ElementNode>()
        while (this.isNotEmpty()) {
            if (this.first() is ESecStart) {
                when (currSecStart?.dirType) {
                    null, DirType.TEXT -> {
                        sections.add(SText(currSecStart, *content.toTypedArray()))
                    }

                    DirType.DATA -> {
                        sections.add(SData(currSecStart, *content.toTypedArray()))
                    }

                    DirType.RODATA -> {
                        sections.add(SRoData(currSecStart, *content.toTypedArray()))
                    }

                    DirType.BSS -> {
                        sections.add(SBss(currSecStart, *content.toTypedArray()))
                    }

                    else -> {}
                }
                content.clear()
                currSecStart = this.first() as ESecStart
                this.removeFirst()
                continue
            }

            when (currSecStart?.dirType) {
                null, DirType.TEXT -> {
                    if (this.first() !is EInstr && this.first() !is ELabel && this.first() !is EGlobal && this.first() !is ESetPC) {
                        val element = this.first()
                        errors.add(Error("Wrong section for ${element::class.simpleName}!", this.removeFirst()))
                    } else {
                        content.add(this.removeFirst())
                    }
                }

                DirType.DATA -> {
                    if (this.first() !is ELabel && this.first() !is EInitData && this.first() !is EGlobal && this.first() !is ESetPC) {
                        val element = this.first()
                        errors.add(Error("Wrong section for ${element::class.simpleName}!", this.removeFirst()))
                    } else {
                        content.add(this.removeFirst())
                    }
                }

                DirType.RODATA -> {
                    if (this.first() !is ELabel && this.first() !is EInitData && this.first() !is EGlobal && this.first() !is ESetPC) {
                        val element = this.first()
                        errors.add(Error("Wrong section for ${element::class.simpleName}!", this.removeFirst()))
                    } else {
                        content.add(this.removeFirst())
                    }
                }

                DirType.BSS -> {
                    if (this.first() !is ELabel && this.first() !is EUnInitData && this.first() !is EGlobal && this.first() !is ESetPC) {
                        val element = this.first()
                        errors.add(Error("Wrong section for ${element::class.simpleName}!", this.removeFirst()))
                    } else {
                        content.add(this.removeFirst())
                    }
                }

                else -> {}
            }
        }
        when (currSecStart?.dirType) {
            null, DirType.TEXT -> {
                sections.add(SText(currSecStart, *content.toTypedArray()))
            }

            DirType.DATA -> {
                sections.add(SData(currSecStart, *content.toTypedArray()))
            }

            DirType.RODATA -> {
                sections.add(SRoData(currSecStart, *content.toTypedArray()))
            }

            DirType.BSS -> {
                sections.add(SBss(currSecStart, *content.toTypedArray()))
            }

            else -> {}
        }

        return this
    }

    data object Seqs {
        val SeqAfterEquDir = TokenSeq(WordNoDots, Specific(","), Constant, ignoreSpaces = true)
        val SeqMacroAttrInsert = TokenSeq(Specific("""\"""), WordNoDots)
        val SeqLabel = TokenSeq(Word, Specific(":"))
        val SeqImport = TokenSeq(Specific("#"), Specific("import"), Space, StringConst)
        val SeqSetPC = TokenSeq(Specific("*"), Specific("="), SpecConst(RV32.XLEN))
    }

    /**
     * Syntax Types Holding the Instruction and Directive Definitions
     */

    enum class ParamType(val pseudo: Boolean, val exampleString: String, val tokenSeq: TokenSeq) {
        // NORMAL INSTRUCTIONS
        RD_I20(false, "rd, imm20", TokenSeq(WordNoDotsAndUS, Space, Register(RV32.standardRegFile), Specific(","), SpecConst(Bit20()), NewLine, ignoreSpaces = true)) {
            override fun getTSParamString(arch: Architecture, paramMap: MutableMap<RV32BinMapper.MaskLabel, Bin>, labelName: String): String {
                val rd = paramMap[RV32BinMapper.MaskLabel.RD]
                return if (rd != null) {
                    paramMap.remove(RV32BinMapper.MaskLabel.RD)
                    val immString = labelName.ifEmpty { "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString(" ") { it.toHex().getRawHexStr() }}" }
                    "${arch.getRegByAddr(rd)?.aliases?.first()},\t$immString"
                } else {
                    "param missing"
                }
            }
        }, // rd, imm
        RD_OFF12(false, "rd, imm12(rs)", TokenSeq(WordNoDotsAndUS, Space, Register(RV32.standardRegFile), Specific(","), SpecConst(Bit12()), Specific("("), Register(RV32.standardRegFile), Specific(")"), NewLine, ignoreSpaces = true)) {
            override fun getTSParamString(arch: Architecture, paramMap: MutableMap<RV32BinMapper.MaskLabel, Bin>, labelName: String): String {
                val rd = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1 = paramMap[RV32BinMapper.MaskLabel.RS1]
                return if (rd != null && rs1 != null) {
                    paramMap.remove(RV32BinMapper.MaskLabel.RD)
                    paramMap.remove(RV32BinMapper.MaskLabel.RS1)
                    val immString = labelName.ifEmpty { "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString(" ") { it.toHex().getRawHexStr() }}" }
                    "${arch.getRegByAddr(rd)?.aliases?.first()},\t$immString(${arch.getRegByAddr(rs1)?.aliases?.first()})"
                } else {
                    "param missing"
                }
            }
        }, // rd, imm12(rs)
        RS2_OFF12(false, "rs2, imm12(rs1)", TokenSeq(WordNoDotsAndUS, Space, Register(RV32.standardRegFile), Specific(","), SpecConst(Bit12()), Specific("("), Register(RV32.standardRegFile), Specific(")"), NewLine, ignoreSpaces = true)) {
            override fun getTSParamString(arch: Architecture, paramMap: MutableMap<RV32BinMapper.MaskLabel, Bin>, labelName: String): String {
                val rs2 = paramMap[RV32BinMapper.MaskLabel.RS2]
                val rs1 = paramMap[RV32BinMapper.MaskLabel.RS1]
                return if (rs2 != null && rs1 != null) {
                    paramMap.remove(RV32BinMapper.MaskLabel.RS2)
                    paramMap.remove(RV32BinMapper.MaskLabel.RS1)
                    val immString = labelName.ifEmpty { "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString(" ") { it.toHex().getRawHexStr() }}" }
                    "${arch.getRegByAddr(rs2)?.aliases?.first()},\t$immString(${arch.getRegByAddr(rs1)?.aliases?.first()})"
                } else {
                    "param missing"
                }
            }
        }, // rs2, imm5(rs1)
        RD_RS1_RS2(false, "rd, rs1, rs2", TokenSeq(WordNoDotsAndUS, Space, Register(RV32.standardRegFile), Specific(","), Register(RV32.standardRegFile), Specific(","), Register(RV32.standardRegFile), NewLine, ignoreSpaces = true)) {
            override fun getTSParamString(arch: Architecture, paramMap: MutableMap<RV32BinMapper.MaskLabel, Bin>, labelName: String): String {
                val rd = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1 = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2 = paramMap[RV32BinMapper.MaskLabel.RS2]
                return if (rd != null && rs2 != null && rs1 != null) {
                    paramMap.remove(RV32BinMapper.MaskLabel.RD)
                    paramMap.remove(RV32BinMapper.MaskLabel.RS2)
                    paramMap.remove(RV32BinMapper.MaskLabel.RS1)
                    "${arch.getRegByAddr(rd)?.aliases?.first()},\t${arch.getRegByAddr(rs1)?.aliases?.first()},\t${arch.getRegByAddr(rs2)?.aliases?.first()}"
                } else {
                    "param missing"
                }
            }
        }, // rd, rs1, rs2
        RD_RS1_I12(false, "rd, rs1, imm12", TokenSeq(WordNoDotsAndUS, Space, Register(RV32.standardRegFile), Specific(","), Register(RV32.standardRegFile), Specific(","), SpecConst(Bit12()), NewLine, ignoreSpaces = true)) {
            override fun getTSParamString(arch: Architecture, paramMap: MutableMap<RV32BinMapper.MaskLabel, Bin>, labelName: String): String {
                val rd = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1 = paramMap[RV32BinMapper.MaskLabel.RS1]
                return if (rd != null && rs1 != null) {
                    paramMap.remove(RV32BinMapper.MaskLabel.RD)
                    paramMap.remove(RV32BinMapper.MaskLabel.RS1)
                    val immString = labelName.ifEmpty { "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString(" ") { it.toHex().getRawHexStr() }}" }
                    "${arch.getRegByAddr(rd)?.aliases?.first()},\t${arch.getRegByAddr(rs1)?.aliases?.first()},\t$immString"
                } else {
                    "param missing"
                }
            }
        }, // rd, rs, imm
        RD_RS1_I5(false, "rd, rs1, shamt5", TokenSeq(WordNoDotsAndUS, Space, Register(RV32.standardRegFile), Specific(","), Register(RV32.standardRegFile), Specific(","), SpecConst(Bit5()), NewLine, ignoreSpaces = true)) {
            override fun getTSParamString(arch: Architecture, paramMap: MutableMap<RV32BinMapper.MaskLabel, Bin>, labelName: String): String {
                val rd = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1 = paramMap[RV32BinMapper.MaskLabel.RS1]
                return if (rd != null && rs1 != null) {
                    paramMap.remove(RV32BinMapper.MaskLabel.RD)
                    paramMap.remove(RV32BinMapper.MaskLabel.RS1)
                    val immString = labelName.ifEmpty { "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString(" ") { it.toHex().getRawHexStr() }}" }
                    "${arch.getRegByAddr(rd)?.aliases?.first()},\t${arch.getRegByAddr(rs1)?.aliases?.first()},\t$immString"
                } else {
                    "param missing"
                }
            }
        }, // rd, rs, shamt
        RS1_RS2_I12(false, "rs1, rs2, imm12", TokenSeq(WordNoDotsAndUS, Space, Register(RV32.standardRegFile), Specific(","), Register(RV32.standardRegFile), Specific(","), SpecConst(Bit12()), NewLine, ignoreSpaces = true)) {
            override fun getTSParamString(arch: Architecture, paramMap: MutableMap<RV32BinMapper.MaskLabel, Bin>, labelName: String): String {
                val rs2 = paramMap[RV32BinMapper.MaskLabel.RS2]
                val rs1 = paramMap[RV32BinMapper.MaskLabel.RS1]
                return if (rs2 != null && rs1 != null) {
                    paramMap.remove(RV32BinMapper.MaskLabel.RS2)
                    paramMap.remove(RV32BinMapper.MaskLabel.RS1)
                    val immString = labelName.ifEmpty { "0x${paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString(" ") { it.toHex().getRawHexStr() }}" }
                    "${arch.getRegByAddr(rs1)?.aliases?.first()},\t${arch.getRegByAddr(rs2)?.aliases?.first()},\t$immString"
                } else {
                    "param missing"
                }
            }
        }, // rs1, rs2, imm
        CSR_RD_OFF12_RS1(false, "rd, csr12, rs1", TokenSeq(WordNoDotsAndUS, Space, Register(RV32.standardRegFile), Specific(","), RegOrSpecConst(Bit12(), notInRegFile = RV32.standardRegFile), Specific(","), Register(RV32.standardRegFile), NewLine, ignoreSpaces = true)) {
            override fun getTSParamString(arch: Architecture, paramMap: MutableMap<RV32BinMapper.MaskLabel, Bin>, labelName: String): String {
                val rd = paramMap[RV32BinMapper.MaskLabel.RD]
                val csr = paramMap[RV32BinMapper.MaskLabel.CSR]
                val rs1 = paramMap[RV32BinMapper.MaskLabel.RS1]
                return if (rd != null && csr != null && rs1 != null) {
                    paramMap.remove(RV32BinMapper.MaskLabel.RD)
                    paramMap.remove(RV32BinMapper.MaskLabel.CSR)
                    paramMap.remove(RV32BinMapper.MaskLabel.RS1)
                    "${arch.getRegByAddr(rd)?.aliases?.first()},\t${arch.getRegByAddr(csr)?.aliases?.first()},\t${arch.getRegByAddr(rs1)?.aliases?.first()}"
                } else {
                    "param missing"
                }
            }
        },
        CSR_RD_OFF12_UIMM5(false, "rd, offset, uimm5", TokenSeq(WordNoDotsAndUS, Space, Register(RV32.standardRegFile), Specific(","), RegOrSpecConst(Bit12(), notInRegFile = RV32.standardRegFile), Specific(","), SpecConst(Bit5()), NewLine, ignoreSpaces = true)) {
            override fun getTSParamString(arch: Architecture, paramMap: MutableMap<RV32BinMapper.MaskLabel, Bin>, labelName: String): String {
                val rd = paramMap[RV32BinMapper.MaskLabel.RD]
                val csr = paramMap[RV32BinMapper.MaskLabel.CSR]
                return if (rd != null && csr != null) {
                    paramMap.remove(RV32BinMapper.MaskLabel.RD)
                    paramMap.remove(RV32BinMapper.MaskLabel.CSR)
                    val immString = labelName.ifEmpty { paramMap.map { it.value }.sortedBy { it.size.bitWidth }.reversed().joinToString(" ") { it.toBin().toString() } }
                    "${arch.getRegByAddr(rd)?.aliases?.first()},\t${arch.getRegByAddr(csr)?.aliases?.first()},\t$immString"
                } else {
                    "param missing"
                }
            }
        },

        // PSEUDO INSTRUCTIONS
        PS_RS1_RS2_JLBL(true, "rs1, rs2, jlabel", TokenSeq(WordNoDotsAndUS, Space, Register(RV32.standardRegFile), Specific(","), Register(RV32.standardRegFile), Specific(","), WordNoDots, NewLine, ignoreSpaces = true)),
        PS_RD_I32(true, "rd, imm32", TokenSeq(WordNoDotsAndUS, Space, Register(RV32.standardRegFile), Specific(","), SpecConst(Bit32()), NewLine, ignoreSpaces = true)), // rd, imm
        PS_RS1_JLBL(true, "rs, jlabel", TokenSeq(WordNoDotsAndUS, Space, Register(RV32.standardRegFile), Specific(","), WordNoDots, NewLine, ignoreSpaces = true)), // rs, label
        PS_RD_ALBL(true, "rd, alabel", TokenSeq(WordNoDotsAndUS, Space, Register(RV32.standardRegFile), Specific(","), WordNoDots, NewLine, ignoreSpaces = true)), // rd, label
        PS_JLBL(true, "jlabel", TokenSeq(WordNoDotsAndUS, Space, WordNoDots, NewLine, ignoreSpaces = true)),  // label
        PS_RD_RS1(true, "rd, rs", TokenSeq(WordNoDotsAndUS, Space, Register(RV32.standardRegFile), Specific(","), Register(RV32.standardRegFile), NewLine, ignoreSpaces = true)), // rd, rs
        PS_RS1(true, "rs1", TokenSeq(WordNoDotsAndUS, Space, Register(RV32.standardRegFile), NewLine, ignoreSpaces = true)),
        PS_CSR_RS1(true, "csr, rs1", TokenSeq(WordNoDotsAndUS, Space, RegOrSpecConst(Bit12(), notInRegFile = RV32.standardRegFile), Specific(","), Register(RV32.standardRegFile), NewLine, ignoreSpaces = true)),
        PS_RD_CSR(true, "rd, csr", TokenSeq(WordNoDotsAndUS, Space, Register(RV32.standardRegFile), Specific(","), RegOrSpecConst(Bit12(), notInRegFile = RV32.standardRegFile), NewLine, ignoreSpaces = true)),

        // NONE PARAM INSTR
        NONE(false, "none", TokenSeq(WordNoDotsAndUS, NewLine, ignoreSpaces = true)),
        PS_NONE(true, "none", TokenSeq(WordNoDotsAndUS, NewLine, ignoreSpaces = true));

        open fun getTSParamString(arch: Architecture, paramMap: MutableMap<RV32BinMapper.MaskLabel, Bin>, labelName: String): String {
            return "pseudo param type"
        }
    }

    enum class InstrType(val id: String, val pseudo: Boolean, val paramType: ParamType, val opCode: RV32BinMapper.OpCode? = null, val memWords: Int = 1, val relative: InstrType? = null, val needFeatures: List<Int> = emptyList()) {
        LUI("LUI", false, ParamType.RD_I20, RV32BinMapper.OpCode("00000000000000000000 00000 0110111", arrayOf(RV32BinMapper.MaskLabel.IMM20, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap) // only for console information
                // get relevant parameters from binary map
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val imm20 = paramMap[RV32BinMapper.MaskLabel.IMM20]
                if (rdAddr == null || imm20 == null) return

                // get relevant registers
                val rd = arch.getRegByAddr(rdAddr)
                val pc = arch.getRegContainer().pc
                if (rd == null) return

                // calculate
                val shiftedIMM = imm20.getResized(RV32.XLEN) shl 12 // from imm20 to imm32
                // change states
                rd.set(shiftedIMM)    // set register to imm32 value
                pc.set(pc.get() + Hex("4"))
            }
        },
        AUIPC("AUIPC", false, ParamType.RD_I20, RV32BinMapper.OpCode("00000000000000000000 00000 0010111", arrayOf(RV32BinMapper.MaskLabel.IMM20, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                if (rdAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val imm20 = paramMap[RV32BinMapper.MaskLabel.IMM20]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm20 != null) {
                        val shiftedIMM = imm20.getUResized(RV32.XLEN) shl 12
                        val sum = pc.get() + shiftedIMM
                        rd.set(sum)
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        JAL("JAL", false, ParamType.RD_I20, RV32BinMapper.OpCode("00000000000000000000 00000 1101111", arrayOf(RV32BinMapper.MaskLabel.IMM20, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                if (rdAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val imm20 = paramMap[RV32BinMapper.MaskLabel.IMM20]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm20 != null) {
                        val imm20str = imm20.getRawBinStr()

                        /**
                         *      RV32IDOC Index   20 19 18 17 16 15 14 13 12 11 10  9  8  7  6  5  4  3  2  1
                         *        String Index    0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19
                         *        Location       20 [      10 : 1               ] 11 [ 19 : 12             ]
                         */

                        val shiftedImm = Bin(imm20str[0].toString() + imm20str.substring(12) + imm20str[11] + imm20str.substring(1, 11), Bit20()).getResized(RV32.XLEN) shl 1

                        rd.set(pc.get() + Hex("4"))
                        pc.set(pc.get() + shiftedImm)
                    }
                }
            }
        },
        JALR("JALR", false, ParamType.RD_RS1_I12, RV32BinMapper.OpCode("000000000000 00000 000 00000 1100111", arrayOf(RV32BinMapper.MaskLabel.IMM12, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[RV32BinMapper.MaskLabel.IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val jumpAddr = rs1.get() + imm12.getResized(RV32.XLEN)
                        rd.set(pc.get() + Hex("4"))
                        pc.set(jumpAddr)
                    }
                }
            }
        },
        ECALL("ECALL", false, ParamType.NONE, RV32BinMapper.OpCode("000000000000 00000 000 00000 1110011", arrayOf(RV32BinMapper.MaskLabel.NONE, RV32BinMapper.MaskLabel.NONE, RV32BinMapper.MaskLabel.NONE, RV32BinMapper.MaskLabel.NONE, RV32BinMapper.MaskLabel.OPCODE))),
        EBREAK("EBREAK", false, ParamType.NONE, RV32BinMapper.OpCode("000000000001 00000 000 00000 1110011", arrayOf(RV32BinMapper.MaskLabel.NONE, RV32BinMapper.MaskLabel.NONE, RV32BinMapper.MaskLabel.NONE, RV32BinMapper.MaskLabel.NONE, RV32BinMapper.MaskLabel.OPCODE))),
        BEQ(
            "BEQ", false, ParamType.RS1_RS2_I12,
            RV32BinMapper.OpCode("0000000 00000 00000 000 00000 1100011", arrayOf(RV32BinMapper.MaskLabel.IMM7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.IMM5, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[RV32BinMapper.MaskLabel.IMM7]
                    val imm5 = paramMap[RV32BinMapper.MaskLabel.IMM5]
                    val pc = arch.getRegContainer().pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Bit5()).getRawBinStr()
                        val imm12 = Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Bit12())

                        val offset = imm12.toBin().getResized(RV32.XLEN) shl 1
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
            "BNE", false, ParamType.RS1_RS2_I12,
            RV32BinMapper.OpCode("0000000 00000 00000 001 00000 1100011", arrayOf(RV32BinMapper.MaskLabel.IMM7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.IMM5, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[RV32BinMapper.MaskLabel.IMM7]
                    val imm5 = paramMap[RV32BinMapper.MaskLabel.IMM5]
                    val pc = arch.getRegContainer().pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Bit5()).getRawBinStr()
                        val imm12 = Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Bit12())
                        val offset = imm12.toBin().getResized(RV32.XLEN) shl 1
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
            "BLT", false, ParamType.RS1_RS2_I12,
            RV32BinMapper.OpCode("0000000 00000 00000 100 00000 1100011", arrayOf(RV32BinMapper.MaskLabel.IMM7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.IMM5, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[RV32BinMapper.MaskLabel.IMM7]
                    val imm5 = paramMap[RV32BinMapper.MaskLabel.IMM5]
                    val pc = arch.getRegContainer().pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Bit5()).getRawBinStr()
                        val imm12 = Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Bit12())
                        val offset = imm12.toBin().getResized(RV32.XLEN) shl 1
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
            "BGE", false, ParamType.RS1_RS2_I12,
            RV32BinMapper.OpCode("0000000 00000 00000 101 00000 1100011", arrayOf(RV32BinMapper.MaskLabel.IMM7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.IMM5, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[RV32BinMapper.MaskLabel.IMM7]
                    val imm5 = paramMap[RV32BinMapper.MaskLabel.IMM5]
                    val pc = arch.getRegContainer().pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Bit5()).getRawBinStr()
                        val imm12 = Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Bit12())
                        val offset = imm12.toBin().getResized(RV32.XLEN) shl 1
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
            "BLTU", false, ParamType.RS1_RS2_I12,
            RV32BinMapper.OpCode("0000000 00000 00000 110 00000 1100011", arrayOf(RV32BinMapper.MaskLabel.IMM7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.IMM5, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[RV32BinMapper.MaskLabel.IMM7]
                    val imm5 = paramMap[RV32BinMapper.MaskLabel.IMM5]
                    val pc = arch.getRegContainer().pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Bit5()).getRawBinStr()
                        val imm12 = Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Bit12())
                        val offset = imm12.toBin().getResized(RV32.XLEN) shl 1
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
            "BGEU", false, ParamType.RS1_RS2_I12,
            RV32BinMapper.OpCode("0000000 00000 00000 111 00000 1100011", arrayOf(RV32BinMapper.MaskLabel.IMM7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.IMM5, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]
                if (rs2Addr != null && rs1Addr != null) {
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm7 = paramMap[RV32BinMapper.MaskLabel.IMM7]
                    val imm5 = paramMap[RV32BinMapper.MaskLabel.IMM5]
                    val pc = arch.getRegContainer().pc
                    if (rs2 != null && imm5 != null && imm7 != null && rs1 != null) {
                        val imm7str = imm7.getResized(Bit7()).getRawBinStr()
                        val imm5str = imm5.getResized(Bit5()).getRawBinStr()
                        val imm12 = Bin(imm7str[0].toString() + imm5str[4] + imm7str.substring(1) + imm5str.substring(0, 4), Bit12())
                        val offset = imm12.toBin().getResized(RV32.XLEN) shl 1
                        if (rs1.get().toUDec() >= rs2.get().toUDec()) {
                            pc.set(pc.get() + offset)
                        } else {
                            pc.set(pc.get() + Hex("4"))
                        }
                    }
                }
            }
        },
        BEQ1("BEQ", true, ParamType.PS_RS1_RS2_JLBL, relative = BEQ),
        BNE1("BNE", true, ParamType.PS_RS1_RS2_JLBL, relative = BNE),
        BLT1("BLT", true, ParamType.PS_RS1_RS2_JLBL, relative = BLT),
        BGE1("BGE", true, ParamType.PS_RS1_RS2_JLBL, relative = BGE),
        BLTU1("BLTU", true, ParamType.PS_RS1_RS2_JLBL, relative = BLTU),
        BGEU1("BGEU", true, ParamType.PS_RS1_RS2_JLBL, relative = BGEU),
        LB("LB", false, ParamType.RD_OFF12, RV32BinMapper.OpCode("000000000000 00000 000 00000 0000011", arrayOf(RV32BinMapper.MaskLabel.IMM12, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val imm12 = paramMap[RV32BinMapper.MaskLabel.IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV32.XLEN)
                        val loadedByte = arch.getMemory().load(memAddr.toHex()).toBin().getResized(RV32.XLEN)
                        rd.set(loadedByte)
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        LH("LH", false, ParamType.RD_OFF12, RV32BinMapper.OpCode("000000000000 00000 001 00000 0000011", arrayOf(RV32BinMapper.MaskLabel.IMM12, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val imm12 = paramMap[RV32BinMapper.MaskLabel.IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV32.XLEN)
                        val loadedHalfWord = arch.getMemory().load(memAddr.toHex(), 2).toBin().getResized(RV32.XLEN)
                        rd.set(loadedHalfWord)
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        LW("LW", false, ParamType.RD_OFF12, RV32BinMapper.OpCode("000000000000 00000 010 00000 0000011", arrayOf(RV32BinMapper.MaskLabel.IMM12, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val imm12 = paramMap[RV32BinMapper.MaskLabel.IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV32.XLEN)
                        val loadedWord = arch.getMemory().load(memAddr.toHex(), 4).toBin().getResized(RV32.XLEN)
                        rd.set(loadedWord)
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        LBU("LBU", false, ParamType.RD_OFF12, RV32BinMapper.OpCode("000000000000 00000 100 00000 0000011", arrayOf(RV32BinMapper.MaskLabel.IMM12, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val imm12 = paramMap[RV32BinMapper.MaskLabel.IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV32.XLEN)
                        val loadedByte = arch.getMemory().load(memAddr.toHex())
                        rd.set(Bin(rd.get().toBin().getRawBinStr().substring(0, RV32.XLEN.bitWidth - 8) + loadedByte.toBin().getRawBinStr(), RV32.XLEN))
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        LHU("LHU", false, ParamType.RD_OFF12, RV32BinMapper.OpCode("000000000000 00000 101 00000 0000011", arrayOf(RV32BinMapper.MaskLabel.IMM12, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val imm12 = paramMap[RV32BinMapper.MaskLabel.IMM12]
                if (rdAddr != null && rs1Addr != null && imm12 != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null) {
                        val memAddr = rs1.get().toBin() + imm12.getResized(RV32.XLEN)
                        val loadedByte = arch.getMemory().load(memAddr.toHex(), 2)
                        rd.set(Bin(rd.get().toBin().getRawBinStr().substring(0, RV32.XLEN.bitWidth - 16) + loadedByte.toBin().getRawBinStr(), RV32.XLEN))
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        SB(
            "SB", false, ParamType.RS2_OFF12,
            RV32BinMapper.OpCode("0000000 00000 00000 000 00000 0100011", arrayOf(RV32BinMapper.MaskLabel.IMM7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.IMM5, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]
                val imm5 = paramMap[RV32BinMapper.MaskLabel.IMM5]
                val imm7 = paramMap[RV32BinMapper.MaskLabel.IMM7]
                if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rs1 != null && rs2 != null) {
                        val off64 = (imm7.getResized(RV32.XLEN) shl 5) + imm5
                        val memAddr = rs1.get().toBin().getResized(RV32.XLEN) + off64
                        arch.getMemory().store(memAddr, rs2.get().toBin().getResized(Bit8()))
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        SH(
            "SH", false, ParamType.RS2_OFF12,
            RV32BinMapper.OpCode("0000000 00000 00000 001 00000 0100011", arrayOf(RV32BinMapper.MaskLabel.IMM7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.IMM5, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]
                val imm5 = paramMap[RV32BinMapper.MaskLabel.IMM5]
                val imm7 = paramMap[RV32BinMapper.MaskLabel.IMM7]
                if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rs1 != null && rs2 != null) {
                        val off64 = (imm7.getResized(RV32.XLEN) shl 5) + imm5
                        val memAddr = rs1.get().toBin().getResized(RV32.XLEN) + off64
                        arch.getMemory().store(memAddr, rs2.get().toBin().getResized(Bit16()))
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        SW(
            "SW", false, ParamType.RS2_OFF12,
            RV32BinMapper.OpCode("0000000 00000 00000 010 00000 0100011", arrayOf(RV32BinMapper.MaskLabel.IMM7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.IMM5, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]
                val imm5 = paramMap[RV32BinMapper.MaskLabel.IMM5]
                val imm7 = paramMap[RV32BinMapper.MaskLabel.IMM7]
                if (rs1Addr != null && rs2Addr != null && imm5 != null && imm7 != null) {
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rs1 != null && rs2 != null) {
                        val off64 = (imm7.getResized(RV32.XLEN) shl 5) + imm5
                        val memAddr = rs1.variable.get().toBin().getResized(RV32.XLEN) + off64
                        arch.getMemory().store(memAddr, rs2.get().toBin().getResized(Bit32()))
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        ADDI("ADDI", false, ParamType.RD_RS1_I12, RV32BinMapper.OpCode("000000000000 00000 000 00000 0010011", arrayOf(RV32BinMapper.MaskLabel.IMM12, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[RV32BinMapper.MaskLabel.IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getResized(RV32.XLEN)
                        val sum = rs1.get().toBin() + paddedImm64
                        rd.set(sum)
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        SLTI("SLTI", false, ParamType.RD_RS1_I12, RV32BinMapper.OpCode("000000000000 00000 010 00000 0010011", arrayOf(RV32BinMapper.MaskLabel.IMM12, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[RV32BinMapper.MaskLabel.IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getResized(RV32.XLEN)
                        rd.set(if (rs1.get().toDec() < paddedImm64.toDec()) Bin("1", RV32.XLEN) else Bin("0", RV32.XLEN))
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        SLTIU("SLTIU", false, ParamType.RD_RS1_I12, RV32BinMapper.OpCode("000000000000 00000 011 00000 0010011", arrayOf(RV32BinMapper.MaskLabel.IMM12, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[RV32BinMapper.MaskLabel.IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getUResized(RV32.XLEN)
                        rd.set(if (rs1.get().toBin() < paddedImm64) Bin("1", RV32.XLEN) else Bin("0", RV32.XLEN))
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        XORI("XORI", false, ParamType.RD_RS1_I12, RV32BinMapper.OpCode("000000000000 00000 100 00000 0010011", arrayOf(RV32BinMapper.MaskLabel.IMM12, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[RV32BinMapper.MaskLabel.IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getUResized(RV32.XLEN)
                        rd.set(rs1.get().toBin() xor paddedImm64)
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        ORI("ORI", false, ParamType.RD_RS1_I12, RV32BinMapper.OpCode("000000000000 00000 110 00000 0010011", arrayOf(RV32BinMapper.MaskLabel.IMM12, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[RV32BinMapper.MaskLabel.IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getUResized(RV32.XLEN)
                        rd.set(rs1.get().toBin() or paddedImm64)
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        ANDI("ANDI", false, ParamType.RD_RS1_I12, RV32BinMapper.OpCode("000000000000 00000 111 00000 0010011", arrayOf(RV32BinMapper.MaskLabel.IMM12, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val imm12 = paramMap[RV32BinMapper.MaskLabel.IMM12]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && imm12 != null && rs1 != null) {
                        val paddedImm64 = imm12.getUResized(RV32.XLEN)
                        rd.set(rs1.get().toBin() and paddedImm64)
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        SLLI(
            "SLLI", false, ParamType.RD_RS1_I5,
            RV32BinMapper.OpCode("0000000 00000 00000 001 00000 0010011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.SHAMT, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val shamt5 = paramMap[RV32BinMapper.MaskLabel.SHAMT]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && shamt5 != null && rs1 != null) {
                        rd.set(rs1.get().toBin() ushl shamt5.getRawBinStr().toInt(2))
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        SRLI(
            "SRLI", false, ParamType.RD_RS1_I5,
            RV32BinMapper.OpCode("0000000 00000 00000 101 00000 0010011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.SHAMT, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val shamt5 = paramMap[RV32BinMapper.MaskLabel.SHAMT]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && shamt5 != null && rs1 != null) {
                        rd.set(rs1.get().toBin() ushr shamt5.getRawBinStr().toInt(2))
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        SRAI(
            "SRAI", false, ParamType.RD_RS1_I5,
            RV32BinMapper.OpCode("0100000 00000 00000 101 00000 0010011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.SHAMT, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                if (rdAddr != null && rs1Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val shamt5 = paramMap[RV32BinMapper.MaskLabel.SHAMT]
                    val pc = arch.getRegContainer().pc
                    if (rd != null && shamt5 != null && rs1 != null) {
                        rd.set(rs1.get().toBin() shr shamt5.getRawBinStr().toInt(2))
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        ADD(
            "ADD", false, ParamType.RD_RS1_RS2,
            RV32BinMapper.OpCode("0000000 00000 00000 000 00000 0110011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]
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
        SUB(
            "SUB", false, ParamType.RD_RS1_RS2,
            RV32BinMapper.OpCode("0100000 00000 00000 000 00000 0110011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]
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
        SLL(
            "SLL", false, ParamType.RD_RS1_RS2,
            RV32BinMapper.OpCode("0000000 00000 00000 001 00000 0110011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin() ushl rs2.get().toBin().getUResized(Bit6()).getRawBinStr().toInt(2))
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        SLT(
            "SLT", false, ParamType.RD_RS1_RS2,
            RV32BinMapper.OpCode("0000000 00000 00000 010 00000 0110011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]
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
        SLTU(
            "SLTU", false, ParamType.RD_RS1_RS2,
            RV32BinMapper.OpCode("0000000 00000 00000 011 00000 0110011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]
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
        XOR(
            "XOR", false, ParamType.RD_RS1_RS2,
            RV32BinMapper.OpCode("0000000 00000 00000 100 00000 0110011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]
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
        SRL(
            "SRL", false, ParamType.RD_RS1_RS2,
            RV32BinMapper.OpCode("0000000 00000 00000 101 00000 0110011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin() ushr rs2.get().toBin().getUResized(Bit6()).getRawBinStr().toInt(2))
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        SRA(
            "SRA", false, ParamType.RD_RS1_RS2,
            RV32BinMapper.OpCode("0100000 00000 00000 101 00000 0110011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]
                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        rd.set(rs1.get().toBin() shr rs2.get().toBin().getUResized(Bit6()).getRawBinStr().toInt(2))
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        OR(
            "OR", false, ParamType.RD_RS1_RS2,
            RV32BinMapper.OpCode("0000000 00000 00000 110 00000 0110011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]
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
        AND(
            "AND", false, ParamType.RD_RS1_RS2,
            RV32BinMapper.OpCode("0000000 00000 00000 111 00000 0110011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE))
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]
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
        CSRRW(
            "CSRRW", false, ParamType.CSR_RD_OFF12_RS1,
            RV32BinMapper.OpCode("000000000000 00000 001 00000 1110011", arrayOf(RV32BinMapper.MaskLabel.CSR, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE)), needFeatures = listOf(RV32.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val csrAddr = paramMap[RV32BinMapper.MaskLabel.CSR]
                if (rdAddr != null && rs1Addr != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val csr = arch.getRegByAddr(csrAddr, RV32.CSR_REGFILE_NAME)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV32.XLEN)
                            rd.set(t)
                        }

                        csr.set(rs1.get())

                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        CSRRS(
            "CSRRS", false, ParamType.CSR_RD_OFF12_RS1,
            RV32BinMapper.OpCode("000000000000 00000 010 00000 1110011", arrayOf(RV32BinMapper.MaskLabel.CSR, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE)), needFeatures = listOf(RV32.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val csrAddr = paramMap[RV32BinMapper.MaskLabel.CSR]
                if (rdAddr != null && rs1Addr != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val csr = arch.getRegByAddr(csrAddr, RV32.CSR_REGFILE_NAME)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV32.XLEN)
                            rd.set(t)
                        }

                        csr.set(rs1.get().toBin() or csr.get().toBin())

                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        CSRRC(
            "CSRRC", false, ParamType.CSR_RD_OFF12_RS1,
            RV32BinMapper.OpCode("000000000000 00000 011 00000 1110011", arrayOf(RV32BinMapper.MaskLabel.CSR, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE)), needFeatures = listOf(RV32.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val csrAddr = paramMap[RV32BinMapper.MaskLabel.CSR]
                if (rdAddr != null && rs1Addr != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val csr = arch.getRegByAddr(csrAddr, RV32.CSR_REGFILE_NAME)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV32.XLEN)
                            rd.set(t)
                        }

                        csr.set(csr.get().toBin() and rs1.get().toBin().inv())

                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        CSRRWI(
            "CSRRWI", false, ParamType.CSR_RD_OFF12_UIMM5,
            RV32BinMapper.OpCode("000000000000 00000 101 00000 1110011", arrayOf(RV32BinMapper.MaskLabel.CSR, RV32BinMapper.MaskLabel.UIMM5, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE)), needFeatures = listOf(RV32.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val uimm5 = paramMap[RV32BinMapper.MaskLabel.UIMM5]
                val csrAddr = paramMap[RV32BinMapper.MaskLabel.CSR]
                if (rdAddr != null && uimm5 != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val csr = arch.getRegByAddr(csrAddr, RV32.CSR_REGFILE_NAME)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV32.XLEN)
                            rd.set(t)
                        }

                        csr.set(uimm5.getUResized(RV32.XLEN))

                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        CSRRSI(
            "CSRRSI", false, ParamType.CSR_RD_OFF12_UIMM5,
            RV32BinMapper.OpCode("000000000000 00000 110 00000 1110011", arrayOf(RV32BinMapper.MaskLabel.CSR, RV32BinMapper.MaskLabel.UIMM5, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE)), needFeatures = listOf(RV32.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val uimm5 = paramMap[RV32BinMapper.MaskLabel.UIMM5]
                val csrAddr = paramMap[RV32BinMapper.MaskLabel.CSR]
                if (rdAddr != null && uimm5 != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val csr = arch.getRegByAddr(csrAddr, RV32.CSR_REGFILE_NAME)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV32.XLEN)
                            rd.set(t)
                        }

                        csr.set(csr.get().toBin() or uimm5.getUResized(RV32.XLEN))

                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        CSRRCI(
            "CSRRCI", false, ParamType.CSR_RD_OFF12_UIMM5,
            RV32BinMapper.OpCode("000000000000 00000 111 00000 1110011", arrayOf(RV32BinMapper.MaskLabel.CSR, RV32BinMapper.MaskLabel.UIMM5, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE)), needFeatures = listOf(RV32.EXTENSION.CSR.ordinal)
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val uimm5 = paramMap[RV32BinMapper.MaskLabel.UIMM5]
                val csrAddr = paramMap[RV32BinMapper.MaskLabel.CSR]
                if (rdAddr != null && uimm5 != null && csrAddr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val csr = arch.getRegByAddr(csrAddr, RV32.CSR_REGFILE_NAME)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && csr != null) {
                        if (rd.address.toHex().getRawHexStr() != "00000") {
                            val t = csr.get().toBin().getUResized(RV32.XLEN)
                            rd.set(t)
                        }

                        csr.set(csr.get().toBin() and uimm5.getUResized(RV32.XLEN).inv())

                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },

        CSRW("CSRW", true, ParamType.PS_CSR_RS1, needFeatures = listOf(RV32.EXTENSION.CSR.ordinal)),
        CSRR("CSRR", true, ParamType.PS_RD_CSR, needFeatures = listOf(RV32.EXTENSION.CSR.ordinal)),

        // M Extension
        MUL(
            "MUL",
            false,
            ParamType.RD_RS1_RS2,
            RV32BinMapper.OpCode("0000001 00000 00000 000 00000 0110011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE)),
            needFeatures = listOf(RV32.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]

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
        MULH(
            "MULH",
            false,
            ParamType.RD_RS1_RS2,
            RV32BinMapper.OpCode("0000001 00000 00000 001 00000 0110011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE)),
            needFeatures = listOf(RV32.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = factor1.flexTimesSigned(factor2, false).ushr(RV32.XLEN.bitWidth).getResized(RV32.XLEN)
                        rd.set(result)
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        MULHSU(
            "MULHSU",
            false,
            ParamType.RD_RS1_RS2,
            RV32BinMapper.OpCode("0000001 00000 00000 010 00000 0110011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE)),
            needFeatures = listOf(RV32.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = factor1.flexTimesSigned(factor2, resizeToLargestParamSize = false, true).ushr(RV32.XLEN.bitWidth).getResized(RV32.XLEN)
                        rd.set(result)
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        MULHU(
            "MULHU",
            false,
            ParamType.RD_RS1_RS2,
            RV32BinMapper.OpCode("0000001 00000 00000 011 00000 0110011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE)),
            needFeatures = listOf(RV32.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]

                if (rdAddr != null && rs1Addr != null && rs2Addr != null) {
                    val rd = arch.getRegByAddr(rdAddr)
                    val rs1 = arch.getRegByAddr(rs1Addr)
                    val rs2 = arch.getRegByAddr(rs2Addr)
                    val pc = arch.getRegContainer().pc
                    if (rd != null && rs1 != null && rs2 != null) {
                        val factor1 = rs1.get().toBin()
                        val factor2 = rs2.get().toBin()
                        val result = (factor1 * factor2).toBin().ushr(RV32.XLEN.bitWidth).getUResized(RV32.XLEN)
                        rd.set(result)
                        pc.set(pc.get() + Hex("4"))
                    }
                }
            }
        },
        DIV(
            "DIV",
            false,
            ParamType.RD_RS1_RS2,
            RV32BinMapper.OpCode("0000001 00000 00000 100 00000 0110011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE)),
            needFeatures = listOf(RV32.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]

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
        DIVU(
            "DIVU",
            false,
            ParamType.RD_RS1_RS2,
            RV32BinMapper.OpCode("0000001 00000 00000 101 00000 0110011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE)),
            needFeatures = listOf(RV32.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]

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
        REM(
            "REM",
            false,
            ParamType.RD_RS1_RS2,
            RV32BinMapper.OpCode("0000001 00000 00000 110 00000 0110011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE)),
            needFeatures = listOf(RV32.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]

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
        REMU(
            "REMU",
            false,
            ParamType.RD_RS1_RS2,
            RV32BinMapper.OpCode("0000001 00000 00000 111 00000 0110011", arrayOf(RV32BinMapper.MaskLabel.FUNCT7, RV32BinMapper.MaskLabel.RS2, RV32BinMapper.MaskLabel.RS1, RV32BinMapper.MaskLabel.FUNCT3, RV32BinMapper.MaskLabel.RD, RV32BinMapper.MaskLabel.OPCODE)),
            needFeatures = listOf(RV32.EXTENSION.M.ordinal)
        ) {
            override fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
                super.execute(arch, paramMap)
                val rdAddr = paramMap[RV32BinMapper.MaskLabel.RD]
                val rs1Addr = paramMap[RV32BinMapper.MaskLabel.RS1]
                val rs2Addr = paramMap[RV32BinMapper.MaskLabel.RS2]

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

        Nop("NOP", true, ParamType.PS_NONE),
        Mv("MV", true, ParamType.PS_RD_RS1),
        Li("LI", true, ParamType.PS_RD_I32, memWords = 2),
        La("LA", true, ParamType.PS_RD_ALBL, memWords = 2),
        Not("NOT", true, ParamType.PS_RD_RS1),
        Neg("NEG", true, ParamType.PS_RD_RS1),
        Seqz("SEQZ", true, ParamType.PS_RD_RS1),
        Snez("SNEZ", true, ParamType.PS_RD_RS1),
        Sltz("SLTZ", true, ParamType.PS_RD_RS1),
        Sgtz("SGTZ", true, ParamType.PS_RD_RS1),
        Beqz("BEQZ", true, ParamType.PS_RS1_JLBL),
        Bnez("BNEZ", true, ParamType.PS_RS1_JLBL),
        Blez("BLEZ", true, ParamType.PS_RS1_JLBL),
        Bgez("BGEZ", true, ParamType.PS_RS1_JLBL),
        Bltz("BLTZ", true, ParamType.PS_RS1_JLBL),
        BGTZ("BGTZ", true, ParamType.PS_RS1_JLBL),
        Bgt("BGT", true, ParamType.PS_RS1_RS2_JLBL),
        Ble("BLE", true, ParamType.PS_RS1_RS2_JLBL),
        Bgtu("BGTU", true, ParamType.PS_RS1_RS2_JLBL),
        Bleu("BLEU", true, ParamType.PS_RS1_RS2_JLBL),
        J("J", true, ParamType.PS_JLBL),
        JAL1("JAL", true, ParamType.PS_RS1_JLBL, relative = JAL),
        JAL2("JAL", true, ParamType.PS_JLBL, relative = JAL),
        Jr("JR", true, ParamType.PS_RS1),
        JALR1("JALR", true, ParamType.PS_RS1, relative = JALR),
        JALR2("JALR", true, ParamType.RD_OFF12, relative = JALR),
        Ret("RET", true, ParamType.PS_NONE),
        Call("CALL", true, ParamType.PS_JLBL, memWords = 2),
        Tail("TAIL", true, ParamType.PS_JLBL, memWords = 2);

        open fun execute(arch: Architecture, paramMap: Map<RV32BinMapper.MaskLabel, Bin>) {
            arch.getConsole().log("> $id {...}")
        }
    }

    enum class DirMajType(val docName: String) {
        PRE("Pre resolved directive"),
        SECTIONSTART("Section identification"),
        DE_ALIGNED("Data emitting aligned"),
        DE_UNALIGNED("Data emitting unaligned"),
        ASSEMLYINFO("Optional assembly information")
    }

    enum class DirType(val dirname: String, val dirMajType: DirMajType, val tokenSeq: TokenSeq, val deSize: Variable.Size? = null) {
        EQU("equ", DirMajType.PRE, TokenSeq(Specific(".equ", ignoreCase = true))),
        MACRO("macro", DirMajType.PRE, TokenSeq(Specific(".macro", ignoreCase = true))),
        ENDM("endm", DirMajType.PRE, TokenSeq(Specific(".endm", ignoreCase = true))),

        TEXT("text", DirMajType.SECTIONSTART, TokenSeq(Specific(".text", ignoreCase = true))),
        DATA("data", DirMajType.SECTIONSTART, TokenSeq(Specific(".data", ignoreCase = true))),
        RODATA("rodata", DirMajType.SECTIONSTART, TokenSeq(Specific(".rodata", ignoreCase = true))),
        BSS("bss", DirMajType.SECTIONSTART, TokenSeq(Specific(".bss", ignoreCase = true))),

        BYTE("byte", DirMajType.DE_ALIGNED, TokenSeq(Specific(".byte", ignoreCase = true)), Bit8()),
        HALF("half", DirMajType.DE_ALIGNED, TokenSeq(Specific(".half", ignoreCase = true)), Bit16()),
        WORD("word", DirMajType.DE_ALIGNED, TokenSeq(Specific(".word", ignoreCase = true)), Bit32()),
        DWORD("dword", DirMajType.DE_ALIGNED, TokenSeq(Specific(".dword", ignoreCase = true)), Bit64()),
        ASCIZ("asciz", DirMajType.DE_ALIGNED, TokenSeq(Specific(".asciz", ignoreCase = true))),
        STRING("string", DirMajType.DE_ALIGNED, TokenSeq(Specific(".string", ignoreCase = true))),

        BYTE_2("2byte", DirMajType.DE_UNALIGNED, TokenSeq(Specific(".2byte", ignoreCase = true)), Bit16()),
        BYTE_4("4byte", DirMajType.DE_UNALIGNED, TokenSeq(Specific(".4byte", ignoreCase = true)), Bit32()),
        BYTE_8("8byte", DirMajType.DE_UNALIGNED, TokenSeq(Specific(".8byte", ignoreCase = true)), Bit64()),

        GLOBAL("global", DirMajType.ASSEMLYINFO, TokenSeq(Specific(".global"), Space, WordNoDots)),
        GLOBL(".globl", DirMajType.ASSEMLYINFO, TokenSeq(Specific(".globl"), Space, WordNoDots))
    }


    /**
     * Syntax Tree Tokens
     */

    class PREComment(vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RV32Flags.comment), "comment", *tokens)
    class PreEquDef(directive: List<Compiler.Token>, val equname: Compiler.Token.Word, comma: Compiler.Token.Symbol, val constant: Compiler.Token.Constant) : TreeNode.ElementNode(
        ConnectedHL(RV32Flags.directive to directive, RV32Flags.pre_equ to listOf(equname), RV32Flags.constant to listOf(constant), RV32Flags.pre_equ to listOf(comma)),
        "equ_def",
        *directive.toTypedArray(),
        equname,
        comma,
        constant
    )

    class PreEquRep(token: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RV32Flags.pre_equ), "equ_insert", token)
    class PreMacroDef(directives: List<Compiler.Token>, val macroname: Compiler.Token.Word, val attributes: List<Compiler.Token.Word>, commas: List<Compiler.Token>, val macroContent: List<Compiler.Token>) : TreeNode.ElementNode(
        ConnectedHL(
            RV32Flags.directive to directives,
            RV32Flags.pre_macro to listOf(macroname, *attributes.toTypedArray(), *macroContent.filterNot { it is Compiler.Token.Constant || it is Compiler.Token.Register }.toTypedArray(), *commas.toTypedArray()),
            RV32Flags.constant to macroContent.filterIsInstance<Compiler.Token.Constant>(),
            RV32Flags.register to macroContent.filterIsInstance<Compiler.Token.Register>()
        ),
        "macro_def",
        *directives.toTypedArray(),
        macroname,
        *attributes.toTypedArray(),
        *commas.toTypedArray(),
        *macroContent.toTypedArray()
    ) {
        fun getMacroReplacement(params: List<Compiler.Token>, arch: Architecture): List<Compiler.Token>? {
            if (params.size != attributes.size) return null
            val content = macroContent.toMutableList()
            val contentRep = mutableListOf<Compiler.Token>()

            while (content.first() is Compiler.Token.Space) {
                content.removeFirst()
            }

            while (content.isNotEmpty()) {
                // Check for Attribute Insert
                val result = Seqs.SeqMacroAttrInsert.matchStart(*content.toTypedArray())
                if (result.matches) {
                    val tokensToReplace = result.sequenceMap.map { it.token }
                    val attribute = attributes.firstOrNull { it.content == tokensToReplace[1].content }
                    if (attribute != null) {
                        content.removeAll(tokensToReplace)
                        val pseudoParam = arch.getCompiler().pseudoTokenize(params[attributes.indexOf(attribute)].content)
                        contentRep.add(pseudoParam.first())
                        continue
                    }
                }

                // Else add to replace content
                val pseudoToken = arch.getCompiler().pseudoTokenize(content.removeFirst().content)
                contentRep.add(pseudoToken.first())
            }
            return contentRep
        }
    }

    class PreMacroRep(macroName: Compiler.Token, attributes: List<Compiler.Token>, commas: List<Compiler.Token>) : TreeNode.ElementNode(
        ConnectedHL(
            RV32Flags.pre_macro to commas + macroName,
            RV32Flags.constant to attributes.filterIsInstance<Compiler.Token.Constant>(),
            RV32Flags.register to attributes.filterIsInstance<Compiler.Token.Register>(),
            RV32Flags.label to attributes.filterIsInstance<Compiler.Token.Word>()
        ),
        "macro_insert",
        macroName,
        *attributes.toTypedArray(),
        *commas.toTypedArray()
    )

    class PreImport(symbol: Compiler.Token, word: Compiler.Token, space: Compiler.Token, filename: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RV32Flags.pre_import to listOf(symbol, word), RV32Flags.constant to listOf(filename)), "import", symbol, word, space, filename)

    class ELabel(currentLabel: ELabel? = null, nameToken: Compiler.Token, endSymbol: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RV32Flags.label), "label", *setOfNotNull(nameToken, endSymbol).toTypedArray()) {

        val spaceSub: Boolean = nameToken.content.startsWith(".")
        val parentLabel = if (spaceSub) {
            currentLabel
        } else {
            null
        }
        val nameString = nameToken.content

        var address: Variable.Value? = null

        fun setAddress(address: Variable.Value) {
            this.address = address
        }
    }

    class EInstr(val type: InstrType, val paramType: ParamType, nameToken: Compiler.Token, val params: List<Compiler.Token>, spaces: List<Compiler.Token>, val parentLabel: ELabel? = null) : TreeNode.ElementNode(
        ConnectedHL(
            RV32Flags.instruction to listOf(nameToken),
            RV32Flags.register to params.filterIsInstance<Compiler.Token.Register>(),
            RV32Flags.constant to params.filterIsInstance<Compiler.Token.Constant>(),
            RV32Flags.label to params.filterIsInstance<Compiler.Token.Word>(),
        ), "instr", nameToken, *params.toTypedArray(), *spaces.toTypedArray()
    ) {
        val registers: Collection<Compiler.Token.Register>
        val constants: Collection<Compiler.Token.Constant>
        val unlinkedlabels: Collection<Compiler.Token.Word>
        val linkedLabels: MutableCollection<ELabel> = mutableListOf()

        var address: Variable.Value? = null

        init {
            registers = params.filterIsInstance<Compiler.Token.Register>()
            constants = params.filterIsInstance<Compiler.Token.Constant>()
            unlinkedlabels = params.filterIsInstance<Compiler.Token.Word>()
        }

        /**
         * Returns true if all labels where linked correctly
         */
        fun link(labels: Collection<ELabel>, errors: MutableList<Error>): Boolean {
            var linkingErrors = false
            for (unlinkedlabel in unlinkedlabels) {
                val label = if (unlinkedlabel.content.startsWith(".")) {
                    labels.firstOrNull { it.parentLabel == parentLabel && it.nameString == unlinkedlabel.content }
                } else {
                    labels.firstOrNull { if (it.parentLabel != null) "${it.parentLabel.nameString}${it.nameString}" == unlinkedlabel.content else it.nameString == unlinkedlabel.content }
                }
                if (label == null) {
                    linkingErrors = true
                    errors.add(Error("(${unlinkedlabel.content}) couldn't get linked to any label!", unlinkedlabel))
                    continue
                }
                linkedLabels.add(label)
            }
            return !linkingErrors
        }

        fun setAddress(address: Variable.Value) {
            this.address = address
        }
    }

    class EInitData(val dirType: DirType, val dirTokens: List<Compiler.Token>, val constants: List<Compiler.Token.Constant>, commas: List<Compiler.Token>) :
        TreeNode.ElementNode(ConnectedHL(RV32Flags.directive to dirTokens, RV32Flags.constant to constants), "init_data", *dirTokens.toTypedArray(), *constants.toTypedArray(), *commas.toTypedArray()) {
        val values = constants.map { it.getValue(dirType.deSize) }
        val bytesNeeded: Variable.Value
        var address: Variable.Value? = null

        init {
            var result = 0
            values.forEach { result += it.size.getByteCount() }
            bytesNeeded = Hex(result.toString(16), RV32.MEM_ADDRESS_WIDTH)
        }

        fun setAddress(address: Variable.Value) {
            this.address = address
        }
    }

    class EUnInitData(val dirType: DirType, val dirTokens: List<Compiler.Token>) : TreeNode.ElementNode(ConnectedHL(RV32Flags.directive to dirTokens), "uninit_data", *dirTokens.toTypedArray()) {
        var address: Variable.Value? = null

        fun setAddress(address: Variable.Value) {
            this.address = address
        }
    }

    class ESecStart(val dirType: DirType, val dirTokens: List<Compiler.Token>) : TreeNode.ElementNode(ConnectedHL(RV32Flags.directive to dirTokens), "sec_start", *dirTokens.toTypedArray())

    class EGlobal(val directive: Compiler.Token, space: Compiler.Token, val labelname: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(RV32Flags.directive to listOf(directive), RV32Flags.label to listOf(labelname)), "global", directive, space, labelname) {
        var linkedlabel: ELabel? = null

        fun link(labels: List<ELabel>): Boolean {
            linkedlabel = labels.firstOrNull { !it.spaceSub && it.nameString == labelname.content } ?: return false
            return true
        }
    }

    class ESetPC(symbols: List<Compiler.Token>, val constant: Compiler.Token.Constant) : TreeNode.ElementNode(ConnectedHL(RV32Flags.setpc to symbols, RV32Flags.constant to listOf(constant)), "setpc", *symbols.toTypedArray(), constant)

    class SText(secStart: ESecStart? = null, vararg val elements: ElementNode) : TreeNode.SectionNode("text", collNodes = if (secStart != null) elements + secStart else elements)
    class SData(secStart: ESecStart, vararg val elements: ElementNode) : TreeNode.SectionNode("data", secStart, *elements)
    class SRoData(secStart: ESecStart, vararg val elements: ElementNode) : TreeNode.SectionNode("rodata", secStart, *elements)
    class SBss(secStart: ESecStart, vararg val elements: ElementNode) : TreeNode.SectionNode("bss", secStart, *elements)

    class CSections(vararg val sections: SectionNode) : TreeNode.ContainerNode("sections", *sections)

}