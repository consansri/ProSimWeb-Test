package ui.editor

import io.nacular.doodle.drawing.Color

data class EditorTheme(
    val name: String,
    val bg: Color,
    val fg: Color,
    val border: Color,
    val controlsBg: Color,
    val tabsBg: Color,
    val hlGrayscale: HLGrayscale,
    val hlColors: HLColors

){
    data class HLGrayscale(
        val base0: Color,
        val base1: Color,
        val base2: Color,
        val base3: Color,
        val base4: Color,
        val base5: Color,
        val base6: Color,
        val base7: Color,
    )

    data class HLColors(
        val red: Color,
        val orange: Color,
        val yellow: Color,
        val green: Color,
        val greenPCMark: Color,
        val cyan: Color,
        val blue: Color,
        val violet: Color,
        val magenta: Color
    )
}
