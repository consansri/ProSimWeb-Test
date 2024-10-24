package cengine.project

import cengine.lang.asm.ast.TargetSpec
import cengine.vfs.FPath
import kotlinx.serialization.Serializable

@Serializable
data class ProjectState(
    val absRootPath: String,
    val target: String,
) {
    var emuObjFilePath: FPath? = null
        set(value) {
            field = value
            ProjectStateManager.projectStateChanged()
        }

    fun getTarget(): TargetSpec? = TargetSpec.specs.firstOrNull { it.name == target }
}
