package ui

import io.nacular.doodle.drawing.Font
import ui.editor.EditorTheme

data object Styles {

    val lightEditorTheme = EditorTheme(
        name = "light",
        bg = UITools.col(0xEEEEEE),
        fg = UITools.col(0x313131),
        border = UITools.col(0x6b7394),
        tabsBg = UITools.col(0x293256),
        controlsBg = UITools.col(0x5e6687)
    )

    val darkEditorTheme = EditorTheme(
        name = "dark",
        bg = UITools.col(0x222222),
        fg = UITools.col(0xEEEEEE),
        border = UITools.col(0x6b7394),
        tabsBg = UITools.col(0x293256),
        controlsBg = UITools.col(0x5e6687)
    )

    val darkTheme = AppTheme(
        "dark",
        darkEditorTheme
    )

    val lightTheme = AppTheme(
        "light",
        lightEditorTheme
    )

}