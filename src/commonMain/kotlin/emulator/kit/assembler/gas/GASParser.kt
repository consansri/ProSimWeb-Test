package emulator.kit.assembler.gas

import debug.DebugTools
import emulator.kit.assembler.*
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.gas.nodes.GASNode.*
import emulator.kit.assembler.gas.nodes.GASNodeType
import emulator.kit.assembler.lexer.Lexer
import emulator.kit.assembler.lexer.Severity
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Parser
import emulator.kit.assembler.parser.TreeResult
import emulator.kit.common.Memory
import emulator.kit.nativeError
import emulator.kit.nativeLog
import emulator.kit.optional.Feature
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*
import emulator.kit.types.Variable.Size.*
import emulator.kit.types.Variable.Value

class GASParser(compiler: CompilerInterface, val definedAssembly: DefinedAssembly) : Parser(compiler) {
    override fun getDirs(features: List<Feature>): List<DirTypeInterface> = definedAssembly.getAdditionalDirectives() + GASDirType.entries
    override fun getInstrs(features: List<Feature>): List<InstrTypeInterface> = definedAssembly.getInstrs(features)
    override fun parseTree(source: List<Token>, others: List<CompilerFile>, features: List<Feature>): TreeResult {
        // Preprocess and Filter Tokens
        val filteredSource = filter(source)

        // Build the tree
        val root = try {
            GASNode.buildNode(GASNodeType.ROOT, filteredSource, getDirs(features), definedAssembly)
        } catch (e: ParserError) {
            e.token.addSeverity(Severity.Type.ERROR, e.message)
            nativeError(e.message)
            null
        }
        if (root == null || root !is Root) return TreeResult(null, source, filteredSource)

        // Filter
        root.removeEmptyStatements()

        return TreeResult(root, source, filteredSource)
    }

