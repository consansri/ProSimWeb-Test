package emulator.archs.riscv64

import emulator.kit.assembly.Compiler
import emulator.kit.assembly.Syntax
import emulator.kit.common.FileHandler
import emulator.kit.common.Transcript
import emulator.kit.types.Variable

class RV64Syntax: Syntax() {

    override val applyStandardHLForRest: Boolean = true
    override val decimalValueSize: Variable.Size = Variable.Size.Bit64()

    override fun clear() {

    }

    override fun check(compiler: Compiler, tokenLines: List<List<Compiler.Token>>, others: List<FileHandler.File>, transcript: Transcript): SyntaxTree {
        return SyntaxTree()
    }

}