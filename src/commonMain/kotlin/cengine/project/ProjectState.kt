package cengine.project

import cengine.lang.asm.ast.AsmSpec

data class ProjectState(
    val absRootPath: String,
    val asmSpec: AsmSpec
)
