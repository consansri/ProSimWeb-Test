package emulator.kit.assembler

import emulator.kit.*
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.lexer.Lexer
import emulator.kit.assembler.lexer.Token
import emulator.kit.assembler.parser.Parser

/**
 * The [Compiler] handles all [Process].
 * Processes can be initiated and Process Results will be delivered.
 *
 * @param definedAssembly delivers the arch dependent assembler implementation for the [Parser].
 * @property parser holds the Parser Implementation this Compiler will always use the [GASParser] for this.
 * @property lexer holds the Lexer Implementation this Compiler will always use the [Lexer] for this.
 */
class Compiler(
    private val architecture: Architecture,
    private val definedAssembly: DefinedAssembly
) {

    val parser: Parser = GASParser(this, definedAssembly)
    val lexer = Lexer(architecture, definedAssembly.detectRegistersByName, definedAssembly.prefices)

    val processes: MutableList<Process> = mutableListOf()
    private var lastLineAddrMap: Map<String, Token.LineLoc> = mapOf()

    fun getLastLineMap(): Map<String, Token.LineLoc> = lastLineAddrMap

    /**
     * Executes and controls the compilation process
     */
    fun compile(mainFile: CompilerFile, others: List<CompilerFile>, build: Process.Mode): Process.Result {
        architecture.getConsole().clear()
        val process = Process(mainFile, others, build)
        processes.add(process)
        val result = process.launch(lexer, parser, architecture.getMemory(), architecture.getAllFeatures())

        result.tree.printError()?.let {
            architecture.getConsole().error(it)
        }

        result.tree.printWarning()?.let {
            architecture.getConsole().warn(it)
        }

        if (result.hasErrors()) {
            architecture.getConsole().error("Process failed with an exception!\n$process")
        } else {
            architecture.getConsole().info("Process finished SUCCESSFUL\n$process")
        }

        if (build == Process.Mode.FULLBUILD) {
            lastLineAddrMap = result.assemblyMap
        }

        processes.remove(process)
        return result
    }

    fun runningProcesses(): List<Process> = processes

    fun isInTreeCacheAndHasNoErrors(file: CompilerFile): Boolean = !(parser.treeCache[file]?.hasErrors() ?: true)

}