    override fun semanticAnalysis(lexer: Lexer, tree: TreeResult, others: List<CompilerFile>, features: List<Feature>): SemanticResult {

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

        val tempContainer = TempContainer(definedAssembly, compiler, others, features, root)

        try {
            while (root.getAllStatements().isNotEmpty()) {
                val firstStatement = root.getAllStatements().first()
                firstStatement.label?.let {
                    tempContainer.currSection.addContent(Label(it))
                }
                when (firstStatement) {
                    is Statement.Dir -> {
                        firstStatement.dir.type.executeDirective(firstStatement, tempContainer)
                    }

                    is Statement.Empty -> {

                    }

                    is Statement.Instr -> {
                        definedAssembly.parseInstrParams(firstStatement.rawInstr, tempContainer).forEach {
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
            nativeError(e.message)
        }

        val sections = tempContainer.sections.toTypedArray()

        /**
         * Define Section Start Addresses
         */
        var currentAddress: Value = Hex("0", definedAssembly.MEM_ADDRESS_SIZE)
        val sectionAddressMap = mutableMapOf<Section, Hex>()
        sections.forEach {
            sectionAddressMap.set(it, currentAddress.toHex())
            currentAddress += it.lastOffset
        }

        /**
         * Link All Section Labels
         */
        val allLinkedLabels = mutableListOf<Pair<Label, Hex>>()
        sections.forEach { sec ->
            val addr = sectionAddressMap[sec]
            addr?.let {
                allLinkedLabels.addAll(sec.linkLabels(it))
            }
        }

        try {
            /**
             * Generate Bytes
             */
            sections.forEach { sec ->
                val addr = sectionAddressMap[sec]
                addr?.let {
                    sec.generateBytes(it, allLinkedLabels)
                }
            }
        } catch (e: ParserError) {
            e.token.addSeverity(Severity.Type.ERROR, e.message)
            nativeError(e.message)
        }

        nativeLog(tempContainer.sections.joinToString("\n\n") {
            it.toString()
        })

        return SemanticResult(sections)
    }

    /**
     * Filter the [tokens] and Build up the List of Matching [GASElement]s
     */
    private fun filter(tokens: List<Token>): List<Token> {
        val remaining = tokens.toMutableList()
        if (remaining.lastOrNull()?.type != Token.Type.LINEBREAK) {
            remaining.lastOrNull()?.addSeverity(Severity.Type.WARNING, "File should end with a linebreak!")
        }
        val elements = mutableListOf<Token>()

        while (remaining.isNotEmpty()) {
            // Add Base Node if not found any special node
            val replaceWithSpace = when (remaining.first().type) {
                Token.Type.COMMENT_NATIVE -> true
                Token.Type.COMMENT_SL -> true
                Token.Type.COMMENT_ML -> true
                else -> false
            }

            if (!replaceWithSpace) elements.add(remaining.removeFirst()) else {
                val replacing = remaining.removeFirst()
                elements.add(Token(Token.Type.WHITESPACE, replacing.lineLoc, " ", replacing.id))
            }
        }

        // Remove Spaces between DIRECTIVE and LINEBREAK

        return elements
    }

    data class TempContainer(
        val definedAssembly: DefinedAssembly,
        val compiler: CompilerInterface,
        val others: List<CompilerFile>,
        val features: List<Feature>,
        val root: Root,
        val symbols: MutableList<Symbol> = mutableListOf(),
        val sections: MutableList<Section> = mutableListOf(Section("text", definedAssembly.MEM_ADDRESS_SIZE), Section("data", definedAssembly.MEM_ADDRESS_SIZE), Section("bss", definedAssembly.MEM_ADDRESS_SIZE)),
        val macros: MutableList<Macro> = mutableListOf(),
        var currSection: Section = sections.first(),
    ) {

        fun pseudoTokenize(pseudoOf: Token, content: String): List<Token> = compiler.lexer.pseudoTokenize(pseudoOf, content)

        fun parse(tokens: List<Token>): List<GASNode.Statement> {
            val tree = compiler.parser.parseTree(tokens, others, features)
            return tree.rootNode?.getAllStatements() ?: listOf()
        }

        fun switchToOrAppendSec(name: String, flags: String = "") {
            val sec = sections.firstOrNull { it.name.lowercase() == name.lowercase() }
            if (sec != null) {
                currSection = sec
                if (flags.isNotEmpty()) sec.flags = flags
                return
            }
            val newSec = Section(name, definedAssembly.MEM_ADDRESS_SIZE, flags)
            sections.add(newSec)
            currSection = newSec
        }

        fun importFile(referenceToken: Token, name: String): Boolean {
            val file = others.firstOrNull { it.name == name } ?: return false
            nativeLog("Importing: ${file.name} others: ${(others - file).joinToString { it.name }}")
            val tree = compiler.compile(file, others - file, Process.Mode.STOP_AFTER_TREE_HAS_BEEN_BUILD)
            tree.tokens.forEach {
                it.isPseudoOf = referenceToken
            }
            if (tree.hasErrors()) throw ParserError(referenceToken, "Include has not succeeded! File $name has errors!")
            val importedTree = tree.root ?: throw ParserError(referenceToken, "Include has not succeeded! File $name has errors!")
            root.addChilds(1, importedTree.getAllStatements())
            return true
        }

        fun setOrReplaceSymbol(symbol: Symbol) {
            val alreadydefined = symbols.firstOrNull { it.name == symbol.name }
            symbols.remove(alreadydefined)
            symbols.add(symbol)
        }

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

    data class Macro(val name: String, val arguments: List<Argument>, val content: List<Statement>) {
        init {
            content.forEach { stmnt ->
                stmnt.getAllTokens().forEach {
                    it.removeSeverityIfError()
                }
            }
        }

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
                            def.getAllTokens().firstOrNull()?.let {
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

    sealed class Symbol(val name: String) {
        var descriptor: Hex = Hex("0", Bit16())

        class Undefined(name: String) : Symbol(name)
        class StringExpr(name: String, val expr: GASNode.StringExpr) : Symbol(name)
        class IntegerExpr(name: String, val expr: NumericExpr) : Symbol(name)
        class TokenRef(name: String, val token: Token) : Symbol(name)
    }

    data class Section(val name: String, val addressSize: Variable.Size, var flags: String = "") {
        private val content: MutableList<MappedContent<*>> = mutableListOf()
        var lastOffset: Value = Hex("0", addressSize)

        private var sectionStart: Hex = Hex("0", addressSize)

        fun addContent(sectionContent: SecContent) {
            content.add(MappedContent(lastOffset.toHex(), sectionContent))
            lastOffset += Hex(sectionContent.bytesNeeded.toString(16), addressSize)
        }

        fun getLastAddress(addressSize: Variable.Size): Value {
            return lastOffset
        }

        fun calcPadding(alignement: Int): Value {
            return Hex(alignement.toString(16)) - lastOffset % Hex(alignement.toString(16))
        }

        fun linkLabels(sectionStartAddress: Hex): List<Pair<Label, Hex>> {
            sectionStart = sectionStartAddress
            return content.filter { it.content is Label }.filterIsInstance<MappedContent<Label>>().map {
                it.content to (sectionStartAddress + it.offset).toHex()
            }
        }

        fun generateBytes(sectionStartAddress: Hex, labels: List<Pair<Label, Hex>>) {
            sectionStart = sectionStartAddress
            content.forEach {
                it.bytes = it.content.getBinaryArray(sectionStartAddress + it.offset, labels)
            }
        }

        fun getSectionAddr() = sectionStart
        fun getContent() = content.toTypedArray()

        fun getTSContent(): Array<BundledContent> {
            val labels = content.filter { it.content is Label }

            return content.filter { it.content !is Label }.map { mapped ->
                BundledContent((sectionStart + mapped.offset).toHex(), mapped.bytes, mapped.content.getContentString(), labels.filter { lbl -> lbl.offset == mapped.offset }.map { it.content }.filterIsInstance<Label>())
            }.toTypedArray()
        }

        override fun toString(): String = "$name: ${content.joinToString("") { "\n\t$it" }}"

        data class MappedContent<T : SecContent>(val offset: Hex, val content: T) {
            var bytes: Array<Bin> = arrayOf()
            override fun toString(): String = "${if (content.bytesNeeded != 0) offset.toRawZeroTrimmedString() else ""}${if (bytes.isNotEmpty()) "\t" + bytes.joinToString("") { it.toHex().getRawHexStr() } + "\t" else ""}${content.getContentString()}"
        }

        data class BundledContent(val address: Hex, val bytes: Array<Bin>, val content: String, val label: List<Label>) {
            fun getAddrLblBytesTranscript(): Array<String> = arrayOf(address.toRawZeroTrimmedString(), label.joinToString("\n") { it.getContentString() }, bytes.joinToString("\n") { it.toHex().getRawHexStr() }, content)
        }
    }

    interface SecContent {
        val bytesNeeded: Int
        fun getFirstToken(): Token
        fun getMark(): Memory.InstanceType

        /**
         * This will be stored into memory!
         *
         * Values larger than one byte will be stored memory endianness dependent!
         */
        fun getBinaryArray(yourAddr: Value, labels: List<Pair<Label, Hex>>): Array<Bin>
        fun getContentString(): String
    }

    class Data(val referenceToken: Token, val bin: Hex, val type: DataType) : SecContent {
        override val bytesNeeded: Int = bin.size.getByteCount()

        override fun getFirstToken(): Token = referenceToken

        override fun getMark(): Memory.InstanceType = Memory.InstanceType.DATA

        override fun getBinaryArray(yourAddr: Value, labels: List<Pair<Label, Hex>>): Array<Bin> = arrayOf(bin.toBin())

        override fun getContentString(): String = ".${type.name.lowercase()} ${bin}"

        enum class DataType {
            BYTE,
            SHORT,
            WORD
        }
    }

    data class Label(val label: GASNode.Label) : SecContent {
        override val bytesNeeded: Int = 0
        override fun getMark(): Memory.InstanceType = Memory.InstanceType.PROGRAM
        override fun getFirstToken(): Token = label.getAllTokens().first()
        override fun getBinaryArray(yourAddr: Value, labels: List<Pair<Label, Hex>>): Array<Bin> = emptyArray()
        override fun getContentString(): String = label.identifier + ":"
    }

}