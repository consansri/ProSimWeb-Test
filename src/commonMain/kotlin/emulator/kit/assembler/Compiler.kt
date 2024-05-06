package emulator.kit.assembler

import emulator.kit.*
import emulator.kit.assembler.gas.DefinedAssembly
import emulator.kit.assembler.gas.GASParser
import emulator.kit.types.Variable.Value.*
import emulator.kit.types.Variable.Size.*
import emulator.kit.assembler.lexer.Lexer
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Parser
import kotlinx.datetime.*

/**
 * The [Compiler] is the first instance which analyzes the text input. Common pre analyzed tokens will be delivered to each Syntax implementation. The [Compiler] fires the compilation events in the following order.
 *
 * 1. common analysis ([tokenize])
 * 2. specific analysis ([parse] which uses the given logic from [parser])
 * 3. highlight tokens ([highlight])
 * 4. convert syntax tree to binary ([assemble] which uses the given logic from [assembly])
 *
 * @param parser gets an object of the architecture specific [Syntax]-Class implementation from the assembler configuration through the [Architecture].
 * @param assembly gets an object of the architecture specific [Assembly]-Class implementation from the assembler configuration through the [Architecture].
 * @param hlFlagCollection contains the standard token highlighting flags.
 *
 * @property regexCollection contains the standard token regular expressions.
 *
 */
class Compiler(
    private val architecture: Architecture,
    private val definedAssembly: DefinedAssembly
) : CompilerInterface {

    override val parser: Parser = GASParser(this, definedAssembly)
    private val lexer = Lexer(architecture, definedAssembly.detectRegistersByName, definedAssembly.prefices)

    val processes: MutableList<Process> = mutableListOf()
    private var lastLineAddrMap: Map<String, Token.LineLoc> = mapOf()
        set(value) {
            field = value
        }

    override fun getLastLineMap(): Map<String, Token.LineLoc> = lastLineAddrMap

    /**
     * Executes and controls the compilation
     */
    override fun compile(mainFile: CompilerFile, others: List<CompilerFile>, build: Boolean): Process.Result {
        architecture.getConsole().clear()
        val process = Process(mainFile, others, build)
        processes.add(process)
        val result = process.launch(lexer, parser, architecture.getMemory(), architecture.getAllFeatures())

        result.tree?.printError()?.let {
            architecture.getConsole().error(it)
        }

        result.tree?.printWarning()?.let {
            architecture.getConsole().warn(it)
        }

        if (result.hasErrors()) {
            architecture.getConsole().error("Process failed with an exception!\n$process")
        } else {
            architecture.getConsole().info("Process finished SUCCESSFUL\n$process")
        }

        processes.remove(process)

        if (build) lastLineAddrMap = result.assemblyMap
        return result
    }

    override fun runningProcesses(): List<Process> = processes

    override fun isInTreeCacheAndHasNoErrors(file: CompilerFile): Boolean = !(parser.treeCache[file]?.hasErrors() ?: true)

}