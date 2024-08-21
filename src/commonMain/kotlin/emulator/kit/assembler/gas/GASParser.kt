package emulator.kit.assembler.gas

import cengine.util.integer.Size
import cengine.util.integer.Value
import debug.DebugTools
import emulator.core.*
import cengine.util.integer.Size.Bit16
import cengine.util.integer.*
import emulator.kit.assembler.*
import emulator.kit.assembler.gas.GASNode.*
import emulator.kit.assembler.lexer.Lexer
import emulator.kit.assembler.lexer.Severity
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Parser
import emulator.kit.assembler.parser.TreeResult
import emulator.kit.memory.Memory
import emulator.kit.nativeLog
import emulator.kit.optional.Feature

/**
 * The GASParser class is responsible for parsing GAS (GNU Assembler) syntax.
 * It extends the Parser class and overrides methods to handle directives and instructions specific to GAS.
 */
class GASParser(assembler: Assembler, private val asmHeader: AsmHeader) : Parser(assembler) {

    /**
     * Retrieves the list of directive types supported by the parser.
     *
     * @param features The list of features that may modify the supported directives.
     * @return A list of DirTypeInterface instances representing the supported directives.
     */
    override fun getDirs(features: List<Feature>): List<DirTypeInterface> = asmHeader.additionalDirectives() + GASDirType.entries

    /**
     * Retrieves the list of instruction types supported by the parser.
     *
     * @param features The list of features that may modify the supported instructions.
     * @return A list of InstrTypeInterface instances representing the supported instructions.
     */
    override fun getInstrs(features: List<Feature>): List<InstrTypeInterface> = asmHeader.instrTypes(features)

    /**
     * Parses the source tokens into a syntax tree.
     *
     * @param source The list of tokens to be parsed.
     * @param others Additional files to be considered during parsing.
     * @param features The list of features that may influence the parsing process.
     * @return A TreeResult instance containing the parsed tree and tokens.
     */
    override fun parseTree(source: List<Token>, others: List<AsmFile>, features: List<Feature>): TreeResult {
        // Preprocess and Filter Tokens
        val filteredSource = filter(source)

        if (DebugTools.KIT_showFilteredTokens) {
            nativeLog("Tokens: ${filteredSource.joinToString("") { "\n\t${it.lineLoc}:${it.type}:${it.content}" }}")
        }

        // Build the tree
        val root = try {
            GASNode.buildNode(GASNodeType.ROOT, filteredSource, getDirs(features), asmHeader)
        } catch (e: ParserError) {
            e.token.addSeverity(Severity.Type.ERROR, e.message)
            null
        }
        if (root == null || root !is GASNode.Root) return TreeResult(null, source, filteredSource)

        // Filter
        root.removeEmptyStatements()

        return TreeResult(root, source, filteredSource)
    }

    /**
     * Performs semantic analysis on the parsed syntax tree.
     *
     * @param lexer The lexer used for tokenization.
     * @param tree The parsed syntax tree.
     * @param others Additional files to be considered during analysis.
     * @param features The list of features that may influence the analysis.
     * @return A SemanticResult instance containing the analyzed sections.
     */
    override fun semanticAnalysis(lexer: Lexer, tree: TreeResult, others: List<AsmFile>, features: List<Feature>): SemanticResult {

        val root = tree.rootNode ?: return SemanticResult(arrayOf())

        /**
         * SEMANTIC ANALYSIS
         * - Resolve Directive Statements
         *   Iterate over statements if
         *   - Definition add to definitions
         *   - Resolve Unmatched Statements
         *
         * - Resolve Absolute Symbol Values
         *   Iterate over statements if
         *   - Directive is symbol defining -> add to local list of symbols (Valid Types: Undefined, String, Integer, Reg)
         *   - Directive is symbol setting -> change setted value and type
         *   - Statement contains symbol as Operand in Expression -> set Operand Value to symbol value (throw error if invalid type)
         *
         * - Fix Instruction ParamType and WordSizes
         *
         * - Calculate Relative Label Addresses
         */

        /**
         * - Resolve Statements
         */

        val tempContainer = TempContainer(asmHeader, assembler, others, features, root)

        try {
            while (root.getAllStatements().isNotEmpty()) {
                val firstStatement = root.getAllStatements().first()
                firstStatement.label?.let {
                    tempContainer.currSection.addContent(Label(it))
                }
                when (firstStatement) {
                    is GASNode.Statement.Dir -> {
                        firstStatement.dir.type.executeDirective(firstStatement, tempContainer)
                    }

                    is Statement.Empty -> {

                    }

                    is Statement.Instr -> {
                        asmHeader.parseInstrParams(firstStatement.rawInstr, tempContainer).forEach {
                            tempContainer.currSection.addContent(it)
                        }
                    }

                    is Statement.Unresolved -> {

                    }
                }

                root.removeChild(firstStatement)
            }

        } catch (e: ParserError) {
            e.token.addSeverity(Severity.Type.ERROR, e.message)
        }

        val sections = tempContainer.sections.toTypedArray()

        /**
         * Define Section Start Addresses
         */
        var currentAddress: Value = Hex("0", asmHeader.memAddrSize)
        sections.forEach {
            it.setSectionStart(currentAddress.toHex())
            currentAddress = it.getLastAddrOffset()
        }

        try {
            /**
             * Link All Section Labels
             */
            val allLinkedLabels = mutableListOf<Pair<Label, Hex>>()
            sections.forEach { sec ->
                allLinkedLabels.addAll(sec.linkLabels())
            }

            /**
             * Check Label Semantic
             */
            allLinkedLabels.checkLabelSemantic()

            /**
             * Generate Bytes
             */
            sections.forEach { sec ->
                sec.generateBytes(allLinkedLabels)
            }

        } catch (e: ParserError) {
            e.token.addSeverity(Severity.Type.ERROR, e.message)
        }

        if (DebugTools.KIT_showSections) {
            nativeLog(tempContainer.sections.joinToString("\n\n") {
                it.toString()
            })
        }

        return SemanticResult(sections)
    }

