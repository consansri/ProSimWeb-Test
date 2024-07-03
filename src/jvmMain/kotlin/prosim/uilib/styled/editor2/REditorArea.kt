package prosim.uilib.styled.editor2

import cengine.annotation.Annotater
import cengine.completion.Completer
import cengine.editing.Editor
import cengine.highlighting.Highlighter
import cengine.CodeModel
import cengine.selection.Caret
import cengine.selection.Selection
import cengine.selection.Selector
import cengine.text.RopeModel
import cengine.text.TextModel
import org.jetbrains.skia.Canvas
import prosim.uilib.styled.core.SComp
import prosim.uilib.styled.core.border.Insets

class REditorArea(var laf: EditorLAF = EditorLAF()) : SComp(), CodeModel {

    override val textModel: TextModel = RopeModel()
    override val selector: Selector = object : Selector {
        override val caret: Caret = Caret(textModel)
        override val selection: Selection = Selection()

    }
    override var editor: Editor? = null
    override var highlighter: Highlighter? = null
    override var annotater: Annotater? = null
    override var completer: Completer? = null

    val rowHeader: RRowHeader = RRowHeader(this)

    override var insets: Insets = Insets(5,5,5,5)

    /*override fun setDefaults(theme: Theme, scaling: Scaling, icons: Icons) {
        laf = EditorLAF()
    }*/

    override val preferredWidth: Int get() =  laf.fontCode.measureTextWidth(textModel.toString()).toInt() + insets.left + insets.right
    override val preferredHeight: Int get() = (laf.fontCode.metrics.height * textModel.lines).toInt() + insets.top + insets.bottom

    override fun render(canvas: Canvas) {

    }

    override fun customRender(canvas: Canvas) {
        // Draw Content

        var xContent = insets.left.toFloat()
        var yContent = insets.top.toFloat()

        // Draw All
        for (i in 0..<textModel.length) {
            val char = textModel.charAt(i)

            if (i == selector.caret.index) {
                canvas.drawLine(xContent + laf.fgPaint.strokeWidth / 2, yContent, xContent + laf.fgPaint.strokeWidth / 2, yContent + laf.fontCode.metrics.height, laf.fgPaint)
            }

            if (char == '\n') {
                xContent = insets.left.toFloat()
                yContent += laf.fontCode.metrics.height
            } else {
                canvas.drawString(char.toString(), xContent, yContent - laf.fontCode.metrics.ascent, laf.fontCode, laf.fgPaint)
                xContent += laf.fontCode.measureTextWidth(char.toString())
            }
        }
    }
}