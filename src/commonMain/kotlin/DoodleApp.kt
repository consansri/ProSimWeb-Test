import io.nacular.doodle.application.Application
import io.nacular.doodle.controls.text.TextField
import io.nacular.doodle.core.*
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.Color.Companion.Black
import io.nacular.doodle.drawing.Color.Companion.Blue
import io.nacular.doodle.drawing.Color.Companion.Red
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.paint
import io.nacular.doodle.geometry.Circle
import io.nacular.doodle.utils.Dimension
import ui.Styles
import ui.TestView
import ui.editor.Editor
import ui.editor.EditorTheme
import kotlin.math.min
import ui.UITools.col

class DoodleApp(display: Display, textMetrics: TextMetrics) : Application {
    init {
        println("startup ${this::class.simpleName}")

        display += Editor(display, textMetrics, Styles.darkEditorTheme)

        /*display += TestView(display, textMetrics, "some cool message!")

        display += view {
            size = display.size
            render = {
                circle(
                    Circle(
                        center = display.center,
                        radius = min(display.width, display.height) / 2 - 10
                    ), fill = Red.paint
                )
            }
        }

        display.fill(Black.paint)*/


    }

    override fun shutdown() {
        println("shutdown ${this::class.simpleName}!")
    }


}