    /**
     * Checks the semantic validity of labels, ensuring no multiple definitions.
     */
    private fun List<Pair<Label, Hex>>.checkLabelSemantic() {
        this.forEach { lbl ->
            when (lbl.first.label.type) {
                GASNode.Label.Type.LOCAL -> {

                }

                GASNode.Label.Type.GLOBAL -> {
                    val multiDef = (this - lbl).firstOrNull { it.first.label.identifier == lbl.first.label.identifier }
                    if (multiDef != null) {
                        throw ParserError(lbl.first.label.tokens().first(), "Label is defined multiple times!")
                    }
                }
            }
        }
    }

    /**
     * Filters the [tokens] for only relevant ones for the parsing process.
     *
     * - Replaces comments with whitespaces
     *
     * @param tokens The list of tokens to filter.
     * @return A list of filtered tokens.
     */
    private fun filter(tokens: List<Token>): List<Token> {
        val remaining = tokens.toMutableList()
        if (remaining.lastOrNull()?.type != Token.Type.LINEBREAK) {
            remaining.lastOrNull()?.addSeverity(Severity.Type.WARNING, "File should end with a linebreak!")
        }
        val filtered = mutableListOf<Token>()

        while (remaining.isNotEmpty()) {
            // Add Base Node if not found any special node
            val replaceWithSpace = when (remaining.first().type) {
                Token.Type.COMMENT_NATIVE -> true
                Token.Type.COMMENT_SL -> true
                Token.Type.COMMENT_ML -> true
                else -> false
            }

            if (!replaceWithSpace) filtered.add(remaining.removeFirst()) else {
                val replacing = remaining.removeFirst()
                filtered.add(Token(Token.Type.WHITESPACE, replacing.lineLoc, " ", replacing.id))
            }
        }

        return filtered
    }

