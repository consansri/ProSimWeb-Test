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

class EditorModel: CodeModel {
    override val textModel: TextModel = RopeModel("""
        .equ LED_BASE_ADDR, 0x8000000000000000
        .equ UART_BASE_ADDR, 0x8000000000000001
        .equ USB_BASE_ADDR, 0x8000000000000004

        .equ ACLINT_MTIME, 0x8000000000001824

        .equ RAM_START, 0x800
        .equ LED_BASE_ADDR, 0x8000000000000000
        .equ UART_BASE_ADDR, 0x8000000000000001
        .equ USB_BASE_ADDR, 0x8000000000000004

        .equ ACLINT_MTIME, 0x8000000000001824

        .equ RAM_START, 0x800
        .equ LED_BASE_ADDR, 0x8000000000000000
        .equ UART_BASE_ADDR, 0x8000000000000001
        .equ USB_BASE_ADDR, 0x8000000000000004

        .equ ACLINT_MTIME, 0x8000000000001824

        .equ RAM_START, 0x800
    """.trimIndent())
    override val selector: Selector = object : Selector{
        override val caret: Caret = Caret(textModel).apply {
            set(10)
        }
        override val selection: Selection = Selection()
    }
    override var editor: Editor? = null
    override var highlighter: Highlighter? = null
    override var annotater: Annotater? = null
    override var completer: Completer? = null
}