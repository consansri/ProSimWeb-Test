package ui.editor

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.core.container
import io.nacular.doodle.core.width
import io.nacular.doodle.drawing.*
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.layout.constraints.center
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.layout.constraints.fill
import io.nacular.doodle.utils.Direction
import io.nacular.doodle.utils.Resizer
import ui.editor.controls.EditorControls
import ui.editor.field.EditorTextField
import ui.editor.tabs.EditorTabs
import kotlin.math.min

class Editor(display: Display, textMetrics: TextMetrics, val theme: EditorTheme) : View() {
    init {
        // Layout Childviews
        val controls = EditorControls(theme).apply {
            backgroundColor = theme.controlsBg
            width = 32.0
        }
        val textField = EditorTextField(theme).apply {
            minimumSize = Size(0.0, 0.0)
            Resizer(this, manageCursor = true).apply { directions = setOf(Direction.North); movable = false }
        }
        val tabs = EditorTabs(theme).apply {
            backgroundColor = theme.tabsBg
            height = 32.0
            minimumSize = Size(0.0, 32.0)
            Resizer(this, manageCursor = true).apply { directions = setOf(Direction.South); movable = false }
        }

        display += container {
            this += listOf(controls, tabs, textField)

            size = Size(min(382.0, display.width - 20), min(632.0, display.width - 20))

            val inset = 1

            layout = constrain(textField, controls, tabs) { field, controls, tabs ->
                tabs.top eq inset
                controls.top eq inset
                controls.left eq inset

                tabs.left eq controls.right
                field.left eq controls.right

                field.top eq tabs.bottom

                controls.bottom eq field.bottom
                tabs.right eq field.right

                controls.width + field.width eq parent.width - 2 * inset
                tabs.height + field.height eq parent.height - 2 * inset
            }

            render = { rect(bounds.atOrigin, fill = theme.border.paint) }

            Resizer(this, manageCursor = true, movable = true).apply { directions = setOf(Direction.South, Direction.East); movable = false }
        }
    }

    override fun render(canvas: Canvas) {
        canvas.rect(bounds.atOrigin, fill = Color.Lightgray.paint)
        canvas.text("${this::class.simpleName}", color = theme.bg)
    }

}