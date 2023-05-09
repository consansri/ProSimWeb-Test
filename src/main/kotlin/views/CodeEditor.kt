package views

import AppData
import csstype.ClassName
import kotlinx.browser.document
import kotlinx.browser.localStorage
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLTextAreaElement
import react.*
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.textarea

external interface CodeEditorProps : Props {
    var appData: AppData
    var update: Boolean
    var updateParent: (newData: AppData) -> Unit
}

// CSS CLASSES
var CLASS_EDITOR_CONTROL = "editor-control"

// CSS IDS
var STR_EDITOR_CONTROLS = "editor-controls"
var STR_EDITOR_CONTROL_CHECK = "editor-check"
var STR_EDITOR_CONTAINER = "editor-container"
var STR_EDITOR_LINE_NUMBERS = "editor-line-numbers"
var STR_EDITOR_AREA = "editor-area"
var STR_EDITOR_SAVE = "savedText"
var STR_EDITOR_SAVESTATE = "saveState"
var STR_EDITOR_SAVESTATELENGTH = "saveStateLength"

val CodeEditor = FC<CodeEditorProps> { props ->

    val textareaRef = useRef<HTMLTextAreaElement>(null)
    val lineNumbersRef = useRef<HTMLDivElement>(null)
    val btnClearRef = useRef<HTMLAnchorElement>(null)
    val btnUndoRef = useRef<HTMLAnchorElement>(null)

    // State für aktuellen Text
    val saveState: MutableList<String> = mutableListOf()

    var data by useState(props.appData)
    val (change, setChange) = useState(props.update)

    useEffect(change) {
        console.log("processorView updated")
    }

    fun updateLineNumbers() {
        val textarea = textareaRef.current ?: return
        val lineNumbers = lineNumbersRef.current ?: return
        val numberOfLines = textarea.value.split("\n").size

        val spanStr = buildString {
            repeat(numberOfLines) { append("<span></span>") }
        }

        lineNumbers.innerHTML = spanStr
    }

    fun updateClearButton(textarea: HTMLTextAreaElement) {
        if (textarea.value != "") {
            btnClearRef.current?.style?.display = "block"
        } else {
            btnClearRef.current?.style?.display = "none"
        }
    }

    fun handleUndo() {
        textareaRef.current?.let {
            saveState.removeLast()
            it.value = saveState.last()
            updateClearButton(it)
            localStorage.setItem(STR_EDITOR_SAVE, it.value)
            updateLineNumbers()
        }
        btnUndoRef.current?.let {
            if (saveState.size > 1) {
                it.style.display = "block"
            } else {
                it.style.display = "none"
            }
        }
    }

    fun updateSaveState(value: String) {
        if (saveState.size >= 20) {
            saveState.removeFirst()
        }
        saveState += value
        for (i in saveState.indices) {
            localStorage.setItem(STR_EDITOR_SAVESTATE + "$i", saveState[i])
        }
        localStorage.setItem(STR_EDITOR_SAVESTATELENGTH, saveState.size.toString())
        btnUndoRef.current?.let {
            if (saveState.size > 1) {
                it.style.display = "block"
            } else {
                it.style.display = "none"
            }
        }
    }

    fun updateCodeCorrection() {
        var state = "correct"
    }

    div {
        id = STR_EDITOR_CONTROLS


        a {
            id = "undo"
            className = ClassName(CLASS_EDITOR_CONTROL)
            ref = btnUndoRef
            img {
                src = "icons/undo.svg"
            }

            onClick = {
                if (saveState.size > 1) {
                    handleUndo()
                    props.updateParent
                }

            }

        }

        a {
            className = ClassName(CLASS_EDITOR_CONTROL)
            id = STR_EDITOR_CONTROL_CHECK
            img {
                src = "icons/exclamation-mark2.svg"
            }

        }

        a {
            id = "editor-clear"
            className = ClassName(CLASS_EDITOR_CONTROL)
            ref = btnClearRef
            img {
                src = "icons/clear.svg"
            }

            onClick = {
                textareaRef.current?.let {
                    updateSaveState(it.value)
                    it.value = ""
                    updateClearButton(it)
                    updateLineNumbers()
                    localStorage.setItem(STR_EDITOR_SAVE, it.value)
                    props.updateParent
                }
            }
        }
    }

    div {
        id = STR_EDITOR_CONTAINER

        div {
            id = STR_EDITOR_LINE_NUMBERS
            ref = lineNumbersRef
            span {

            }

        }
        textarea {
            id = STR_EDITOR_AREA
            ref = textareaRef
            spellCheck = false


            onChange = { event ->
                val lineNumbers = document.getElementById(STR_EDITOR_LINE_NUMBERS) as HTMLDivElement
                val numberOfLines = event.currentTarget.value.split("\n").size

                var spanStr = ""

                for (i in 0 until numberOfLines) {
                    spanStr += "<span></span>"
                }

                lineNumbers.innerHTML = spanStr

                updateSaveState(event.currentTarget.value)

                // Save the text in localStorage
                localStorage.setItem(STR_EDITOR_SAVE, event.currentTarget.value)

                // Make Clear Button visible if value not empty
                updateClearButton(event.currentTarget)
                props.updateParent
            }

            onKeyDown = { event ->
                if (event.key == "Tab") {
                    val area = document.getElementById(STR_EDITOR_AREA) as HTMLTextAreaElement
                    val start = area.selectionStart ?: error("CodeEditor: OnKeyDown Tab handling: start is null")
                    val end = area.selectionEnd ?: error("CodeEditor: OnKeyDown Tab handling: end is null")

                    area.value = area.value.substring(0, start) + '\t' + area.value.substring(end)

                    area.selectionEnd = end + 1

                    event.preventDefault()
                    props.updateParent
                }
            }

            // Load the saved text from localStorage
            useEffect {
                val area = textareaRef.current ?: return@useEffect
                val savedText = localStorage.getItem(STR_EDITOR_SAVE)
                val saveStateLength = localStorage.getItem(STR_EDITOR_SAVESTATELENGTH)?.toInt()
                saveStateLength?.let {
                    for (i in 0..saveStateLength) {
                        localStorage.getItem(STR_EDITOR_SAVESTATE + "$i")?.let {
                            saveState.add(i, it)
                        }
                    }
                }

                if (savedText != null && savedText != "") {
                    area.value = savedText
                    updateLineNumbers()
                } else {
                    btnClearRef.current?.style?.display = "none"
                }
                btnUndoRef.current?.let {
                    if (saveState.size > 1) {
                        it.style.display = "block"
                    } else {
                        it.style.display = "none"
                    }
                }
            }

        }
    }

}