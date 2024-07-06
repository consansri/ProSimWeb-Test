package prosim.uilib.styled.editor3

import cengine.CodeModel
import cengine.annotation.Annotater
import cengine.completion.Completer
import cengine.editing.Editor
import cengine.highlighting.Highlighter
import cengine.selection.Caret
import cengine.selection.Selection
import cengine.selection.Selector
import cengine.text.RopeModel
import cengine.text.TextModel
import java.awt.Graphics
import javax.swing.JComponent

class CEditorArea : JComponent(), CodeModel {
    override val textModel: TextModel = RopeModel()
    override val selector: Selector = object : Selector {
        override val caret: Caret = Caret(textModel)
        override val selection: Selection = Selection()
    }
    override var editor: Editor? = null
    override var highlighter: Highlighter? = null
    override var annotater: Annotater? = null
    override var completer: Completer? = null


    override fun paint(g: Graphics?) {

    }

}