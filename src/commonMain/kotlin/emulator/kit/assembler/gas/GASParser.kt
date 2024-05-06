package emulator.kit.assembler.gas

import debug.DebugTools
import emulator.kit.assembler.CompilerFile
import emulator.kit.assembler.CompilerInterface
import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.gas.nodes.GASNode.*
import emulator.kit.assembler.gas.nodes.GASNodeType
import emulator.kit.assembler.lexer.Lexer
import emulator.kit.assembler.lexer.Severity
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Parser
import emulator.kit.assembler.parser.ParserTree
import emulator.kit.common.Memory
import emulator.kit.nativeLog
import emulator.kit.optional.Feature
import emulator.kit.types.Variable

class GASParser(compiler: CompilerInterface, val definedAssembly: DefinedAssembly) : Parser(compiler) {
    override fun getDirs(features: List<Feature>): List<DirTypeInterface> = definedAssembly.getAdditionalDirectives() + GASDirType.entries
    override fun getInstrs(features: List<Feature>): List<InstrTypeInterface> = definedAssembly.getInstrs(features)
    override fun parse(source: List<Token>, others: List<CompilerFile>, features: List<Feature>): ParserTree {
        // Preprocess and Filter Tokens
        val filteredSource = filter(source)

        // Build the tree
        val root = GASNode.buildNode(GASNodeType.ROOT, filteredSource, getDirs(features), definedAssembly)
        if (root == null || root !is Root) return ParserTree(null, source, filteredSource, arrayOf())

        // Filter
        root.removeEmptyStatements()

        if (DebugTools.KIT_showGrammarTree) {
            nativeLog("Tree: ${root.print("")}")
        }

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

        val tempContainer = TempContainer(definedAssembly.MEM_ADDRESS_SIZE, root)

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
        }

        val sections = tempContainer.sections.toTypedArray()

        /**
         * Define Section Start Addresses
         */
        var currentAddress: Variable.Value = Variable.Value.Hex("0", definedAssembly.MEM_ADDRESS_SIZE)
        val sectionAddressMap = mutableMapOf<Section, Variable.Value.Hex>()
        sections.forEach {
            sectionAddressMap.set(it, currentAddress.toHex())
            currentAddress += it.lastOffset
        }

