package emulator.kit.assembler

import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.gas.GASNode
import emulator.kit.assembler.lexer.Lexer
import emulator.kit.optional.Feature
import emulator.kit.types.Variable

interface DefinedAssembly {

    val memAddrSize: Variable.Size
    val wordSize: Variable.Size
    val instrsAreWordAligned: Boolean
    val detectRegistersByName: Boolean
    val prefices: Lexer.Prefices
    fun getInstrs(features: List<Feature>): List<InstrTypeInterface>
    fun getAdditionalDirectives(): List<DirTypeInterface>
    fun parseInstrParams(rawInstr: GASNode.RawInstr, tempContainer: GASParser.TempContainer): List<GASParser.SecContent>

}