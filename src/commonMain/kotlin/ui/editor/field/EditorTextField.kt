package ui.editor.field

import CommonConsole
import Coroutines
import io.nacular.doodle.animation.*
import io.nacular.doodle.controls.panels.ScrollPanel
import io.nacular.doodle.drawing.*
import io.nacular.doodle.event.*
import io.nacular.doodle.focus.FocusManager
import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Size
import io.nacular.doodle.system.Cursor
import io.nacular.doodle.text.*
import kotlinx.coroutines.launch
import ui.editor.EditorTheme
import ui.editor.utils.CursorLocation
import kotlin.time.measureTime

class EditorTextField(text: StyledText = StyledText(""), focusManager: FocusManager, animate: Animator, fontLoader: FontLoader, val textMetrics: TextMetrics, val editorTheme: EditorTheme) : ScrollPanel() {
    private var cursorVisible = true
    private var cursorAnimState = true
        set(value) {
            field = value
            actualStyledText = actualStyledText
        }

    private var charWidth: Double = 6.0
    private var lineHeight: Double = 15.5

    public var text: String
        get() = styledText.text
        set(value) {
            styledText = StyledText(value, font = font)
        }

    private var visibleStyledText = styledText
        set(value) {
            field = value
            foregroundColor?.invoke { field }
            font?.invoke { field }
            measureText()
        }

    private var actualStyledText = styledText
        set(value) {
            field = value
            visibleStyledText = value
            measureText()
            rerender()
        }

    public var styledText: StyledText
        get() = visibleStyledText
        set(value) {
            actualStyledText = value
        }

    private var cursorLoc = CursorLocation.getCursorLocation(0, text.text, charWidth, lineHeight)
        set(value) {
            field = value
            actualStyledText = actualStyledText
        }

    init {
        // Setup Font
        Coroutines.context.launch {
            val loadedFont = fontLoader("fonts/JetBrainsMono-2.304/fonts/variable/JetBrainsMono[wght].ttf") {
                this.family = "JetBrainsMono"
                this.weight = 500
                this.size = 10
            }
            font = loadedFont

            CommonConsole.log("loaded this font: ${font?.family}")
        }

        styledText = text

        styleChanged += {
            // force update
            actualStyledText = actualStyledText
            rerender()
        }

        mirrorWhenRightLeft = false

        // force update
        actualStyledText = actualStyledText

        focusable = true

        cursor = Cursor.Text

        // Animate Cursor
        Coroutines.loop(500) {
            cursorAnimState = !cursorAnimState
        }

        // Pointer Listener
        pointerChanged += PointerListener.on {
            focusManager.requestFocus(this)
            val measure = measureTime {
                setCursorPosition(it.location)
            }
            CommonConsole.warn("${this::class.simpleName} -> Cursor Update in ${measure.inWholeMilliseconds}ms")
        }

        keyChanged += KeyListener.pressed {


            when (it.type) {
                KeyState.Type.Up -> {

                }

                KeyState.Type.Down -> {
                    val measure = measureTime {
                        when (it.code) {
                            KeyCode.Enter -> insertText("\n")
                            KeyCode.Tab -> insertText("\t")
                            KeyCode.Backspace -> removeText()
                            KeyCode.ArrowUp -> moveCursorUp(1)
                            KeyCode.ArrowDown -> moveCursorDown(1)
                            KeyCode.ArrowLeft -> moveCursorLeft(1)
                            KeyCode.ArrowRight -> moveCursorRight(1)
                            else -> {
                                insertText(it.key.text)
                            }
                        }
                    }
                    CommonConsole.warn("${this::class.simpleName} -> Text Update in ${measure.inWholeMilliseconds}ms")
                }
            }
        }

        cursorLoc = CursorLocation.getCursorLocation(8, text.text, charWidth, lineHeight)

        focusManager.requestFocus(this)
    }

    private fun moveCursorLeft(amount: Int) {
        cursorLoc = CursorLocation.getCursorLocation(cursorLoc.pos - amount, text, charWidth, lineHeight)
    }

    private fun moveCursorRight(amount: Int) {
        cursorLoc = CursorLocation.getCursorLocation(cursorLoc.pos + amount, text, charWidth, lineHeight)
    }

    private fun moveCursorUp(amount: Int) {
        cursorLoc = CursorLocation.getCursorLocation(cursorLoc.pos - amount, text, charWidth, lineHeight)
    }

    private fun moveCursorDown(amount: Int) {
        cursorLoc = CursorLocation.getCursorLocation(cursorLoc.pos + amount, text, charWidth, lineHeight)
    }

    private fun setCursorPosition(position: Int) {
        cursorLoc = CursorLocation.getCursorLocation(position, text, charWidth, lineHeight)
    }

    private fun setCursorPosition(point: Point) {
        cursorLoc = CursorLocation.getCursorLocation(point, text, charWidth, lineHeight)
    }

    private fun insertText(insertion: String) {
        text = text.substring(0, cursorLoc.pos + 1) + insertion + text.substring(cursorLoc.pos + 1)
        setCursorPosition(cursorLoc.pos + insertion.length)
        CommonConsole.log("Inserted: $insertion")
    }

    private fun removeText() {
        if (cursorLoc.pos > 0) {
            text = text.substring(0, cursorLoc.pos) + text.substring(cursorLoc.pos + 1)
        }
    }

    override fun addedToDisplay() {
        measureText()

        // force update
        actualStyledText = actualStyledText
    }

    override fun render(canvas: Canvas) {
        super.render(canvas)

        canvas.rect(bounds.atOrigin, fill = editorTheme.bg.paint)
        canvas.text(visibleStyledText, at = Point.Origin, textSpacing = TextSpacing(0.0, 0.0))

        // Render Cursor
        if (hasFocus && cursorVisible && cursorAnimState) {
            canvas.line(cursorLoc.coords.first, cursorLoc.coords.second, Stroke(editorTheme.fg.paint, thickness = 1.0))
        }
    }

    private fun measureText(): Size {
        val size = textMetrics.size(visibleStyledText)
        lineHeight = size.height / (text.split("\n").size - 1)
        this.size = size
        CommonConsole.log("$size")
        return size
    }
}

