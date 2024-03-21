package emulator.archs.ikrrisc2

import emulator.kit.assembly.Compiler
import emulator.kit.assembly.standards.StandardSyntax

class IKRRisc2Syntax: StandardSyntax(IKRRisc2.WORD_WIDTH, '#', listOf(),instrParamsCanContainWordsBesideLabels = false) {
    override fun MutableList<Compiler.Token>.checkInstr(elements: MutableList<TreeNode.ElementNode>, errors: MutableList<Error>, warnings: MutableList<Warning>, currentLabel: ELabel?): Boolean {
        TODO("Not yet implemented")
    }
}