    /**
     * Temporary container class used during the parsing and semantic analysis processes.
     *
     * @property asmHeader The defined assembly.
     * @property assembler The compiler instance.
     * @property others Additional files to consider.
     * @property features The list of features that may influence the process.
     * @property root The root node of the syntax tree.
     * @property symbols The list of symbols.
     * @property sections The list of sections.
     * @property macros The list of macros.
     * @property currSection The current section being processed.
     */
    data class TempContainer(
        val asmHeader: AsmHeader,
        val assembler: Assembler,
        val others: List<AsmFile>,
        val features: List<Feature>,
        val root: Root,
        val symbols: MutableList<Symbol> = mutableListOf(),
        val sections: MutableList<Section> = mutableListOf(Section("text", asmHeader), Section("data", asmHeader), Section("bss", asmHeader)),
        val macros: MutableList<Macro> = mutableListOf(),
        var currSection: Section = sections.first(),
    ) {

        /**
         * Generates pseudo tokens for a given content.
         *
         * @param pseudoOf The reference token.
         * @param content The content to tokenize.
         * @return A list of generated tokens.
         */
        fun pseudoTokenize(pseudoOf: Token, content: String): List<Token> = assembler.lexer.pseudoTokenize(pseudoOf, content)

        /**
         * Parses a list of tokens into a list of statements.
         *
         * @param tokens The list of tokens to parse.
         * @return A list of parsed statements.
         */
        fun parse(tokens: List<Token>): List<Statement> {
            val tree = assembler.parser.parseTree(tokens, others, features)
            return tree.rootNode?.getAllStatements() ?: listOf()
        }

        /**
         * Switches to or appends a new section with the given name and flags.
         *
         * @param name The name of the section.
         * @param flags The flags for the section.
         */
        fun switchToOrAppendSec(name: String, flags: String = "") {
            val sec = sections.firstOrNull { it.name.lowercase() == name.lowercase() }
            if (sec != null) {
                currSection = sec
                if (flags.isNotEmpty()) sec.flags = flags
                return
            }
            val newSec = Section(name, asmHeader, flags)
            sections.add(newSec)
            currSection = newSec
        }

        /**
         * Imports a file for compilation.
         *
         * @param referenceToken The reference token.
         * @param name The name of the file to import.
         * @return True if the file was imported successfully, false otherwise.
         */
        fun importFile(referenceToken: Token, name: String): Boolean {
            val file = others.firstOrNull { it.mainRelativeName == name }
            if (file == null) {
                throw ParserError(referenceToken, "Couldn't find file with relative path $name!")
            }
            nativeLog("Importing: ${file.mainRelativeName} others: ${(others - file).joinToString { it.mainRelativeName }}")
            val tree = assembler.compile(file, others - file, Process.Mode.STOP_AFTER_TREE_HAS_BEEN_BUILD)
            tree.tokens.forEach {
                it.isPseudoOf = referenceToken
            }
            if (tree.hasErrors()) throw ParserError(referenceToken, "Include has not succeeded! File $name has errors!")
            val importedTree = tree.root ?: throw ParserError(referenceToken, "Include has not succeeded! File $name has errors!")
            root.addChilds(1, importedTree.getAllStatements())
            return true
        }

        /**
         * Sets or replaces a symbol in the symbol list.
         *
         * @param symbol The symbol to set or replace.
         */
        fun setOrReplaceSymbol(symbol: Symbol) {
            val alreadydefined = symbols.firstOrNull { it.name == symbol.name }
            symbols.remove(alreadydefined)
            symbols.add(symbol)
        }

        /**
         * Sets or replaces a descriptor for a symbol.
         *
         * @param symbolName The name of the symbol.
         * @param descriptor The descriptor to set.
         */
        fun setOrReplaceDescriptor(symbolName: String, descriptor: Hex) {
            val alreadydefined = symbols.firstOrNull { it.name == symbolName }
            if (alreadydefined != null) {
                alreadydefined.descriptor = descriptor
                return
            }
            val newSymbol = Symbol.Undefined(symbolName)
            newSymbol.descriptor = descriptor
            symbols.add(newSymbol)
        }
    }

    /**
     * Represents a macro with a name, arguments, and content.
     *
     * @property name The name of the macro.
     * @property arguments The list of arguments for the macro.
     * @property content The content of the macro.
     */
    data class Macro(val name: String, val arguments: List<Argument>, val content: List<Statement>) {
        init {
            content.forEach { stmnt ->
                stmnt.tokens().forEach {
                    it.removeSeverityIfError()
                }
            }
        }

        /**
         * Generates pseudo statements by replacing arguments in the content.
         *
         * @param argMap The list of argument definitions.
         * @return The content with arguments replaced.
         */
        fun generatePseudoStatements(argMap: List<ArgDef>): String {
            var content = content.map { it.contentBackToString() }.joinToString("") { it }
            val args = arguments.map { it.argName to it.getDefaultValue() }.toTypedArray()

            // Check for mixture of positional and indexed arguments
            argMap.forEachIndexed { index, def ->
                when (def) {
                    is ArgDef.Named -> {
                        val argID = args.indexOfFirst { it.first.content == def.name.content }
                        if (argID == -1) throw ParserError(def.name, "${def.name.content} is not a defined attribute name!")
                        args[argID] = args[argID].first to def.content.joinToString("") { it.content }
                    }

                    is ArgDef.Positional -> {
                        if (index >= args.size) {
                            def.tokens().firstOrNull()?.let {
                                throw ParserError(it, "Macro $name has no argument expected at position $index!")
                            }
                            return@forEachIndexed
                        }
                        args[index] = args[index].first to def.content.joinToString("") { it.content }
                    }
                }
            }

            args.forEach {
                content = content.replace("\\${it.first}", it.second)
            }

            content = content.replace("\\()", "")

            return content + "\n"
        }
    }

