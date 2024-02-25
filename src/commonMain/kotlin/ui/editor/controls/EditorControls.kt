package ui.editor.controls

import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.drawing.text
import ui.editor.EditorTheme

class EditorControls(val editorTheme: EditorTheme): View() {

    override fun render(canvas: Canvas) {
        canvas.rect(bounds.atOrigin, editorTheme.controlsBg.paint)
        canvas.text("${this::class.simpleName}", color = editorTheme.fg)
    }

}