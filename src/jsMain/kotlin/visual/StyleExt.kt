package visual

import StyleAttr
import emulator.kit.assembly.Compiler
import emulator.kit.common.Memory
import web.cssom.Color

object StyleExt {

    fun Memory.InstanceType.get(mode: StyleAttr.Mode): Color {
        return when (mode) {
            StyleAttr.Mode.LIGHT -> Color("#${this.light.toString(16)}")
            StyleAttr.Mode.DARK -> Color("#${this.dark?.toString(16) ?: this.light.toString(16)}")
        }
    }

    fun Compiler.CodeStyle.get(mode: StyleAttr.Mode): Color {
        return when (mode) {
            StyleAttr.Mode.LIGHT -> Color("#${this.lightHexColor.toString(16)}")
            StyleAttr.Mode.DARK -> Color("#${this.darkHexColor?.toString(16) ?: this.lightHexColor.toString(16)}")
        }
    }

    fun List<Compiler.Token>.getVCRows(): List<String> {
        return this.joinToString("") {
            val severity = it.getSeverity()?.type
            val codeStyle = it.getCodeStyle()
            if (severity != null) {
                if (codeStyle == null) highlight(it.content, it.id, severity.name) else highlight(it.content, it.id, severity.name, codeStyle.name)
            } else {
                if (codeStyle == null) it.content else highlight(it.content, it.id, codeStyle.name)
            }
        }.split("\n")
    }

    /**
     * Tool
     * for surrounding a input with a certain highlighting html tag
     */
    fun highlight(input: String, id: Int? = null, vararg classes: String): String {
        val tag = "span"
        return "<$tag class='${classes.joinToString(" ") { it }}' ${id?.let { "id='$id'" }}>$input</$tag>"
    }


}