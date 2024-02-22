package emulator.kit.assembly.standards

import emulator.kit.Architecture
import emulator.kit.assembly.Compiler
import emulator.kit.assembly.Syntax
import emulator.kit.common.FileHandler
import emulator.kit.common.Transcript
import emulator.kit.types.Variable
import emulator.kit.assembly.Syntax.TokenSeq.Component.*

abstract class StandardSyntax(val memAddressWidth: Variable.Size, val commentStartSymbol: Char, val instrParamsCanContainWordsBesideLabels: Boolean) : Syntax() {

    override val applyStandardHLForRest: Boolean = false
    override fun clear() {}
    override fun check(arch: Architecture, compiler: Compiler, tokens: List<Compiler.Token>, others: List<FileHandler.File>, transcript: Transcript): SyntaxTree {
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

        var errorCount = 0

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
            errors.add(Error("Unexpected ${remainingTokens.first()::class.simpleName} (${remainingTokens.first().content})!", remainingTokens.removeFirst()))
            ++errorCount
            if (errorCount > 10) {
                break
            }
        }
        remainingTokens.removeAll { it is Compiler.Token.Space || it is Compiler.Token.NewLine }
        if (remainingTokens.isNotEmpty()) {
            errors.add(Error("Faulty Syntax! Check Syntax Examples!", *remainingTokens.toTypedArray()))
        }

        // Link
        elements.linkLabels(sections, errors)

        elements.bundleSections(sections, errors, warnings)