    /**
     * Sealed class representing a symbol with a name and descriptor.
     *
     * @property name The name of the symbol.
     */
    sealed class Symbol(val name: String) {
        var descriptor: Hex = Hex("0", Bit16)

        class Undefined(name: String) : Symbol(name)
        class StringExpr(name: String, val expr: GASNode.StringExpr) : Symbol(name)
        class IntegerExpr(name: String, val expr: NumericExpr) : Symbol(name)
        class TokenRef(name: String, val token: Token) : Symbol(name)
    }

    /**
     * Represents a section in the assembly with a name, address size, and flags.
     *
     * @property name The name of the section.
     * @property addressSize The size of the address space.
     * @property flags The flags for the section.
     */
    data class Section(val name: String, val asmHeader: AsmHeader, var flags: String = "") {
        private val content: MutableList<MappedContent<*>> = mutableListOf()
        private var lastOffset: Value = Hex("0", asmHeader.memAddrSize)
        private var sectionStart: Hex = Hex("0", asmHeader.memAddrSize)

        /**
         * Adds content to the section.
         *
         * @param sectionContent The content to add.
         */
        fun addContent(sectionContent: SecContent) {
            content.add(MappedContent(lastOffset.toHex(), sectionContent))
            lastOffset += Hex(sectionContent.bytesNeeded.toString(16), asmHeader.memAddrSize)
        }

        fun setSectionStart(sectionStartAddress: Hex) {
            sectionStart = sectionStartAddress
        }

        /**
         * Gets the last address in the section.
         *
         * @return The last address as a Value.
         */
        fun getLastAddrOffset(): Value {
            return lastOffset.toBin().ushr(asmHeader.addrShift).toHex()
        }

        /**
         * Links labels to their addresses within the section.
         *
         * @param sectionStartAddress The starting address of the section.
         * @return A list of label-address pairs.
         */
        fun linkLabels(): List<Pair<Label, Hex>> {
            return content.filter { it.content is Label }.filterIsInstance<MappedContent<Label>>().map {
                it.content to (sectionStart + it.addrOffset()).toHex()
            }
        }

        /**
         * Generates bytes for the section content.
         *
         * @param sectionStartAddress The starting address of the section.
         * @param labels The list of label-address pairs.
         */
        fun generateBytes(labels: List<Pair<Label, Hex>>) {
            content.forEach {
                it.bytes = it.content.getBinaryArray(sectionStart + it.addrOffset(), labels)
            }
        }

        /**
         * Gets the starting address of the section.
         *
         * @return The starting address as a Hex value.
         */
        fun getSectionAddr() = sectionStart

        /**
         * Gets the content of the section.
         *
         * @return An array of MappedContent instances.
         */
        fun getContent() = content.toTypedArray()

        /**
         * Gets the bundled content of the section.
         *
         * @return An array of BundledContent instances.
         */
        fun getTSContent(): Array<BundledContent> {
            val labels = content.filter { it.content is Label }

            return content.filter { it.content !is Label }.map { mapped ->
                BundledContent((sectionStart + mapped.addrOffset()).toHex(), mapped.bytes, mapped.content.getContentString(), labels.filter { lbl -> lbl.addrOffset() == mapped.addrOffset() }.map { it.content }.filterIsInstance<Label>())
            }.toTypedArray()
        }

        override fun toString(): String = "$name: ${content.joinToString("") { "\n\t$it" }}"

        fun store(memory: Memory): Map<String, List<Token.LineLoc>> {
            val lineAddressMap = mutableMapOf<String, List<Token.LineLoc>>()
            if (asmHeader.addrShift == 0) {
                val secAddr = getSectionAddr()
                getContent().forEach {
                    val addr = (secAddr + it.addrOffset()).toHex()
                    lineAddressMap[addr.toHex().toRawString()] = it.content.allTokensIncludingPseudo().map { token -> token.lineLoc }
                    memory.storeArray(addr, *it.bytes, mark = it.content.getMark())
                }
            } else {
                val groups = content.groupBy {
                    it.addrOffset()
                }

                groups.forEach { group ->
                    val mark = group.value.firstOrNull()?.content?.getMark()
                    val addr = sectionStart + group.key
                    lineAddressMap[addr.toHex().toRawString()] = group.value.flatMap { it.content.allTokensIncludingPseudo().map { token -> token.lineLoc } }

                    val bytes = group.value.flatMap { content ->
                        content.bytes.flatMap {
                            when (memory.globalEndianess()) {
                                Memory.Endianess.LittleEndian -> it.splitToByteArray().reversed().toList()
                                Memory.Endianess.BigEndian -> it.splitToByteArray().toList()
                            }
                        }
                    }.toTypedArray()
                    val words = bytes.mergeToChunks(Size.Bit8, memory.instanceSize)

                    memory.storeArray(addr.toHex(), *words, mark = mark ?: Memory.InstanceType.DATA)
                }
            }

            return lineAddressMap
        }

