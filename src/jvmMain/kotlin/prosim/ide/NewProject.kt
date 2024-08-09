package prosim.ide

import cengine.lang.asm.ast.AsmSpec
import cengine.lang.asm.ast.target.riscv.rv32.RV32Spec
import emulator.kit.nativeLog
import prosim.uilib.styled.CChooser
import prosim.uilib.styled.CTextButton
import prosim.uilib.styled.button.CDirectoryChooser
import prosim.uilib.styled.frame.CFrame
import prosim.uilib.styled.params.FontType
import java.awt.GridBagConstraints
import java.awt.GridBagLayout

class NewProject : CFrame("New Project") {
    val directoryChooser = CDirectoryChooser()
    val asmChooser = CChooser<AsmSpec>(CChooser.Model(AsmSpec.specs, RV32Spec, "Target"), FontType.BASIC)
    val open = CTextButton("open").apply {
        addActionListener {
            val dir = directoryChooser.selectedDirectory
            if (dir != null) {
                nativeLog("NewProject: ${dir.absolutePath}")
                WindowManager.openProject(dir.absolutePath, asmChooser.value)
                dispose()
            }
        }
    }

    init {
        contentPane.layout = GridBagLayout()
        val gbc = GridBagConstraints()
        gbc.gridy = 0
        gbc.weightx = 1.0
        contentPane.add(directoryChooser, gbc)

        gbc.gridy = 1
        contentPane.add(asmChooser, gbc)

        gbc.gridy = 2
        contentPane.add(open, gbc)

        pack()
        setLocationRelativeTo(null)
        isVisible = true
        revalidate()
        repaint()
    }
}