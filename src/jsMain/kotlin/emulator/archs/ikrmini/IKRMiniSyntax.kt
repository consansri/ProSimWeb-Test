package emulator.archs.ikrmini

import emulator.kit.Architecture
import emulator.kit.assembly.Compiler
import emulator.kit.assembly.Syntax
import emulator.kit.common.FileHandler
import emulator.kit.common.Transcript

class IKRMiniSyntax : Syntax() {
    override val applyStandardHLForRest: Boolean = true

    override fun clear() {    }

    override fun check(arch: Architecture, compiler: Compiler, tokens: List<Compiler.Token>, tokenLines: List<List<Compiler.Token>>, others: List<FileHandler.File>, transcript: Transcript): SyntaxTree {

        return SyntaxTree()
    }
}