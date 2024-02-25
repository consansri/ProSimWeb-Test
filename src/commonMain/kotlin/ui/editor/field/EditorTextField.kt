package ui.editor.field

import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.core.View
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.drawing.text
import ui.editor.EditorTheme

class EditorTextField(val editorTheme: EditorTheme): TextField("Code goes here ...") {


    init {
        
    }


    override fun render(canvas: Canvas) {
        canvas.rect(bounds.atOrigin, editorTheme.bg.paint)
        canvas.text("${this::class.simpleName}", color = editorTheme.fg)
    }

}