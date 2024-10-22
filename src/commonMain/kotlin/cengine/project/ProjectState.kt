package cengine.project

import cengine.lang.asm.ast.TargetSpec
import kotlinx.serialization.Serializable

@Serializable
data class ProjectState(
    val absRootPath: String,
    val target: String
){
    fun getTarget(): TargetSpec? = TargetSpec.specs.firstOrNull { it.name == target }
}