        return SyntaxTree(TreeNode.RootNode(errors, warnings, TreeNode.ContainerNode("pre", *preElements.toTypedArray()), CSections(*sections.toTypedArray())))
    }

    /**
     * Syntax Detection Functions
     */
    /**
     * Removes all comments from the compiler tokens and adds them to the preElements
     */
    private fun MutableList<Compiler.Token>.removeComments(preElements: MutableList<TreeNode.ElementNode>): MutableList<Compiler.Token> {
        while (true) {
            val commentStart = this.firstOrNull { it.content == commentStartSymbol.toString() } ?: break
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
                val pseudoConst = arch.getCompiler().pseudoTokenize(equDef.constant.content, foundToken.lineLoc).firstOrNull()
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
                this.addAll(indexOfName, macro.getMacroReplacement(params, macroNameRef.lineLoc,arch) ?: listOf())
                preElements.add(preMacroRep)
            }
        }

        return this
    }

    /**
     * Resolves other file imports
     */
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

    /**
     * Checks beginning for data
     */
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
                elements.add(EInitData(dirType, dirTokens, constants, commas, memAddressWidth))
            } else {
                elements.add(EUnInitData(dirType, dirTokens))
            }
            this.removeAll(constants)
            this.removeAll(commas)

            return true
        }

        return false
    }

    /**
     * Checks beginning for a label
     */
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

    /**
     * Checks beginning for a Instruction
     */
    abstract fun MutableList<Compiler.Token>.checkInstr(elements: MutableList<TreeNode.ElementNode>, errors: MutableList<Error>, warnings: MutableList<Warning>, currentLabel: ELabel?): Boolean

    /**
     * Checks beginning for a section start
     */
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

    /**
     * Checks beginning for other assembler information
     */
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

        val setPcResult = Seqs.getSetPC(memAddressWidth).matchStart(*this.toTypedArray())
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

    /**
     * Links all labels
     */
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
            val valid = instr.link(labels, errors, instrParamsCanContainWordsBesideLabels)
            if (!valid) {
                this.remove(instr)
            }
        }
    }

    /**
     * Bundles the nodes to sections
     */
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

    /**
     * Sequences
     */
    data object Seqs {
        val SeqAfterEquDir = TokenSeq(InSpecific.WordNoDots, Specific(","), InSpecific.Constant, ignoreSpaces = true)
        val SeqMacroAttrInsert = TokenSeq(Specific("""\"""), InSpecific.WordNoDots)
        val SeqLabel = TokenSeq(InSpecific.Word, Specific(":"))
        val SeqImport = TokenSeq(Specific("#"), Specific("import"), InSpecific.Space, InSpecific.StringConst)
        fun getSetPC(memAddressWidth: Variable.Size): TokenSeq = TokenSeq(Specific("*"), Specific("="), SpecConst(memAddressWidth))
    }


    /**
     * Directives
     */

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

        BYTE("byte", DirMajType.DE_ALIGNED, TokenSeq(Specific(".byte", ignoreCase = true)), Variable.Size.Bit8()),
        HALF("half", DirMajType.DE_ALIGNED, TokenSeq(Specific(".half", ignoreCase = true)), Variable.Size.Bit16()),
        WORD("word", DirMajType.DE_ALIGNED, TokenSeq(Specific(".word", ignoreCase = true)), Variable.Size.Bit32()),
        DWORD("dword", DirMajType.DE_ALIGNED, TokenSeq(Specific(".dword", ignoreCase = true)), Variable.Size.Bit64()),
        ASCIZ("asciz", DirMajType.DE_ALIGNED, TokenSeq(Specific(".asciz", ignoreCase = true))),
        STRING("string", DirMajType.DE_ALIGNED, TokenSeq(Specific(".string", ignoreCase = true))),

        BYTE_2("2byte", DirMajType.DE_UNALIGNED, TokenSeq(Specific(".2byte", ignoreCase = true)), Variable.Size.Bit16()),
        BYTE_4("4byte", DirMajType.DE_UNALIGNED, TokenSeq(Specific(".4byte", ignoreCase = true)), Variable.Size.Bit32()),
        BYTE_8("8byte", DirMajType.DE_UNALIGNED, TokenSeq(Specific(".8byte", ignoreCase = true)), Variable.Size.Bit64()),

        GLOBAL("global", DirMajType.ASSEMLYINFO, TokenSeq(Specific(".global"), InSpecific.Space, InSpecific.WordNoDots)),
        GLOBL("globl", DirMajType.ASSEMLYINFO, TokenSeq(Specific(".globl"), InSpecific.Space, InSpecific.WordNoDots))
    }


    /**
     * Nodes
     */

    class PREComment(vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(StandardFlags.comment), "comment", *tokens)
    class PreEquDef(directive: List<Compiler.Token>, val equname: Compiler.Token.Word, comma: Compiler.Token.Symbol, val constant: Compiler.Token.Constant) : TreeNode.ElementNode(
        ConnectedHL(StandardFlags.directive to directive, StandardFlags.pre_equ to listOf(equname), StandardFlags.constant to listOf(constant), StandardFlags.pre_equ to listOf(comma)),
        "equ_def",
        *directive.toTypedArray(),
        equname,
        comma,
        constant
    )

    class PreEquRep(token: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(StandardFlags.pre_equ), "equ_insert", token)
    class PreMacroDef(directives: List<Compiler.Token>, val macroname: Compiler.Token.Word, val attributes: List<Compiler.Token.Word>, commas: List<Compiler.Token>, val macroContent: List<Compiler.Token>) : TreeNode.ElementNode(
        ConnectedHL(
            StandardFlags.directive to directives,
            StandardFlags.pre_macro to listOf(macroname, *attributes.toTypedArray(), *macroContent.filterNot { it is Compiler.Token.Constant || it is Compiler.Token.Register }.toTypedArray(), *commas.toTypedArray()),
            StandardFlags.constant to macroContent.filterIsInstance<Compiler.Token.Constant>(),
            StandardFlags.register to macroContent.filterIsInstance<Compiler.Token.Register>()
        ),
        "macro_def",
        *directives.toTypedArray(),
        macroname,
        *attributes.toTypedArray(),
        *commas.toTypedArray(),
        *macroContent.toTypedArray()
    ) {
        fun getMacroReplacement(params: List<Compiler.Token>, lineLoc: Compiler.LineLoc,arch: Architecture): List<Compiler.Token>? {
            if (params.size != attributes.size) return null
            val content = macroContent.toMutableList()
            val contentRep = mutableListOf<Compiler.Token>()

            while (content.first() is Compiler.Token.Space) {
                content.removeFirst()
            }

            while (content.isNotEmpty()) {
                // Check for Attribute Insert
                val result = StandardSyntax.Seqs.SeqMacroAttrInsert.matchStart(*content.toTypedArray())
                if (result.matches) {
                    val tokensToReplace = result.sequenceMap.map { it.token }
                    val attribute = attributes.firstOrNull { it.content == tokensToReplace[1].content }
                    if (attribute != null) {
                        content.removeAll(tokensToReplace)
                        val pseudoParam = arch.getCompiler().pseudoTokenize(params[attributes.indexOf(attribute)].content, lineLoc)
                        contentRep.add(pseudoParam.first())
                        continue
                    }
                }

                // Else add to replace content
                val pseudoToken = arch.getCompiler().pseudoTokenize(content.removeFirst().content, lineLoc)
                contentRep.add(pseudoToken.first())
            }
            return contentRep
        }
    }

    class PreMacroRep(macroName: Compiler.Token, attributes: List<Compiler.Token>, commas: List<Compiler.Token>) : TreeNode.ElementNode(
        ConnectedHL(
            StandardFlags.pre_macro to commas + macroName,
            StandardFlags.constant to attributes.filterIsInstance<Compiler.Token.Constant>(),
            StandardFlags.register to attributes.filterIsInstance<Compiler.Token.Register>(),
            StandardFlags.label to attributes.filterIsInstance<Compiler.Token.Word>()
        ),
        "macro_insert",
        macroName,
        *attributes.toTypedArray(),
        *commas.toTypedArray()
    )

    class PreImport(symbol: Compiler.Token, word: Compiler.Token, space: Compiler.Token, filename: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(StandardFlags.pre_import to listOf(symbol, word), StandardFlags.constant to listOf(filename)), "import", symbol, word, space, filename)

    class ELabel(currentLabel: ELabel? = null, nameToken: Compiler.Token, endSymbol: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(StandardFlags.label), "label", *setOfNotNull(nameToken, endSymbol).toTypedArray()) {

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

    abstract class EInstr(val nameToken: Compiler.Token, val params: List<Compiler.Token>, val parentLabel: ELabel? = null) : TreeNode.ElementNode(
        ConnectedHL(
            StandardFlags.instruction to listOf(nameToken),
            StandardFlags.register to params.filterIsInstance<Compiler.Token.Register>(),
            StandardFlags.constant to params.filterIsInstance<Compiler.Token.Constant>(),
            StandardFlags.label to params.filterIsInstance<Compiler.Token.Word>(),
        ), "instr", nameToken, *params.toTypedArray()
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
        fun link(labels: Collection<ELabel>, errors: MutableList<Error>, instrParamsCanContainWordsBesideLabels: Boolean): Boolean {
            var linkingErrors = false
            for (unlinkedlabel in unlinkedlabels) {
                val label = if (unlinkedlabel.content.startsWith(".")) {
                    labels.firstOrNull { it.parentLabel == parentLabel && it.nameString == unlinkedlabel.content }
                } else {
                    labels.firstOrNull { if (it.parentLabel != null) "${it.parentLabel.nameString}${it.nameString}" == unlinkedlabel.content else it.nameString == unlinkedlabel.content }
                }
                if (label == null) {
                    if (!instrParamsCanContainWordsBesideLabels) {
                        linkingErrors = true
                        errors.add(Error("(${unlinkedlabel.content}) couldn't get linked to any label!", unlinkedlabel))
                    }
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

    class EInitData(val dirType: StandardSyntax.DirType, val dirTokens: List<Compiler.Token>, val constants: List<Compiler.Token.Constant>, commas: List<Compiler.Token>, memAddressWidth: Variable.Size) :
        TreeNode.ElementNode(ConnectedHL(StandardFlags.directive to dirTokens, StandardFlags.constant to constants), "init_data", *dirTokens.toTypedArray(), *constants.toTypedArray(), *commas.toTypedArray()) {
        val values = constants.map { it.getValue(dirType.deSize) }
        val bytesNeeded: Variable.Value
        var address: Variable.Value? = null

        init {
            var result = 0
            values.forEach { result += it.size.getByteCount() }
            bytesNeeded = Variable.Value.Hex(result.toString(16), memAddressWidth)
        }

        fun setAddress(address: Variable.Value) {
            this.address = address
        }
    }

    class EUnInitData(val dirType: StandardSyntax.DirType, val dirTokens: List<Compiler.Token>) : TreeNode.ElementNode(ConnectedHL(StandardFlags.directive to dirTokens), "uninit_data", *dirTokens.toTypedArray()) {
        var address: Variable.Value? = null

        fun setAddress(address: Variable.Value) {
            this.address = address
        }
    }

    class ESecStart(val dirType: StandardSyntax.DirType, val dirTokens: List<Compiler.Token>) : TreeNode.ElementNode(ConnectedHL(StandardFlags.directive to dirTokens), "sec_start", *dirTokens.toTypedArray())

    class EGlobal(val directive: Compiler.Token, space: Compiler.Token, val labelname: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(StandardFlags.directive to listOf(directive), StandardFlags.label to listOf(labelname)), "global", directive, space, labelname) {
        var linkedlabel: ELabel? = null

        fun link(labels: List<ELabel>): Boolean {
            linkedlabel = labels.firstOrNull { !it.spaceSub && it.nameString == labelname.content } ?: return false
            return true
        }
    }

    class ESetPC(symbols: List<Compiler.Token>, val constant: Compiler.Token.Constant) : TreeNode.ElementNode(ConnectedHL(StandardFlags.set_pc to symbols, StandardFlags.constant to listOf(constant)), "set_pc", *symbols.toTypedArray(), constant)

    class SText(secStart: ESecStart? = null, vararg val elements: ElementNode) : TreeNode.SectionNode("text", collNodes = if (secStart != null) elements + secStart else elements)
    class SData(secStart: ESecStart, vararg val elements: ElementNode) : TreeNode.SectionNode("data", secStart, *elements)
    class SRoData(secStart: ESecStart, vararg val elements: ElementNode) : TreeNode.SectionNode("rodata", secStart, *elements)
    class SBss(secStart: ESecStart, vararg val elements: ElementNode) : TreeNode.SectionNode("bss", secStart, *elements)

    class CSections(vararg val sections: SectionNode) : TreeNode.ContainerNode("sections", *sections)

}