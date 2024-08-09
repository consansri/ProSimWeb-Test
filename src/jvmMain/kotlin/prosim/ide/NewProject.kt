package prosim.ide

import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.ast.target.riscv.rv32.RV32Spec
import cengine.project.ProjectState
import emulator.kit.nativeLog
import prosim.uilib.styled.CChooser
import prosim.uilib.styled.CFrame
import prosim.uilib.styled.CTextButton
import prosim.uilib.styled.button.CDirectoryChooser
import prosim.uilib.styled.params.FontType
import java.awt.GridBagConstraints
import java.awt.GridBagLayout

class NewProject : CFrame() {
    val directoryChooser = CDirectoryChooser()
    val asmChooser = CChooser<AsmSpec>(CChooser.Model(AsmSpec.specs, RV32Spec, "Target"), FontType.BASIC)
    val open = CTextButton("open").apply {
        addActionListener {
            val dir = directoryChooser.selectedDirectory
            if (dir != null) {
                nativeLog("NewProject: ${dir.absolutePath}")
                launchProject(dir.absolutePath, asmChooser.value)
            }
        }
    }

    init {
        content.layout = GridBagLayout()
        val gbc = GridBagConstraints()
        gbc.gridy = 0
        gbc.weightx = 1.0
        content.add(directoryChooser, gbc)

        gbc.gridy = 1
        content.add(asmChooser, gbc)

        gbc.gridy = 2
        content.add(open, gbc)

        revalidate()
        repaint()
    }

    private fun launchProject(path: String, asmSpec: AsmSpec) {
        val content = ProjectWindow(ProjectState(path, asmSpec))
        val frame = CFrame(content)
    }

}