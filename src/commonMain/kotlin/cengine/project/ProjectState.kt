package cengine.project

import cengine.lang.asm.ast.TargetSpec

data class ProjectState(
    val absRootPath: String,
    val targetSpec: TargetSpec
)
