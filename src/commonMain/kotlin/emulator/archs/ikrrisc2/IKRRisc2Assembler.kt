package emulator.archs.ikrrisc2

import emulator.kit.assembler.DirTypeInterface
import emulator.kit.assembler.InstrTypeInterface
import emulator.kit.assembler.gas.DefinedAssembly
import emulator.kit.assembler.gas.GASParser
import emulator.kit.assembler.gas.nodes.GASNode
import emulator.kit.assembler.lexer.Lexer
import emulator.kit.assembler.lexer.Token
import emulator.kit.optional.Feature
import emulator.kit.types.Variable

class IKRRisc2Assembler : DefinedAssembly {
    override val MEM_ADDRESS_SIZE: Variable.Size
        get() = TODO("Not yet implemented")
    override val WORD_SIZE: Variable.Size
        get() = TODO("Not yet implemented")
    override val INSTRS_ARE_WORD_ALIGNED: Boolean
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