package ui

import ui.editor.EditorTheme

data object Styles {

    val lightEditorTheme = EditorTheme(
        name = "light",
        bg = UITools.col(0xEEEEEE),
        fg = UITools.col(0x313131),
        border = UITools.col(0x6b7394),
        tabsBg = UITools.col(0x293256),
        controlsBg = UITools.col(0x5e6687),
        hlGrayscale = EditorTheme.HLGrayscale(
            UITools.col(0x202746),
            UITools.col(0x293256),
            UITools.col(0x5e6687),
            UITools.col(0x6b7394),
            UITools.col(0x898ea4),
            UITools.col(0x979db4),
            UITools.col(0xdfe2f1),
            UITools.col(0xf5f7ff)
        ),
        hlColors = EditorTheme.HLColors(
            red = UITools.col(0xc94922),
            orange = UITools.col(0xc76b29),
            yellow = UITools.col(0xc08b30),
            green = UITools.col(0xac9739),
            greenPCMark = UITools.col(0x008b19),
            cyan = UITools.col(0x22a2c9),
            blue = UITools.col(0x3d8fd1),
            violet = UITools.col(0x6679cc),
            magenta = UITools.col(0x9c637a),
        )
    )

    val darkEditorTheme = EditorTheme(
        name = "dark",
        bg = UITools.col(0x222222),
        fg = UITools.col(0xEEEEEE),
        border = UITools.col(0x6b7394),
        tabsBg = UITools.col(0x293256),
        controlsBg = UITools.col(0x5e6687),
        hlGrayscale = EditorTheme.HLGrayscale(
            UITools.col(0xf5f7ff),
            UITools.col(0xdfe2f1),
            UITools.col(0x979db4),
            UITools.col(0x898ea4),
            UITools.col(0x6b7394),
            UITools.col(0x5e6687),
            UITools.col(0x293256),
            UITools.col(0x202746),
        ),
        hlColors = EditorTheme.HLColors(
            red = UITools.col(0xc94922),
            orange = UITools.col(0xc76b29),
            yellow = UITools.col(0xc08b30),
            green = UITools.col(0xac9739),
            greenPCMark = UITools.col(0x008b19),
            cyan = UITools.col(0x22a2c9),
            blue = UITools.col(0x3d8fd1),
            violet = UITools.col(0x6679cc),
            magenta = UITools.col(0x9c637a),
        )
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