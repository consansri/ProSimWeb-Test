package prosim.ide

import cengine.lang.asm.ast.TargetSpec
import cengine.project.ProjectState
import emulator.kit.nativeLog
import prosim.uilib.styled.frame.CFrame
import java.awt.Dimension

object WindowManager {

    fun createNewProjectWindow(){
        ProjectSelectorWindow()
    }

    fun openProject(path: String, targetSpec: TargetSpec) {
        nativeLog("Open Project: $path ${targetSpec.name}")
        val content = ProjectWindow(ProjectState(path, targetSpec.name))
        val frame = CFrame("$targetSpec - $path")
        frame.contentPane = content
        frame.size = Dimension(1920, 1080)
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }
}