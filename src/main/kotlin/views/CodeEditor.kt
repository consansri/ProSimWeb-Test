package views

import AppLogic
import StorageKey
import StyleConst
import csstype.ClassName
import extendable.ArchConsts
import kotlinx.browser.localStorage
import kotlinx.js.timers.Timeout
import kotlinx.js.timers.clearTimeout
import kotlinx.js.timers.setTimeout
import org.w3c.dom.*
import react.*
import react.dom.aria.ariaHidden
import react.dom.html.AutoComplete
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.code
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.pre
import react.dom.html.ReactHTML.span
import react.dom.html.ReactHTML.textarea
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
    val btnClearRef = useRef<HTMLAnchorElement>(null)
    val btnUndoRef = useRef<HTMLAnchorElement>(null)
    val inputDivRef = useRef<HTMLDivElement>(null)
    val codeAreaRef = useRef<HTMLElement>(null)

    val undoTimeoutRef = useRef<Timeout>(null)
    val preHLTimeoutRef = useRef<Timeout>(null)
    val checkTimeOutRef = useRef<Timeout>(null)

    /* ----------------- REACT STATES ----------------- */

    val appLogic by useState(props.appLogic)
    val update = props.update
    val (checkState, setCheckState) = useState(appLogic.getArch().state.getState())
    val (exeStartLine, setExeStartLine) = useState(0)
    val (lineNumbers, setLineNumbers) = useState<Int>(1)

    /* ----------------- localStorage Sync Objects ----------------- */

    val (vc_rows, setvc_rows) = useState<List<String>>()
    val (ta_val, setta_val) = useState<String>()
    val (ta_val_ss, setta_val_ss) = useState<List<String>>()
    val (transcriptView, setTranscriptView) = useState(false)

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

    fun updateUndoButton() {
        if (ta_val_ss != null && ta_val_ss.size > 1) {
            btnUndoRef.current?.classList?.remove(StyleConst.CLASS_ANIM_DEACTIVATED)
        } else {
            btnUndoRef.current?.classList?.add(StyleConst.CLASS_ANIM_DEACTIVATED)
        }
    }

    /* ----------------- ASYNC Events ----------------- */

    fun addUndoState(taValue: String, immediate: Boolean) {
        if (immediate) {
            val tempTaValSS = ta_val_ss?.toMutableList() ?: mutableListOf<String>()
            if (tempTaValSS.size > 30) {
                tempTaValSS.removeFirst()
            }
            tempTaValSS += taValue
            setta_val_ss(tempTaValSS)
        } else {
            undoTimeoutRef.current?.let {
                clearTimeout(it)
            }
            undoTimeoutRef.current = setTimeout({
                val tempTaValSS = ta_val_ss?.toMutableList() ?: mutableListOf<String>()
                if (tempTaValSS.size > 30) {
                    tempTaValSS.removeFirst()
                }
                tempTaValSS += taValue
                setta_val_ss(tempTaValSS)
            }, 1000)
        }
    }

    fun checkCode(taValue: String, immediate: Boolean) {
        if (immediate) {
            setvc_rows(appLogic.getArch().check(taValue, exeStartLine).split("\n"))
            setCheckState(appLogic.getArch().state.getState())
        } else {
            checkTimeOutRef.current?.let {
                clearTimeout(it)
            }
            checkTimeOutRef.current = setTimeout({
                setvc_rows(appLogic.getArch().check(taValue, exeStartLine).split("\n"))
                setCheckState(appLogic.getArch().state.getState())
            }, 3000)
        }
    }

    fun preHighlight(taValue: String) {
        preHLTimeoutRef.current?.let {
            clearTimeout(it)
        }
        setvc_rows(taValue.split("\n"))
        preHLTimeoutRef.current = setTimeout({
            val hlTaList = appLogic.getArch().getPreHighlighting(taValue).split("\n")
            setvc_rows(hlTaList)
        }, 300)
    }

    fun preHighlight(taChangedRows: Map<Int, String>) {
        preHLTimeoutRef.current?.let {
            clearTimeout(it)
        }
        preHLTimeoutRef.current = setTimeout({
            val prevLines = vc_rows?.toMutableList()
            prevLines?.let {
                for (line in taChangedRows) {
                    prevLines[line.key] = appLogic.getArch().getPreHighlighting(line.value)
                }
            }
            setvc_rows(prevLines)
        }, 0)

    }

    /* ----------------- SYNC EVENTS ----------------- */

    fun undo() {
        appLogic.getArch().state.edit()
        setCheckState(appLogic.getArch().state.getState())
        val tempTaValSS = ta_val_ss?.toMutableList()
        tempTaValSS?.let {
            if (it.last() == ta_val) {
                it.removeLast()
            }
            setta_val(it.last())
            checkCode(it.last(), true)
            setta_val_ss(tempTaValSS)
        }

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

                    src = "icons/cpu-charge.svg"
                }


                onClick = {
                    if (transcriptView) {
                        setTranscriptView(!transcriptView)
                    } else {
                        if (appLogic.getArch().state.getState() == ArchConsts.STATE_BUILDABLE) {
                            textareaRef.current?.let {
                                checkCode(it.value, true)
                                setTranscriptView(!transcriptView)
                            }
                        }
                    }
                }
            }

            a {
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL)

                onClick = {
                    if (ta_val != null) {
                        checkCode(ta_val, true)
                    }
                }

                when (checkState) {
                    ArchConsts.STATE_UNCHECKED -> {
                        title = "Status: loading..."
                        img {
                            className = ClassName(StyleConst.CLASS_ANIM_ROTATION)
                            src = "icons/loading.svg"
                        }
                    }

                    ArchConsts.STATE_BUILDABLE -> {
                        title = "Status: ready to build"
                        img {
                            src = "icons/check.svg"
                        }
                    }

                    ArchConsts.STATE_HASERRORS -> {
                        title = "Status: fix errors!"
                        img {
                            src = "icons/error.svg"
                        }
                    }

                    ArchConsts.STATE_EXECUTION -> {
                        title = "Status: executing..."
                        img {
                            src = "icons/check.svg"
                        }
                    }

                    else -> {
                        title = "Status: loading..."
                        img {
                            className = ClassName(StyleConst.CLASS_ANIM_ROTATION)
                            src = "icons/loading.svg"
                        }
                    }
                }
            }

            a {
                id = "undo"
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL + " " + StyleConst.CLASS_ANIM_HOVER)
                ref = btnUndoRef
                title = "Undo"

                img {
                    src = "icons/undo.svg"
                }

                onClick = {
                    undo()
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
                        
                        Features
                        - Tab Handling
                        - Undo History
                        - Clear Button
                        - Code Highlighting
                        - Transcript Switch
                        - Start of Program (click on linenumber)
                        
                    
                """.trimIndent()

                img {
                    src = "icons/info.svg"
                }

            }

            a {
                id = "editor-clear"
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL + " " + StyleConst.CLASS_ANIM_HOVER)
                ref = btnClearRef
                title = "Clear"

                img {
                    src = "icons/clear.svg"
                }

                onClick = {
                    ta_val?.let {
                        addUndoState(ta_val, true)
                    }
                    setta_val("")
                    setvc_rows(listOf(""))
                }
            }
        }

        div {
            className = ClassName(StyleConst.CLASS_EDITOR_CONTAINER)

            if (transcriptView) {
                TranscriptView {
                    this.ta_val = ta_val ?: ""
                    this.transcript = appLogic.getArch().getTranscript()
                }
            } else {

                div {
                    className = ClassName(StyleConst.CLASS_EDITOR_SCROLL_CONTAINER)

                    div {
                        className = ClassName(StyleConst.CLASS_EDITOR_LINE_NUMBERS)
                        ref = lineNumbersRef

                        for (lineNumber in 1..lineNumbers) {
                            span {

                                if (lineNumber == exeStartLine) {
                                    className = ClassName(StyleConst.CLASS_EDITOR_LINE_ACTIVE)

                                    +"► $lineNumber"
                                    onClick = {
                                        setExeStartLine(ArchConsts.LINE_NOLINE)
                                    }
                                } else {
                                    onClick = {
                                        setExeStartLine(lineNumber)
                                    }
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

                            onChange = { event ->
                                setta_val(event.currentTarget.value)

                                addUndoState(event.currentTarget.value, false)

                                // Fire Change Events
                                val lines = event.currentTarget.value.split("\n")
                                if (vc_rows != null) {
                                    if (lines.size != vc_rows.size) {
                                        preHighlight(event.currentTarget.value)
                                    } else {
                                        val selStart = event.currentTarget.selectionStart ?: 0
                                        val selEnd =
                                            event.currentTarget.selectionEnd ?: (event.currentTarget.value.length - 1)
                                        val lineIDStart =
                                            event.currentTarget.value.substring(0, selStart).split("\n").size - 1
                                        val lineIDEnd =
                                            event.currentTarget.value.substring(0, selEnd).split("\n").size - 1
                                        val editedLines: MutableMap<Int, String> = mutableMapOf()
                                        for (lineID in lineIDStart..lineIDEnd) {
                                            editedLines.put(lineID, lines[lineID])
                                        }
                                        preHighlight(editedLines)
                                    }
                                } else {
                                    preHighlight(event.currentTarget.value)
                                }
                                //

                                appLogic.getArch().state.edit()
                                setCheckState(appLogic.getArch().state.getState())
                                checkCode(event.currentTarget.value, false)

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
                                    }
                                } else if (event.ctrlKey && event.key == "z") {
                                    undo()
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

    useEffect(update, transcriptView) {
        /* Component RELOAD */

        /* -- LOAD from localStorage -- */
        /* ta_val */
        setta_val(localStorage.getItem(StorageKey.TA_VALUE))
        textareaRef.current?.let {
            it.value = ta_val ?: ""
        }

        /* vc_rows */
        val vc_rows_length = localStorage.getItem(StorageKey.VC_ROW_Length)?.toInt() ?: 0
        if (vc_rows_length > 0) {
            val tempVcRows: MutableList<String> = mutableListOf()
            for (i in 0 until vc_rows_length) {
                val vcRowContent = localStorage.getItem(StorageKey.VC_ROW + i) ?: ""
                tempVcRows.add(i, vcRowContent)
            }
            setvc_rows(tempVcRows)
        }

        /* ta_val_ss */
        val ta_val_ss_length = localStorage.getItem(StorageKey.TA_VALUE_SaveState_Length)?.toInt() ?: 0
        if (ta_val_ss_length > 0) {
            val tempTaValSS: MutableList<String> = mutableListOf()
            for (i in 0 until ta_val_ss_length) {
                val taValSS = localStorage.getItem(StorageKey.TA_VALUE_SaveState + i) ?: ""
                tempTaValSS.add(i, taValSS)
            }
            setta_val_ss(tempTaValSS)
        }
    }

    useEffect(checkState) {
        when (checkState) {
            ArchConsts.STATE_BUILDABLE -> {
                btnSwitchRef.current?.let {
                    it.classList.remove(StyleConst.CLASS_ANIM_DEACTIVATED)
                }
            }

            else -> {
                btnSwitchRef.current?.let {
                    it.classList.add(StyleConst.CLASS_ANIM_DEACTIVATED)
                }
            }
        }
    }

    useEffect(exeStartLine) {
        textareaRef.current?.let {
            appLogic.getArch().check(it.value, exeStartLine)
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

    useEffect(vc_rows) {
        /* SAVE vc_rows change */
        if (vc_rows != null) {
            for (vcRowID in vc_rows.indices) {
                localStorage.setItem(StorageKey.VC_ROW + vcRowID, vc_rows[vcRowID])
            }
            localStorage.setItem(StorageKey.VC_ROW_Length, vc_rows.size.toString())
        }
    }

    useEffect(ta_val) {
        /* SAVE ta_val change */
        ta_val?.let {
            localStorage.setItem(StorageKey.TA_VALUE, it)
        }
        textareaRef.current?.let {
            it.value = ta_val ?: ""
        }
        updateTAResize()
        updateClearButton()
        updateLineNumbers()
    }

    useEffect(ta_val_ss) {
        /* SAVE ta_val_ss */
        if (ta_val_ss != null) {
            for (taValSSID in ta_val_ss.indices) {
                localStorage.setItem(StorageKey.TA_VALUE_SaveState + "$taValSSID", ta_val_ss[taValSSID])
            }
            localStorage.setItem(StorageKey.TA_VALUE_SaveState_Length, ta_val_ss.size.toString())
        }
        updateUndoButton()
    }


}