package ui.editor

import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Font

data class EditorTheme(
    val name: String,
    val bg: Color,
    val fg: Color,
    val border: Color,
    val controlsBg: Color,
    val tabsBg: Color
)
