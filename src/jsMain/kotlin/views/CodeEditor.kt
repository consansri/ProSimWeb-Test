package views

import AppLogic
import StyleConst
import csstype.*
import emotion.react.css
import extendable.ArchConst
import extendable.components.connected.FileHandler
import kotlinx.js.timers.*
import org.w3c.dom.*
import react.*
import react.dom.aria.ariaHidden
import react.dom.html.AutoComplete
import react.dom.html.InputType
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.code
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.input
import react.dom.html.ReactHTML.pre
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.textarea
import tools.DebugTools
import views.components.TranscriptView

external interface CodeEditorProps : Props {
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
    var updateParent: (newData: AppLogic) -> Unit
}


val CodeEditor = FC<CodeEditorProps> { props ->

    /* ----------------- STATIC VARIABLES ----------------- */

    val lineHeight = 21

    /* ----------------- REACT REFERENCES ----------------- */

    val textareaRef = useRef<HTMLTextAreaElement>(null)
    val lineNumbersRef = useRef<HTMLDivElement>(null)
    val btnSwitchRef = useRef<HTMLAnchorElement>(null)
    val btnDarkModeRef = useRef<HTMLAnchorElement>(null)
    val btnClearRef = useRef<HTMLAnchorElement>(null)
    val btnUndoRef = useRef<HTMLAnchorElement>(null)
    val btnRedoRef = useRef<HTMLAnchorElement>(null)
    val addtabinput = useRef<HTMLInputElement>(null)
    val infoPanelRef = useRef<HTMLAnchorElement>(null)
    val editorContainerRef = useRef<HTMLDivElement>(null)
    val inputDivRef = useRef<HTMLDivElement>(null)
    val codeAreaRef = useRef<HTMLElement>(null)
    val preHLTimeoutRef = useRef<Timeout>(null)
    val checkTimeOutRef = useRef<Timeout>(null)
    val executionPointInterval = useRef<Timeout>(null)

    /* ----------------- REACT STATES ----------------- */

    val appLogic by useState(props.appLogic)
    val (update, setUpdate) = props.update
    val (internalUpdate, setIUpdate) = useState(false)
    val (checkState, setCheckState) = useState(appLogic.getArch().getState().getState())
    val (currExeLine, setCurrExeLine) = useState(1)
    val (taValueUpdate, setTaValueUpdate) = useState(false)

    val (showAddTab, setShowAddTab) = useState(false)
    val (lineNumbers, setLineNumbers) = useState<Int>(1)
    val (darkMode, setDarkMode) = useState(false)
    val (infoPanelText, setInfoPanelText) = useState("")

    /* ----------------- localStorage Sync Objects ----------------- */

    val (vc_rows, setvc_rows) = useState<List<String>>(emptyList())
    val (files, setFiles) = useState<List<FileHandler.File>>()
    val (transcriptView, setTranscriptView) = useState(false)

    /* ----------------- Initiate Intervals ----------------- */

    executionPointInterval.current?.let {
        clearInterval(it)
    }
    if (!DebugTools.REACT_deactivateAutoRefreshs) {
        executionPointInterval.current = setInterval({
            val lineAddressMap = appLogic.getArch().getAssembly().getAssemblyMap().lineAddressMap
            val pcValue = appLogic.getArch().getRegisterContainer().pc.value.get()
            val lineID = lineAddressMap.get(pcValue.toHex().getRawHexStr())
            lineID?.let {
                setCurrExeLine(it + 1)
            }
        }, 250)
    }

    /* ----------------- UPDATE VISUAL COMPONENTS ----------------- */



    fun updateLineNumbers() {
        val textarea = textareaRef.current ?: return
        val numberOfLines = textarea.value.split("\n").size
        setLineNumbers(numberOfLines)
    }

    fun updateTAResize() {
        var height = 0

        textareaRef.current?.let {
            val lineCount = it.value.split("\n").size + 1
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

    fun updateClearButton() {
        textareaRef.current?.let {
            if (it.value != "") {
                btnClearRef.current?.classList?.remove(StyleConst.CLASS_ANIM_DEACTIVATED)
            } else {
                btnClearRef.current?.classList?.add(StyleConst.CLASS_ANIM_DEACTIVATED)
            }
        }
    }

    fun updateUndoRedoButton() {
        if (appLogic.getArch().getFileHandler().getCurrUndoLength() > 0) {
            btnUndoRef.current?.classList?.remove(StyleConst.CLASS_ANIM_DEACTIVATED)
        } else {
            btnUndoRef.current?.classList?.add(StyleConst.CLASS_ANIM_DEACTIVATED)
        }
        if (appLogic.getArch().getFileHandler().getCurrRedoLength() > 0) {
            btnRedoRef.current?.classList?.remove(StyleConst.CLASS_ANIM_DEACTIVATED)
        } else {
            btnRedoRef.current?.classList?.add(StyleConst.CLASS_ANIM_DEACTIVATED)
        }
    }

    /* ----------------- ASYNC Events ----------------- */

    fun checkCode(immediate: Boolean) {
        val valueToCheck = appLogic.getArch().getFileHandler().getCurrContent()
        val delay: Int
        val size = valueToCheck.split("\n").size

        delay = when {
            size < 500 -> 1000
            size > 3000 -> 3000
            else -> size
        }

        if (immediate) {
            setvc_rows(appLogic.getArch().check(valueToCheck, currExeLine).split("\n"))
            setCheckState(appLogic.getArch().getState().getState())
        } else {
            checkTimeOutRef.current?.let {
                clearTimeout(it)
            }
            checkTimeOutRef.current = setTimeout({
                setvc_rows(appLogic.getArch().check(valueToCheck, currExeLine).split("\n"))
                setCheckState(appLogic.getArch().getState().getState())
            }, delay)
        }
    }

    fun preHighlight() {
        val value = appLogic.getArch().getFileHandler().getCurrContent()
        setvc_rows(value.split("\n"))
        preHLTimeoutRef.current?.let {
            clearTimeout(it)
        }
        setvc_rows(value.split("\n"))
        preHLTimeoutRef.current = setTimeout({
            val hlTaList = appLogic.getArch().getPreHighlighting(value).split("\n")
            setvc_rows(hlTaList)
        }, 300)
    }


    /* ----------------- CHANGE EVENTS ----------------- */
    fun edit(content: String, immediate: Boolean) {
        appLogic.getArch().getState().edit()
        setCheckState(appLogic.getArch().getState().getState())

        appLogic.getArch().getFileHandler().editCurr(content)
        textareaRef.current?.let {
            it.value = appLogic.getArch().getFileHandler().getCurrContent()
        }

        preHighlight()
        checkCode(immediate)
        setTaValueUpdate(!taValueUpdate)
    }

    fun undo() {
        appLogic.getArch().getState().edit()
        setCheckState(appLogic.getArch().getState().getState())

        appLogic.getArch().getFileHandler().undoCurr()
        textareaRef.current?.let {
            it.value = appLogic.getArch().getFileHandler().getCurrContent()
        }

        preHighlight()
        checkCode(false)
        setTaValueUpdate(!taValueUpdate)
    }

    fun redo() {
        appLogic.getArch().getState().edit()
        setCheckState(appLogic.getArch().getState().getState())

        appLogic.getArch().getFileHandler().redoCurr()
        textareaRef.current?.let {
            it.value = appLogic.getArch().getFileHandler().getCurrContent()
        }

        preHighlight()
        checkCode(false)
        setTaValueUpdate(!taValueUpdate)
    }

    /* ----------------- DOM ----------------- */

    div {
        className = ClassName(StyleConst.CLASS_EDITOR)

        div {
            className = ClassName(StyleConst.CLASS_EDITOR_CONTROLS)

            a {
                id = "switch"
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL)
                ref = btnSwitchRef
                title = "Transcript Switch"
                ReactHTML.img {
                    src = StyleConst.Icons.disassembler
                }

                onClick = {
                    setTranscriptView(!transcriptView)
                }
            }

            a {
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL)

                onClick = {
                    checkCode(true)
                }

                when (checkState) {
                    ArchConst.STATE_UNCHECKED -> {
                        title = "Status: loading..."
                        img {
                            className = ClassName(StyleConst.CLASS_ANIM_ROTATION)
                            src = StyleConst.Icons.status_loading
                        }
                    }

                    ArchConst.STATE_EXECUTABLE -> {
                        title = "Status: ready to build"
                        img {
                            src = StyleConst.Icons.status_fine
                        }
                    }

                    ArchConst.STATE_HASERRORS -> {
                        title = "Status: fix errors!"
                        img {
                            src = StyleConst.Icons.status_error
                        }
                    }

                    ArchConst.STATE_EXECUTION -> {
                        title = "Status: executing..."
                        img {
                            src = StyleConst.Icons.status_fine
                        }
                    }

                    else -> {
                        title = "Status: loading..."
                        img {
                            className = ClassName(StyleConst.CLASS_ANIM_ROTATION)
                            src = StyleConst.Icons.status_loading
                        }
                    }
                }
            }

            a {
                id = "darkmode"
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL + " " + StyleConst.CLASS_ANIM_HOVER)
                ref = btnDarkModeRef
                title = "Editor Mode"

                img {
                    if (darkMode) {
                        src = StyleConst.Icons.darkmode
                    } else {
                        src = StyleConst.Icons.lightmode
                    }
                }

                onClick = { event ->
                    setDarkMode(!darkMode)
                }

            }

            a {
                id = "undo"
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL + " " + StyleConst.CLASS_ANIM_HOVER)
                ref = btnUndoRef
                title = "Undo"

                img {
                    src = StyleConst.Icons.backwards
                }

                onClick = {
                    undo()
                }
            }

            a {
                id = "redo"
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL + " " + StyleConst.CLASS_ANIM_HOVER)
                ref = btnRedoRef
                title = "Redo"

                img {
                    src = StyleConst.Icons.forwards
                }

                onClick = {
                    redo()
                }
            }

            a {
                id = "info"
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL + " " + StyleConst.CLASS_ANIM_HOVER)

                title = """
                    Code Editor Info
                 
                        Shortcuts
                        - CTRL + Z  (Undo)
                        - CTRL + C  (Copy)
                        - CTRL + V  (Insert)
                        - CTRL + A  (Select All)
                        - TAB       (Insert Tab)
                        - CTRL + ALT + L (Reformat)
                        
                        Features
                        - Tab Handling
                        - Undo History
                        - Clear Button
                        - Code Highlighting
                        - Transcript Switch
                        - Start of Program (click on linenumber)
                        
                    
                """.trimIndent()

                img {
                    src = StyleConst.Icons.info
                }

            }

            a {
                id = "editor-clear"
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL + " " + StyleConst.CLASS_ANIM_HOVER)
                ref = btnClearRef
                title = "Clear"

                img {
                    src = StyleConst.Icons.delete
                }

                onClick = {
                    edit("", false)
                }
            }

            a {
                id = "editor-info-panel"
                ref = infoPanelRef

                title = "{$infoPanelText}"

                img {
                    src = StyleConst.Icons.tag
                }
            }

        }

        div {
            className = ClassName(StyleConst.CLASS_EDITOR_CONTAINER)
            ref = editorContainerRef

            if (transcriptView) {
                TranscriptView {
                    this.ta_val = ta_val ?: ""
                    this.transcript = appLogic.getArch().getTranscript()
                    this.appLogic = appLogic
                }
            } else {

                div {

                    className = ClassName(StyleConst.CLASS_EDITOR_TABS)

                    for (fileID in appLogic.getArch().getFileHandler().getAllFiles().indices) {
                        val file = appLogic.getArch().getFileHandler().getAllFiles()[fileID]
                        a {

                            className = ClassName(StyleConst.CLASS_EDITOR_TAB + if (file == appLogic.getArch().getFileHandler().getCurrent()) " ${StyleConst.CLASS_EDITOR_TAB_ACTIVE}" else "")

                            a {
                                +file.getName()
                            }

                            if (file == appLogic.getArch().getFileHandler().getCurrent()) {
                                img {
                                    src = StyleConst.Icons.delete

                                    onClick = {
                                        appLogic.getArch().getFileHandler().remove(file)
                                        edit(appLogic.getArch().getFileHandler().getCurrContent(), false)
                                        setTaValueUpdate(!taValueUpdate)
                                    }
                                }
                            } else {
                                onClick = {
                                    appLogic.getArch().getFileHandler().setCurrent(fileID)
                                    edit(appLogic.getArch().getFileHandler().getCurrContent(), false)
                                    setTaValueUpdate(!taValueUpdate)
                                }
                            }
                        }
                    }


                    if (showAddTab) {
                        a {
                            className = ClassName(StyleConst.CLASS_EDITOR_TAB)

                            input {
                                ref = addtabinput
                                type = InputType.text
                                placeholder = "name"
                            }

                            a {
                                +"+"

                                onClick = {
                                    val input = addtabinput.current
                                    input?.let {
                                        val success = appLogic.getArch().getFileHandler().import(FileHandler.File(it.value, ""))
                                        if (success) {
                                            edit(appLogic.getArch().getFileHandler().getCurrContent(), false)
                                            setTaValueUpdate(!taValueUpdate)
                                            setShowAddTab(false)
                                        } else {
                                            input.classList.add(StyleConst.ANIM_SHAKERED)
                                            setTimeout({
                                                input.classList.remove(StyleConst.ANIM_SHAKERED)
                                            }, 300)
                                        }
                                    }
                                }
                            }

                        }
                    } else {
                        a {
                            className = ClassName(StyleConst.CLASS_EDITOR_TAB)

                            +"+"

                            onClick = { event ->
                                setShowAddTab(true)
                            }
                        }
                    }


                }


                div {
                    className = ClassName(StyleConst.CLASS_EDITOR_SCROLL_CONTAINER)

                    div {
                        className = ClassName(StyleConst.CLASS_EDITOR_LINE_NUMBERS)
                        ref = lineNumbersRef

                        for (lineNumber in 1..lineNumbers) {
                            span {
                                onClick = { event ->
                                    appLogic.getArch().exeUntilLine(lineNumber - 1)
                                    props.updateParent(appLogic)
                                }

                                if (lineNumber == currExeLine) {
                                    className = ClassName(StyleConst.CLASS_EDITOR_LINE_ACTIVE)
                                    +"► $lineNumber"

                                } else {

                                    +"$lineNumber"
                                }
                            }
                        }
                    }

                    div {
                        className = ClassName(StyleConst.CLASS_EDITOR_INPUT_DIV)
                        ref = inputDivRef

                        textarea {
                            className = ClassName(StyleConst.CLASS_EDITOR_AREA)
                            ref = textareaRef
                            autoComplete = AutoComplete.off
                            autoCorrect = "off"
                            cols = 50
                            autoCapitalize = "off"
                            spellCheck = false
                            placeholder = "Enter ${appLogic.getArch().getName()} Assembly ..."

                            onSelect = { event ->
                                val cursorPosition = event.currentTarget.selectionStart
                                cursorPosition?.let { cursorPos ->

                                    val lines = event.currentTarget.value.substring(0, cursorPos).split("\n")
                                    val lineID = lines.size - 1
                                    val startIndex = lines[lineID].length

                                    val grammarTree = appLogic.getArch().getAssembly().getGrammarTree()
                                    grammarTree?.rootNode?.let { rootNode ->

                                    }
                                }
                            }

                            onChange = { event ->
                                edit(event.currentTarget.value, false)
                            }

                            onKeyDown = { event ->
                                if (event.key == "Tab") {

                                    textareaRef.current?.let {
                                        val start =
                                            it.selectionStart
                                                ?: error("CodeEditor: OnKeyDown Tab handling: start is null")
                                        val end =
                                            it.selectionEnd ?: error("CodeEditor: OnKeyDown Tab handling: end is null")

                                        it.value = it.value.substring(0, start) + '\t' + it.value.substring(end)

                                        it.selectionEnd = end + 1

                                        event.preventDefault()
                                        edit(event.currentTarget.value, false)
                                    }
                                } else if (event.ctrlKey && event.key == "z") {
                                    undo()
                                } else if (event.key == "Enter") {
                                    edit(event.currentTarget.value, false)
                                } else if (event.ctrlKey && event.altKey && event.key == "l") {
                                    // REFORMAT CODE
                                    val lines = event.currentTarget.value.split("\n").toMutableList()
                                    for (lineID in lines.indices) {
                                        var lineContent = lines[lineID]
                                        for (reformat in Formatter.reformats) {
                                            lineContent = lineContent.replace(reformat.regex) {
                                                it.value.replaceFirst(it.groupValues[1], reformat.replace)
                                            }
                                        }
                                        lines[lineID] = lineContent
                                    }

                                    val formatted = lines.joinToString("\n") { it }
                                    event.currentTarget.value = formatted
                                    edit(formatted, false)
                                }
                            }
                        }

                        pre {
                            className = ClassName(StyleConst.CLASS_EDITOR_HIGHLIGHTING)
                            ariaHidden = true

                            code {
                                className = ClassName(StyleConst.CLASS_EDITOR_HIGHLIGHTING_LANGUAGE)
                                className = ClassName(StyleConst.CLASS_EDITOR_HIGHLIGHTING_CONTENT)
                                ref = codeAreaRef

                                vc_rows?.let {
                                    var contentString = ""
                                    for (i in it.indices) {
                                        contentString += "${it[i]}\n"
                                    }
                                    codeAreaRef.current?.let {
                                        it.innerHTML = contentString
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    /* ----------------- USEEFFECTS (Save and Reload from localStorage) ----------------- */

    useEffect(update) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(update) CodeEditor")
        }
        /* Component RELOAD */
        appLogic.getArch().getFileHandler().getFromLocalStorage()
        /* -- LOAD from localStorage -- */
        setFiles(appLogic.getArch().getFileHandler().getAllFiles())
        textareaRef.current?.let {
            it.value = appLogic.getArch().getFileHandler().getCurrContent()
            edit(it.value, false)
        }
    }

    useEffect(checkState) {
        when (checkState) {
            ArchConst.STATE_EXECUTABLE -> {
                btnSwitchRef.current?.classList?.remove(StyleConst.CLASS_ANIM_DEACTIVATED)
            }

            else -> {
                btnSwitchRef.current?.classList?.add(StyleConst.CLASS_ANIM_DEACTIVATED)
            }
        }
    }

    useEffect(transcriptView) {
        btnSwitchRef.current?.let {
            if (transcriptView) {
                it.classList.add(StyleConst.CLASS_EDITOR_CONTROL_ACTIVE)
            } else {
                it.classList.remove(StyleConst.CLASS_EDITOR_CONTROL_ACTIVE)
            }
        }
        updateTAResize()
        updateClearButton()
        updateLineNumbers()
    }

    useEffect(taValueUpdate) {
        updateTAResize()
        updateClearButton()
        updateLineNumbers()
        updateUndoRedoButton()
    }

    useEffect(darkMode) {
        val div = editorContainerRef.current
        div?.let {
            if (darkMode) {
                div.classList.add(StyleConst.CLASS_EDITOR_DARKMODE)
            } else {
                div.classList.remove(StyleConst.CLASS_EDITOR_DARKMODE)
            }
        }
    }


}

object Formatter {
    val reformats = listOf<ReFormat>(
        ReFormat(Regex("""(\s{2,})"""), "\t"),
        ReFormat(Regex("""(,)\S"""), ", ")
    )
}

data class ReFormat(val regex: Regex, val replace: String)