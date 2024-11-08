package cengine.project

import androidx.compose.runtime.Immutable
import cengine.lang.asm.ast.TargetSpec
import cengine.vfs.FPath
import kotlinx.serialization.Serializable
import ui.EmulatorContentView
import ui.ToolContentType
import ui.ViewType

@Serializable
@Immutable
data class ProjectState(
    val absRootPath: String,
    val target: String,
    val viewType: ViewType = ViewType.IDE,
    val ide: IDEViewState = IDEViewState(),
    val emu: EMUViewState = EMUViewState(),
) {
    @Serializable
    @Immutable
    data class IDEViewState(
        val openFiles: List<FPath> = emptyList(),
        val leftContentType: ToolContentType? = null,
        val rightContentType: ToolContentType? = null,
        val bottomContentType: ToolContentType? = null,
        val leftWidth: Float = 200f,
        val bottomHeight: Float = 200f,
        val rightWidth: Float = 200f
    )

    @Serializable
    @Immutable
    data class EMUViewState(
        val objFilePath: FPath? = null,
        val leftContent: EmulatorContentView? = null,
        val bottomContent: EmulatorContentView? = null,
        val rightContent: EmulatorContentView? = null,
        val leftWidth: Float = 200f,
        val bottomHeight: Float = 200f,
        val rightWidth: Float = 200f
    )

    fun getTarget(): TargetSpec? = TargetSpec.specs.firstOrNull { it.name == target }
}
