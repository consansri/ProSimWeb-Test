package ui.uilib.editor

import androidx.compose.ui.input.key.*
import cengine.editor.CodeEditor


fun KeyHandler(editor: CodeEditor, keyEvent: KeyEvent): Boolean {

    if (keyEvent.type == KeyEventType.KeyDown) {
        if (keyEvent.utf16CodePoint.toChar().isISOControl()) {
            when (keyEvent.key) {
                Key.DirectionUp -> {
                    editor.selector.moveCaretUp(1, keyEvent.isShiftPressed)
                    return true
                }
                Key.DirectionLeft -> {
                    editor.selector.moveCaretLeft(1, keyEvent.isShiftPressed)
                }

                Key.DirectionRight -> {
                    editor.selector.moveCaretRight(1, keyEvent.isShiftPressed)

                }
                Key.DirectionDown -> {
                    editor.selector.moveCaretDown(1, keyEvent.isShiftPressed)
                }
            }
            return true
        } else {
            editor.insert(editor.selector.caret, keyEvent.nativeKeyEvent.toString())
            return true
        }

    } else {

    }

    return false
}
