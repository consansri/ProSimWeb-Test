import io.nacular.doodle.HtmlElementViewFactory
import io.nacular.doodle.animation.Animator
import io.nacular.doodle.animation.AnimatorImpl
import io.nacular.doodle.application.Application
import io.nacular.doodle.application.HtmlElementViewModule
import io.nacular.doodle.application.Modules
import io.nacular.doodle.application.Modules.Companion.AccessibilityModule
import io.nacular.doodle.application.Modules.Companion.FocusModule
import io.nacular.doodle.application.Modules.Companion.FontModule
import io.nacular.doodle.application.Modules.Companion.ImageModule
import io.nacular.doodle.application.Modules.Companion.KeyboardModule
import io.nacular.doodle.application.Modules.Companion.ModalModule
import io.nacular.doodle.application.Modules.Companion.PathModule
import io.nacular.doodle.application.Modules.Companion.PointerModule
import io.nacular.doodle.application.Modules.Companion.UrlViewModule
import io.nacular.doodle.application.application
import io.nacular.doodle.controls.text.Label
import io.nacular.doodle.controls.text.LabelBehavior
import io.nacular.doodle.geometry.PathMetrics
import io.nacular.doodle.layout.constraints.constrain
import io.nacular.doodle.theme.native.NativeTheme.Companion.nativeTextFieldBehavior
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.w3c.dom.HTMLElement

fun main() {
    val root = document.getElementById("root") as HTMLElement
    root.style.width = "100vw"
    root.style.height = "100vh"

    application(root = root, modules = listOf(
        FontModule, // For using Fonts
        PointerModule, // For using Mouse Pointer
        KeyboardModule, // For recognizing keystrokes
        ImageModule, // For rendering and loading images
        PathModule, // For drawing vector graphics
        Modules.DocumentModule,
        AccessibilityModule,
        ModalModule,
        UrlViewModule,
        nativeTextFieldBehavior(),
        DI.Module(name = "AppModule") {
            bindProvider<Animator> { AnimatorImpl(timer = instance(), animationScheduler = instance()) } // for animations (i.e. editor cursor)
        }
    )) {
        DoodleApp(
            display = instance(),
            fonts = instance(),
            images = instance(),
            textMetrics = instance(),
            focusManager = instance(),
            animator = instance()
        )
    }

}