        /**
         * Link All Section Labels
         */
        val allLinkedLabels = mutableListOf<Pair<Label, Variable.Value.Hex>>()
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
        }

        nativeLog(tempContainer.sections.joinToString("\n\n") {
            it.toString()
        })

        return ParserTree(root, source, filteredSource, sections)
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
        val addressSize: Variable.Size,
        val root: Root,
        val symbols: MutableList<Symbol> = mutableListOf(),
        val sections: MutableList<Section> = mutableListOf(Section("text", addressSize), Section("data", addressSize), Section("bss", addressSize)),
        val macros: MutableList<Macro> = mutableListOf(),
        var currSection: Section = sections.first(),
    ) {
        fun switchToOrAppendSec(name: String) {
            val sec = sections.firstOrNull { it.name.lowercase() == name.lowercase() }
            if (sec != null) {
                currSection = sec
                return
            }
            val newSec = Section(name, addressSize)
            sections.add(newSec)
            currSection = newSec
        }

        fun setOrReplaceSymbol(symbol: Symbol) {
            val alreadydefined = symbols.firstOrNull { it.name == symbol.name }
            symbols.remove(alreadydefined)
            symbols.add(symbol)
        }
    }

    data class Macro(val name: String, val arguments: List<Argument>, val content: List<Statement>) {
        fun generatePseudoStatements(lexer: Lexer, lineLoc: Token.LineLoc, argMap: List<ArgDef>): List<Token> {
            var content = content.map { it.contentBackToString() }.joinToString("") { it }
            val args = arguments.map { it.argName.content to it.getDefaultValue() }.toTypedArray()

            // Check for mixture of positional and indexed arguments
            argMap.forEach { def ->
                when (def) {
                    is ArgDef.KeyWord -> {
                        val argID = args.indexOfFirst { it.first == def.keyWord }
                        args[argID] = args[argID].first to def.content
                    }

                    is ArgDef.Positional -> {
                        args[def.position] = args[def.position].first to def.content
                    }
                }
            }

            args.forEach {
                content = content.replace("\\${it.first}", it.second)
            }

            return lexer.pseudoTokenize(lineLoc, content)
        }

        sealed class ArgDef(val content: String) {
            class Positional(token: Token, content: String, val position: Int) : ArgDef(content)
            class KeyWord(token: Token, content: String, val keyWord: String) : ArgDef(content)

        }
    }

    sealed class Symbol(val name: String) {
        class Undefined(name: String) : Symbol(name)
        class StringExpr(name: String, val expr: GASNode.StringExpr) : Symbol(name)
        class IntegerExpr(name: String, val expr: GASNode.NumericExpr) : Symbol(name)
        class TokenRef(name: String, val token: Token) : Symbol(name)
    }

    data class Section(val name: String, val addressSize: Variable.Size) {
        private val content: MutableList<MappedContent<*>> = mutableListOf()
        var lastOffset: Variable.Value = Variable.Value.Hex("0", addressSize)

        private var sectionStart: Variable.Value.Hex = Variable.Value.Hex("0", addressSize)

        fun addContent(sectionContent: SecContent) {
            content.add(MappedContent(lastOffset.toHex(), sectionContent))
            lastOffset += Variable.Value.Hex(sectionContent.bytesNeeded.toString(16), addressSize)
        }

        fun getLastAddress(addressSize: Variable.Size): Variable.Value {
            return lastOffset
        }

        fun calcPadding(alignement: Int): Variable.Value {
            return Variable.Value.Hex(alignement.toString(16)) - lastOffset % Variable.Value.Hex(alignement.toString(16))
        }

        fun linkLabels(sectionStartAddress: Variable.Value.Hex): List<Pair<Label, Variable.Value.Hex>> {
            sectionStart = sectionStartAddress
            return content.filter { it.content is Label }.filterIsInstance<MappedContent<Label>>().map {
                it.content to (sectionStartAddress + it.offset).toHex()
            }
        }

        fun generateBytes(sectionStartAddress: Variable.Value.Hex, labels: List<Pair<Label, Variable.Value.Hex>>) {
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

        override fun toString(): String = "Section $name: ${content.joinToString("") { "\n\t$it" }}"

        data class MappedContent<T : SecContent>(val offset: Variable.Value.Hex, val content: T) {
            var bytes: Array<Variable.Value.Bin> = arrayOf()
            override fun toString(): String = "${if (content.bytesNeeded != 0) offset.toRawZeroTrimmedString() else ""}${if (bytes.isNotEmpty()) "\t" + bytes.joinToString("") { it.toHex().getRawHexStr() } + "\t" else ""}${content.getContentString()}"
        }

        data class BundledContent(val address: Variable.Value.Hex, val bytes: Array<Variable.Value.Bin>, val content: String, val label: List<Label>) {
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
        fun getBinaryArray(yourAddr: Variable.Value, labels: List<Pair<Label, Variable.Value.Hex>>): Array<Variable.Value.Bin>
        fun getContentString(): String
    }

    class Data(val referenceToken: Token, val bin: Variable.Value.Hex, val type: DataType) : SecContent {
        override val bytesNeeded: Int = bin.size.getByteCount()

        override fun getFirstToken(): Token = referenceToken

        override fun getMark(): Memory.InstanceType = Memory.InstanceType.DATA

        override fun getBinaryArray(yourAddr: Variable.Value, labels: List<Pair<Label, Variable.Value.Hex>>): Array<Variable.Value.Bin> = arrayOf(bin.toBin())

        override fun getContentString(): String = ".${type.name.lowercase()} ${bin}"

        enum class DataType{
            BYTE,
            SHORT,
            WORD
        }
    }

    data class Label(val label: GASNode.Label) : SecContent {
        override val bytesNeeded: Int = 0
        override fun getMark(): Memory.InstanceType = Memory.InstanceType.PROGRAM
        override fun getFirstToken(): Token = label.getAllTokens().first()
        override fun getBinaryArray(yourAddr: Variable.Value, labels: List<Pair<Label, Variable.Value.Hex>>): Array<Variable.Value.Bin> = emptyArray()
        override fun getContentString(): String = label.identifier + ":"
    }

}