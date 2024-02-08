package emulator.archs.ikrmini

import emulator.kit.Architecture
import emulator.kit.assembly.Compiler
import emulator.kit.assembly.Syntax
import emulator.kit.common.FileHandler
import emulator.kit.common.Transcript
import emulator.kit.assembly.Syntax.TokenSeq.Component.*
import emulator.kit.assembly.Syntax.TokenSeq.Component.InSpecific.*
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*
import emulator.kit.types.Variable.Size.*
import emulator.archs.ikrmini.IKRMini.WORDSIZE

class IKRMiniSyntax : Syntax() {
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

        return SyntaxTree(TreeNode.RootNode(errors, warnings, TreeNode.ContainerNode("pre", *preElements.toTypedArray()), IKRMiniSyntax.CSections(*sections.toTypedArray())))
    }

    /**
     * Removes all comments from the compiler tokens and adds them to the preElements
     */
    private fun MutableList<Compiler.Token>.removeComments(preElements: MutableList<TreeNode.ElementNode>): MutableList<Compiler.Token> {
        while (true) {
            val commentStart = this.firstOrNull { it.content == ";" } ?: break
            val startIndex = this.indexOf(commentStart)
            val commentEnd = this.firstOrNull { it is Compiler.Token.NewLine && this.indexOf(commentStart) < this.indexOf(it) }
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
    private fun MutableList<Compiler.Token>.checkInstr(elements: MutableList<TreeNode.ElementNode>, errors: MutableList<Error>, warnings: MutableList<Warning>, currentLabel: ELabel?): Boolean {
        for (amode in ParamType.entries) {
            val amodeResult = amode.tokenSeq.matchStart(*this.toTypedArray())

            if (amodeResult.matches) {
                val type = InstrType.entries.firstOrNull { it.name.uppercase() == amodeResult.sequenceMap[0].token.content.uppercase() } ?: return false

                val imm = amodeResult.sequenceMap.map { it.token }.firstOrNull { it is Compiler.Token.Constant } as Compiler.Token.Constant?

                val eInstr = EInstr(type, amode, imm, currentLabel, *amodeResult.sequenceMap.map { it.token }.toTypedArray())

                this.removeAll(eInstr.tokens.toSet())
                elements.add(eInstr)
                return true
            }
        }
        return false
    }

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
            val valid = instr.link(labels, errors)
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
     * Mnemonics and directives
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

    enum class ParamType(val tokenSeq: TokenSeq, val exampleString: String) {
        INDIRECT(TokenSeq(Word, Space, Specific("("), Specific("("), SpecConst(Bit16()), Specific(")"), Specific(")"), NewLine, ignoreSpaces = true), "(([16 Bit]))"),
        DIRECT(TokenSeq(Word, Space, Specific("("), SpecConst(Bit16()), Specific(")"), NewLine, ignoreSpaces = true), "([16 Bit])"),
        IMMEDIATE(TokenSeq(Word, Space, Specific("#"), SpecConst(Bit16()), NewLine, ignoreSpaces = true), "#[16 Bit]"),
        DESTINATION(TokenSeq(Word, Space, Word, NewLine, ignoreSpaces = true), "[label]"),
        IMPLIED(TokenSeq(Word, NewLine, ignoreSpaces = true), ""),
    }

    enum class InstrType(val paramMap: Map<ParamType, Hex>, val descr: String) {
        // Data Transport
        LOAD(mapOf(ParamType.IMMEDIATE to Hex("010C", WORDSIZE), ParamType.DIRECT to Hex("020C", WORDSIZE), ParamType.INDIRECT to Hex("030C", WORDSIZE)), "load AC"),
        LOADI(mapOf(ParamType.IMPLIED to Hex("200C", WORDSIZE)), "load indirect"),
        STORE(mapOf(ParamType.DIRECT to Hex("3200", WORDSIZE), ParamType.INDIRECT to Hex("3300", WORDSIZE)), "store AC at address"),

        // Data Manipulation
        AND(mapOf(ParamType.IMMEDIATE to Hex("018A", WORDSIZE), ParamType.DIRECT to Hex("028A", WORDSIZE), ParamType.INDIRECT to Hex("038A", WORDSIZE)), "and (logic)"),
        OR(mapOf(ParamType.IMMEDIATE to Hex("0188", WORDSIZE), ParamType.DIRECT to Hex("0288", WORDSIZE), ParamType.INDIRECT to Hex("0388", WORDSIZE)), "or (logic)"),
        XOR(mapOf(ParamType.IMMEDIATE to Hex("0189", WORDSIZE), ParamType.DIRECT to Hex("0289", WORDSIZE), ParamType.INDIRECT to Hex("0389", WORDSIZE)), "xor (logic)"),
        ADD(mapOf(ParamType.IMMEDIATE to Hex("018D", WORDSIZE), ParamType.DIRECT to Hex("028D", WORDSIZE), ParamType.INDIRECT to Hex("038D", WORDSIZE)), "add"),
        ADDC(mapOf(ParamType.IMMEDIATE to Hex("01AD", WORDSIZE), ParamType.DIRECT to Hex("02AD", WORDSIZE), ParamType.INDIRECT to Hex("03AD", WORDSIZE)), "add with carry"),
        SUB(mapOf(ParamType.IMMEDIATE to Hex("018E", WORDSIZE), ParamType.DIRECT to Hex("028E", WORDSIZE), ParamType.INDIRECT to Hex("038E", WORDSIZE)), "sub"),
        SUBC(mapOf(ParamType.IMMEDIATE to Hex("01AE", WORDSIZE), ParamType.DIRECT to Hex("02AE", WORDSIZE), ParamType.INDIRECT to Hex("03AE", WORDSIZE)), "sub with carry"),

        LSL(mapOf(ParamType.IMPLIED to Hex("00A0", WORDSIZE), ParamType.DIRECT to Hex("0220", WORDSIZE), ParamType.INDIRECT to Hex("0320", WORDSIZE)), "logic shift left"),
        LSR(mapOf(ParamType.IMPLIED to Hex("00A1", WORDSIZE), ParamType.DIRECT to Hex("0221", WORDSIZE), ParamType.INDIRECT to Hex("0321", WORDSIZE)), "logic shift right"),
        ROL(mapOf(ParamType.IMPLIED to Hex("00A2", WORDSIZE), ParamType.DIRECT to Hex("0222", WORDSIZE), ParamType.INDIRECT to Hex("0322", WORDSIZE)), "rotate left"),
        ROR(mapOf(ParamType.IMPLIED to Hex("00A3", WORDSIZE), ParamType.DIRECT to Hex("0223", WORDSIZE), ParamType.INDIRECT to Hex("0323", WORDSIZE)), "rotate right"),
        ASL(mapOf(ParamType.IMPLIED to Hex("00A4", WORDSIZE), ParamType.DIRECT to Hex("0224", WORDSIZE), ParamType.INDIRECT to Hex("0324", WORDSIZE)), "arithmetic shift left"),
        ASR(mapOf(ParamType.IMPLIED to Hex("00A5", WORDSIZE), ParamType.DIRECT to Hex("0225", WORDSIZE), ParamType.INDIRECT to Hex("0325", WORDSIZE)), "arithmetic shift right"),

        RCL(mapOf(ParamType.IMPLIED to Hex("00A6", WORDSIZE), ParamType.IMMEDIATE to Hex("0126", WORDSIZE), ParamType.DIRECT to Hex("0226", WORDSIZE), ParamType.INDIRECT to Hex("0326", WORDSIZE)), "rotate left with carry"),
        RCR(mapOf(ParamType.IMPLIED to Hex("00A7", WORDSIZE), ParamType.IMMEDIATE to Hex("0127", WORDSIZE), ParamType.DIRECT to Hex("0227", WORDSIZE), ParamType.INDIRECT to Hex("0327", WORDSIZE)), "rotate right with carry"),
        NOT(mapOf(ParamType.IMPLIED to Hex("008B", WORDSIZE), ParamType.DIRECT to Hex("020B", WORDSIZE), ParamType.INDIRECT to Hex("030B", WORDSIZE)), "invert (logic not)"),

        NEG(mapOf(ParamType.DIRECT to Hex("024E", WORDSIZE), ParamType.INDIRECT to Hex("034E", WORDSIZE)), "negotiate"),

        CLR(mapOf(ParamType.IMPLIED to Hex("004C", WORDSIZE)), "clear"),

        INC(mapOf(ParamType.IMPLIED to Hex("009C", WORDSIZE), ParamType.DIRECT to Hex("021C", WORDSIZE), ParamType.INDIRECT to Hex("031C", WORDSIZE)), "increment (+1)"),
        DEC(mapOf(ParamType.IMPLIED to Hex("009F", WORDSIZE), ParamType.DIRECT to Hex("021F", WORDSIZE), ParamType.INDIRECT to Hex("031F", WORDSIZE)), "decrement (-1)"),

        // Unconditional Branches
        BSR(mapOf(ParamType.DESTINATION to Hex("510C", WORDSIZE)), "branch and save return address in AC"),
        JMP(mapOf(ParamType.IMPLIED to Hex("4000", WORDSIZE)), "jump to address in AC"),
        BRA(mapOf(ParamType.DESTINATION to Hex("6101", WORDSIZE)), "branch"),

        // Conditional Branches
        BHI(mapOf(ParamType.DESTINATION to Hex("6102", WORDSIZE)), "branch if higher"),
        BLS(mapOf(ParamType.DESTINATION to Hex("6103", WORDSIZE)), "branch if lower or same"),
        BCC(mapOf(ParamType.DESTINATION to Hex("6104", WORDSIZE)), "branch if carry clear"),
        BCS(mapOf(ParamType.DESTINATION to Hex("6105", WORDSIZE)), "branch if carry set"),
        BNE(mapOf(ParamType.DESTINATION to Hex("6106", WORDSIZE)), "branch if not equal"),
        BEQ(mapOf(ParamType.DESTINATION to Hex("6107", WORDSIZE)), "branch if equal"),
        BVC(mapOf(ParamType.DESTINATION to Hex("6108", WORDSIZE)), "branch if overflow clear"),
        BVS(mapOf(ParamType.DESTINATION to Hex("6109", WORDSIZE)), "branch if overflow set"),
        BPL(mapOf(ParamType.DESTINATION to Hex("610A", WORDSIZE)), "branch if positive"),
        BMI(mapOf(ParamType.DESTINATION to Hex("610B", WORDSIZE)), "branch if negative"),
        BGE(mapOf(ParamType.DESTINATION to Hex("610C", WORDSIZE)), "branch if greater or equal"),
        BLT(mapOf(ParamType.DESTINATION to Hex("610D", WORDSIZE)), "branch if less than"),
        BGT(mapOf(ParamType.DESTINATION to Hex("610E", WORDSIZE)), "branch if greater than"),
        BLE(mapOf(ParamType.DESTINATION to Hex("610F", WORDSIZE)), "branch if less or equal");

    }

    /**
     * SYNTAX
     */
    object Seqs {
        val SeqAfterEquDir = TokenSeq(WordNoDots, Specific(","), Constant, ignoreSpaces = true)
        val SeqMacroAttrInsert = TokenSeq(Specific("""\"""), WordNoDots)
        val SeqLabel = TokenSeq(Word, Specific(":"))
        val SeqImport = TokenSeq(Specific("#"), Specific("import"), Space, StringConst)
        val SeqSetPC = TokenSeq(Specific("*"), Specific("="), SpecConst(WORDSIZE))
    }

    /**
     * TREE NODES
     */

    class PREComment(vararg tokens: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(IKRMiniFlags.comment), "comment", *tokens)
    class PreEquDef(directive: List<Compiler.Token>, val equname: Compiler.Token.Word, comma: Compiler.Token.Symbol, val constant: Compiler.Token.Constant) : TreeNode.ElementNode(
        ConnectedHL(IKRMiniFlags.directive to directive, IKRMiniFlags.pre_equ to listOf(equname), IKRMiniFlags.constant to listOf(constant), IKRMiniFlags.pre_equ to listOf(comma)),
        "equ_def",
        *directive.toTypedArray(),
        equname,
        comma,
        constant
    )

    class PreEquRep(token: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(IKRMiniFlags.pre_equ), "equ_insert", token)
    class PreMacroDef(directives: List<Compiler.Token>, val macroname: Compiler.Token.Word, val attributes: List<Compiler.Token.Word>, commas: List<Compiler.Token>, val macroContent: List<Compiler.Token>) : TreeNode.ElementNode(
        ConnectedHL(
            IKRMiniFlags.directive to directives,
            IKRMiniFlags.pre_macro to listOf(macroname, *attributes.toTypedArray(), *macroContent.filterNot { it is Compiler.Token.Constant || it is Compiler.Token.Register }.toTypedArray(), *commas.toTypedArray()),
            IKRMiniFlags.constant to macroContent.filterIsInstance<Compiler.Token.Constant>()
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
            IKRMiniFlags.pre_macro to commas + macroName,
            IKRMiniFlags.constant to attributes.filterIsInstance<Compiler.Token.Constant>(),
            IKRMiniFlags.label to attributes.filterIsInstance<Compiler.Token.Word>()
        ),
        "macro_insert",
        macroName,
        *attributes.toTypedArray(),
        *commas.toTypedArray()
    )

    class PreImport(symbol: Compiler.Token, word: Compiler.Token, space: Compiler.Token, filename: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(IKRMiniFlags.pre_import to listOf(symbol, word), IKRMiniFlags.constant to listOf(filename)), "import", symbol, word, space, filename)

    class EInstr(val type: InstrType, val paramType: ParamType, val imm: Compiler.Token.Constant?, val parentLabel: ELabel?, vararg tokens: Compiler.Token) : TreeNode.ElementNode(IKRMiniFlags.getInstrHL(*tokens), "instr", *tokens) {

        val unlinkedlabels = tokens.drop(1).filterIsInstance<Compiler.Token.Word>()
        val linkedLabels = mutableListOf<ELabel>()
        var address: Variable.Value? = null

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

    class ELabel(currentLabel: ELabel? = null, nameToken: Compiler.Token, endSymbol: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(IKRMiniFlags.label), "label", *listOfNotNull(nameToken, endSymbol).toTypedArray()) {
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

    class EInitData(val dirType: DirType, val dirTokens: List<Compiler.Token>, val constants: List<Compiler.Token.Constant>, commas: List<Compiler.Token>) :
        TreeNode.ElementNode(ConnectedHL(IKRMiniFlags.directive to dirTokens, IKRMiniFlags.constant to constants), "init_data", *dirTokens.toTypedArray(), *constants.toTypedArray(), *commas.toTypedArray()) {
        val values = constants.map { it.getValue(dirType.deSize) }
        val bytesNeeded: Variable.Value
        var address: Variable.Value? = null

        init {
            var result = 0
            values.forEach { result += it.size.getByteCount() }
            bytesNeeded = Hex(result.toString(16), IKRMini.MEM_ADDRESS_WIDTH)
        }

        fun setAddress(address: Variable.Value) {
            this.address = address
        }
    }

    class EUnInitData(val dirType: DirType, val dirTokens: List<Compiler.Token>) : TreeNode.ElementNode(ConnectedHL(IKRMiniFlags.directive to dirTokens), "uninit_data", *dirTokens.toTypedArray()) {
        var address: Variable.Value? = null

        fun setAddress(address: Variable.Value) {
            this.address = address
        }
    }

    class ESecStart(val dirType: DirType, val dirTokens: List<Compiler.Token>) : TreeNode.ElementNode(ConnectedHL(IKRMiniFlags.directive to dirTokens), "sec_start", *dirTokens.toTypedArray())

    class EGlobal(val directive: Compiler.Token, space: Compiler.Token, val labelname: Compiler.Token) : TreeNode.ElementNode(ConnectedHL(IKRMiniFlags.directive to listOf(directive), IKRMiniFlags.label to listOf(labelname)), "global", directive, space, labelname) {
        var linkedlabel: ELabel? = null

        fun link(labels: List<ELabel>): Boolean {
            linkedlabel = labels.firstOrNull { !it.spaceSub && it.nameString == labelname.content } ?: return false
            return true
        }
    }

    class ESetPC(symbols: List<Compiler.Token>, val constant: Compiler.Token.Constant) : TreeNode.ElementNode(ConnectedHL(IKRMiniFlags.set_pc to symbols, IKRMiniFlags.constant to listOf(constant)), "set_pc", *symbols.toTypedArray(), constant)

    class SText(secStart: ESecStart? = null, vararg val elements: ElementNode) : TreeNode.SectionNode("text", collNodes = if (secStart != null) elements + secStart else elements)
    class SData(secStart: ESecStart, vararg val elements: ElementNode) : TreeNode.SectionNode("data", secStart, *elements)
    class SRoData(secStart: ESecStart, vararg val elements: ElementNode) : TreeNode.SectionNode("rodata", secStart, *elements)
    class SBss(secStart: ESecStart, vararg val elements: ElementNode) : TreeNode.SectionNode("bss", secStart, *elements)

    class CSections(vararg val sections: SectionNode) : TreeNode.ContainerNode("sections", *sections)

}