package prosim.ide

import cengine.lang.asm.ast.AsmSpec
import cengine.project.ProjectState
import prosim.uilib.styled.frame.CFrame
import java.awt.Dimension

object WindowManager {

    fun createNewProjectWindow(){
        NewProject()
    }

    fun openProject(path: String, asmSpec: AsmSpec) {
        val content = ProjectWindow(ProjectState(path, asmSpec))
        val frame = CFrame("$asmSpec - $path")
        frame.contentPane = content
        frame.size = Dimension(1920, 1080)
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }

}