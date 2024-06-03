package emulator.archs.ikrrisc2

import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.DefinedAssembly
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.gas.GASNode
import emulator.kit.assembler.lexer.Lexer
import emulator.kit.optional.Feature
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Size.*
import emulator.kit.types.Variable.Value.*

class IKRRisc2Assembler : DefinedAssembly {
    override val memAddrSize: Variable.Size
        get() = TODO("Not yet implemented")
    override val wordSize: Variable.Size
        get() = TODO("Not yet implemented")
    override val detectRegistersByName: Boolean
        get() = TODO("Not yet implemented")
    override val prefices: Lexer.Prefices = object : Lexer.Prefices {
        override val hex: String = "0x"
        override val bin: String = "0b"
        override val dec: String = ""
        override val oct: String = "0"
        override val comment: String = "#"
        override val symbol: Regex = Regex("""^[a-zA-Z$._][a-zA-Z0-9$._]*""")
    }

    override fun getInstrs(features: List<Feature>): List<InstrTypeInterface> {
        TODO("Not yet implemented")
    }

    override fun getAdditionalDirectives(): List<DirTypeInterface> {
        TODO("Not yet implemented")
    }

    override fun parseInstrParams(rawInstr: GASNode.RawInstr, tempContainer: GASParser.TempContainer): List<GASParser.SecContent> {
        TODO("Not yet implemented")
    }
}