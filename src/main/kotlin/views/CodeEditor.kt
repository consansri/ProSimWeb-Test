package views

import AppLogic
import csstype.*
import kotlinx.browser.localStorage
import org.w3c.dom.*
import react.*
import react.dom.aria.ariaHidden
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.code
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.pre
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.textarea
import kotlin.math.abs

external interface CodeEditorProps : Props {
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
    var updateParent: (newData: AppLogic) -> Unit
}

// CSS CLASSES
var CLASS_EDITOR = "editor"
var CLASS_EDITOR_CONTROL = "editor-control"
var CLASS_EDITOR_CONTROLS = "editor-controls"
var CLASS_EDITOR_HIGHLIGHTING_LANGUAGE = "highlighting-html"
var CLASS_EDITOR_CONTAINER = "editor-container"
var CLASS_EDITOR_SCROLL_CONTAINER = "editor-scroll-container"
var CLASS_EDITOR_LINE_NUMBERS = "editor-line-numbers"
var CLASS_EDITOR_INPUT_DIV = "editor-input-div"
var CLASS_EDITOR_AREA = "editor-area"
var CLASS_EDITOR_HIGHLIGHTING = "editor-highlighting"
var CLASS_EDITOR_HIGHLIGHTING_CONTENT = "editor-highlighting-content"

// CSS IDS
var STR_EDITOR_CONTROL_CHECK = "editor-check"
var STR_EDITOR_SAVE = "savedText"
var STR_EDITOR_SAVESTATE = "saveState"
var STR_EDITOR_SAVESTATELENGTH = "saveStateLength"


val CodeEditor = FC<CodeEditorProps> { props ->

    val textareaRef = useRef<HTMLTextAreaElement>(null)
    val lineNumbersRef = useRef<HTMLDivElement>(null)
    val btnClearRef = useRef<HTMLAnchorElement>(null)
    val btnUndoRef = useRef<HTMLAnchorElement>(null)
    val inputDivRef = useRef<HTMLDivElement>(null)
    val codeAreaRef = useRef<HTMLElement>(null)

    // State für aktuellen Text
    val saveState: MutableList<String> = mutableListOf()

    var data by useState(props.appLogic)
    val change = props.update
    val lineHeight by useState(21)

    fun updateHLText(value: String) {
        codeAreaRef.current?.let {
            it.innerHTML = data.getArch().getHighlightedInput(value)
        }
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

    fun updateTAResize() {
        var height = 0

        textareaRef.current?.let {
            var lineCount = it.value.split("\n").size + 1
            height = lineCount * lineHeight
            it.style.height = "auto"
            it.style.height = "${height}px"
        }
        inputDivRef.current?.let {
            it.style.height = "auto"
            if (height != 0) {
                it.style.height = "${height}px"
            }
        }
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
            updateHLText(it.value)
            updateLineNumbers()
            updateTAResize()
        }
        btnUndoRef.current?.let {
            if (saveState.size > 1) {
                it.style.display = "block"
            } else {
                it.style.display = "none"
            }
        }
    }

    fun addUndoSaveState(value: String) {
        if (saveState.size >= 30) {
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

    fun updateEditor() {
        val taValue: String
        textareaRef.current?.let {
            taValue = it.value
            updateHLText(taValue)
            updateLineNumbers()
            updateTAResize()
            updateClearButton(it)
        }
    }

    div {
        className = ClassName(CLASS_EDITOR)

        div {
            className = ClassName(CLASS_EDITOR_CONTROLS)

            a {
                id = "build"
                className = ClassName(CLASS_EDITOR_CONTROL)
                img {
                    src = "icons/cpu-charge.svg"
                }

            }

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
                        addUndoSaveState(it.value)
                        it.value = ""
                        addUndoSaveState(it.value)
                        localStorage.setItem(STR_EDITOR_SAVE, it.value)
                        updateEditor()
                        props.updateParent
                    }
                }
            }
        }

        div {
            className = ClassName(CLASS_EDITOR_CONTAINER)


            div {
                className = ClassName(CLASS_EDITOR_SCROLL_CONTAINER)

                div {
                    className = ClassName(CLASS_EDITOR_LINE_NUMBERS)
                    ref = lineNumbersRef
                    span {

                    }
                }

                div {
                    className = ClassName(CLASS_EDITOR_INPUT_DIV)
                    ref = inputDivRef

                    textarea {
                        className = ClassName(CLASS_EDITOR_AREA)
                        ref = textareaRef
                        spellCheck = false
                        placeholder = "Enter ${data.getArch().getName()} Assembly ..."

                        onInput = { event ->
                            val area = textareaRef.current

                            // Update Save State (For Undo)
                            if (abs(saveState.last().length - event.currentTarget.value.length) > 5 || saveState.last()
                                    .split("\n").size != event.currentTarget.value.split("\n").size
                            ) {
                                addUndoSaveState(event.currentTarget.value)
                            }
                            // Save the text in localStorage
                            localStorage.setItem(STR_EDITOR_SAVE, event.currentTarget.value)

                            area?.let {
                                updateEditor()
                            }

                            // Update Parent State (App)
                            props.updateParent
                        }

                        onKeyDown = { event ->
                            if (event.key == "Tab") {
                                textareaRef.current?.let {
                                    val start =
                                        it.selectionStart ?: error("CodeEditor: OnKeyDown Tab handling: start is null")
                                    val end =
                                        it.selectionEnd ?: error("CodeEditor: OnKeyDown Tab handling: end is null")

                                    it.value = it.value.substring(0, start) + '\t' + it.value.substring(end)

                                    it.selectionEnd = end + 1

                                    event.preventDefault()
                                    addUndoSaveState(it.value)
                                    updateEditor()
                                    props.updateParent
                                }
                            } else if (event.key == "Backspace") {
                                textareaRef.current?.let {
                                    addUndoSaveState(it.value)
                                }
                            } else if (event.ctrlKey && event.key == "z") {
                                handleUndo()
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
                                updateEditor()
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
                            console.log("(update) CodeEditor")
                        }
                    }

                    pre {
                        className = ClassName(CLASS_EDITOR_HIGHLIGHTING)
                        ariaHidden = true

                        code {
                            className = ClassName(CLASS_EDITOR_HIGHLIGHTING_LANGUAGE)
                            className = ClassName(CLASS_EDITOR_HIGHLIGHTING_CONTENT)
                            ref = codeAreaRef

                        }
                    }

                }
            }
        }
    }

    useEffect(change) {
        console.log("(update) CodeEditor")
    }
}

