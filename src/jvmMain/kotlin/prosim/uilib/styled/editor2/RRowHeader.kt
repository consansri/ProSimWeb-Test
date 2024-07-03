package prosim.uilib.styled.editor2

import org.jetbrains.skia.Canvas
import prosim.uilib.styled.core.SComp

class RRowHeader(val editor: REditorArea) : SComp() {
    override val preferredWidth: Int get() = editor.laf.fontCode.measureTextWidth(editor.textModel.lines.toString()).toInt()
    override val preferredHeight: Int get() = editor.laf.fontCode.measureTextWidth(editor.textModel.lines.toString()).toInt()

    override fun customRender(canvas: Canvas) {
        val lines = editor.textModel.lines

        var y = editor.insets.top.toFloat()
        val leftBound = editor.insets.left.toFloat()
        val rightBound = sWidth - editor.insets.left - editor.insets.right
        val lineCountSpace = editor.laf.fontCode.measureTextWidth(lines.toString())

        canvas.drawLine(sWidth.toFloat() - editor.laf.fgPaintSec.strokeWidth, 0f, sWidth.toFloat() - editor.laf.fgPaintSec.strokeWidth, sHeight.toFloat(), editor.laf.fgPaintSec)

        for (line in 0..lines) {
            val lineStr = line.toString()
            val strWidth = editor.laf.fontCode.measureTextWidth(lineStr)
            canvas.drawString(line.toString(), editor.insets.left.toFloat() + lineCountSpace - strWidth, y - editor.laf.fontCode.metrics.ascent, editor.laf.fontCode, editor.laf.fgPaintSec)
            y += editor.laf.fontCode.metrics.height
        }
    }
}