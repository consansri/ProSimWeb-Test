package emulator.kit.compiler

import emulator.kit.Architecture
import emulator.kit.compiler.gas.DefinedAssembly
import emulator.kit.compiler.gas.GASAssembler
import emulator.kit.compiler.gas.GASParser
import emulator.kit.types.Variable.Value.*
import emulator.kit.types.Variable.Size.*
import emulator.kit.compiler.lexer.TokenSeq.Component.*
import emulator.kit.compiler.lexer.TokenSeq.Component.InSpecific.*
import emulator.kit.compiler.lexer.Lexer
import emulator.kit.compiler.parser.Parser
import emulator.kit.nativeError
import kotlinx.coroutines.Deferred
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
    override val assembly: Assembly = GASAssembler(architecture, definedAssembly)

    private val lexer = Lexer(architecture, definedAssembly.detectRegistersByName)

    val processes: MutableList<Process> = mutableListOf()

    /**
     * Executes and controls the compilation
     */
    override fun compile(mainFile: CompilerFile, others: List<CompilerFile>, build: Boolean): Process.Result {
        architecture.getConsole().clear()
        val process = Process(mainFile, others, build)
        processes.add(process)
        val result = process.launch(architecture.getTranscript(), lexer, parser, assembly)

        result.tree?.printError()?.let {
            architecture.getConsole().error(it)
            nativeError(it)
        }

        if (result.hasErrors()) {
            architecture.getConsole().error("Process failed with an exception!\n$process")
        } else {
            architecture.getConsole().info("Process finished SUCCESSFUL\n$process")
        }


        return result
    }

    override fun runningProcesses(): List<Process> = processes

    override fun isInTreeCacheAndHasNoErrors(file: CompilerFile): Boolean = !(parser.treeCache[file]?.hasErrors() ?: true)

}