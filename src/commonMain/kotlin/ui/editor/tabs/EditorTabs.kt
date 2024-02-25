package ui.editor.tabs

import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.drawing.text
import ui.editor.EditorTheme

class EditorTabs(val editorTheme: EditorTheme): View() {



    override fun render(canvas: Canvas) {
        canvas.rect(bounds.atOrigin, editorTheme.tabsBg.paint)
        canvas.text("${this::class.simpleName}", color = editorTheme.fg)
    }

}