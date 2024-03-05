import io.nacular.doodle.animation.Animator
import io.nacular.doodle.application.Application
import io.nacular.doodle.core.*
import io.nacular.doodle.drawing.FontLoader
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.image.ImageLoader
import io.nacular.doodle.theme.ThemeManager
import io.nacular.doodle.theme.adhoc.DynamicTheme
import io.nacular.doodle.theme.native.NativeHyperLinkStyler
import ui.Styles
import ui.editor.Editor

class DoodleApp(
    display: Display,
    fonts: FontLoader,
    private val images: ImageLoader,
    textMetrics: TextMetrics,
    focusManager: FocusManager,
    animator: Animator
) : Application {
    init {
        println("startup ${this::class.simpleName}")

        display += Editor(display,focusManager, textMetrics, animator,fonts, Styles.lightEditorTheme)

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