        /**
         * Data class mapping content to an offset.
         *
         * @property byteOffset The offset of the content.
         * @property content The content.
         */
        inner class MappedContent<T : SecContent>(private val byteOffset: Hex, val content: T) {
            var bytes: Array<Bin> = arrayOf()
            fun addrOffset(): Hex = byteOffset.toBin().ushr(asmHeader.addrShift).toHex()
            override fun toString(): String = "${if (content.bytesNeeded != 0) addrOffset().toRawZeroTrimmedString() else ""}${if (bytes.isNotEmpty()) "\t" + bytes.joinToString("") { it.toHex().toRawString() } + "\t" else ""}${content.getContentString()}"
        }

        /**
         * Data class representing bundled content with an address, bytes, content, and labels.
         *
         * @property address The address of the content.
         * @property bytes The bytes of the content.
         * @property content The content as a string.
         * @property label The list of labels associated with the content.
         */
        data class BundledContent(val address: Hex, val bytes: Array<Bin>, val content: String, val label: List<Label>) {
            fun getAddrLblBytesTranscript(): Array<String> = arrayOf(address.toRawZeroTrimmedString(), label.joinToString("\n") { it.getContentString() }, bytes.joinToString("\n") { it.toHex().toRawString() }, content)
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is BundledContent) return false

                if (address != other.address) return false
                if (!bytes.contentEquals(other.bytes)) return false
                if (content != other.content) return false
                if (label != other.label) return false

                return true
            }

            override fun hashCode(): Int {
                var result = address.hashCode()
                result = 31 * result + bytes.contentHashCode()
                result = 31 * result + content.hashCode()
                result = 31 * result + label.hashCode()
                return result
            }
        }
    }

    /**
     * Interface for section content.
     */
    interface SecContent {
        val bytesNeeded: Int
        fun getFirstToken(): Token
        fun allTokensIncludingPseudo(): List<Token>
        fun getMark(): Memory.InstanceType

        /**
         * Generates a binary array for the section content.
         * This will be stored into memory!
         *
         * Values larger than one byte will be stored memory endianness dependent!
         *
         * @param yourAddr The address of the content.
         * @param labels The list of label-address pairs.
         * @return An array of binary values.
         */
        fun getBinaryArray(yourAddr: Value, labels: List<Pair<Label, Hex>>): Array<Bin>
        fun getContentString(): String
    }

    /**
     * Class representing data content in a section.
     *
     * @property referenceToken The reference token.
     * @property bin The binary data.
     * @property type The data type.
     */
    class Data(private val referenceToken: Token, val bin: Hex, val type: DataType) : SecContent {
        override val bytesNeeded: Int = bin.size.getByteCount()

        override fun getFirstToken(): Token = referenceToken
        override fun allTokensIncludingPseudo(): List<Token> = listOfNotNull(referenceToken, referenceToken.isPseudoOf)

        override fun getMark(): Memory.InstanceType = Memory.InstanceType.DATA

        override fun getBinaryArray(yourAddr: Value, labels: List<Pair<Label, Hex>>): Array<Bin> = arrayOf(bin.toBin())

        override fun getContentString(): String = ".${type.name.lowercase()} $bin"

        /**
         * Enum representing the types of data.
         */
        enum class DataType {
            BYTE,
            SHORT,
            WORD,
            DWORD
        }
    }

    /**
     * Data class representing a label in the assembly.
     *
     * @property label The label node.
     */
    data class Label(val label: GASNode.Label) : SecContent {
        fun getID(): String = label.identifier

        override val bytesNeeded: Int = 0
        override fun getMark(): Memory.InstanceType = Memory.InstanceType.PROGRAM
        override fun getFirstToken(): Token = label.tokens().first()
        override fun allTokensIncludingPseudo(): List<Token> = label.tokensIncludingReferences().toList()
        override fun getBinaryArray(yourAddr: Value, labels: List<Pair<Label, Hex>>): Array<Bin> = emptyArray()
        override fun getContentString(): String = label.identifier + ":"
    }

}