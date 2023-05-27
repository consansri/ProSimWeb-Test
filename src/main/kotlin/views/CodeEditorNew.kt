package views

import AppLogic
import StorageKey
import csstype.ClassName
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.w3c.dom.*
import react.*
import react.dom.DangerouslySetInnerHTML
import react.dom.aria.ariaHidden
import react.dom.html.AutoComplete
import react.dom.html.ReactHTML
import kotlin.js.Date
import kotlin.time.Duration.Companion.milliseconds

external interface CodeEditorNewProps : Props {
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
    var updateParent: (newData: AppLogic) -> Unit
}


val CodeEditorNew = FC<CodeEditorNewProps> { props ->

    /* ----------------- STATIC VARIABLES ----------------- */

    val lineHeight = 21

    /* ----------------- TIMER VARIABLES ----------------- */

    var timer: Int = 0

    /* ----------------- REACT REFERENCES ----------------- */

    val textareaRef = useRef<HTMLTextAreaElement>(null)
    val lineNumbersRef = useRef<HTMLDivElement>(null)
    val btnClearRef = useRef<HTMLAnchorElement>(null)
    val btnUndoRef = useRef<HTMLAnchorElement>(null)
    val inputDivRef = useRef<HTMLDivElement>(null)
    val codeAreaRef = useRef<HTMLElement>(null)

    /* ----------------- REACT STATES ----------------- */

    var data by useState(props.appLogic)
    var update = props.update

    /* ----------------- PERFORMANCE STATES ----------------- */

    var (visibleItems, setVisibleItems) = useState<MutableList<Element>>()

    /* ----------------- localStorage Sync Objects ----------------- */

    var (vc_rows, setvc_rows) = useState<List<String>>()

    var (ta_val, setta_val) = useState<String>()
    var (ta_val_ss, setta_val_ss) = useState<MutableList<String>>()


    /* ----------------- UPDATE VISUAL COMPONENTS ----------------- */

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

    fun updateClearButton() {
        textareaRef.current?.let {
            if (it.value != "") {
                btnClearRef.current?.style?.display = "block"
            } else {
                btnClearRef.current?.style?.display = "none"
            }
        }
    }

    /* ----------------- USEEFFECTS (Save and Reload from localStorage) ----------------- */

    useEffect(update) {
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

    useEffect(vc_rows) {
        console.log("useEffect ${timer}: vc_rows START ${Date().getTime().milliseconds}")
        /* SAVE vc_rows change */
        if (vc_rows != null) {
            for (vcRowID in vc_rows.indices) {
                localStorage.setItem(StorageKey.VC_ROW + vcRowID, vc_rows[vcRowID])
            }
            localStorage.setItem(StorageKey.VC_ROW_Length, vc_rows.size.toString())
        }
        console.log("useEffect ${timer}: vc_rows END ${Date().getTime().milliseconds}")
    }

    useEffect(ta_val) {
        console.log("useEffect ${timer}: ta_val START ${Date().getTime().milliseconds}")
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
        console.log("useEffect ${timer}: ta_val END ${Date().getTime().milliseconds}")
    }

    useEffect(ta_val_ss) {
        /* SAVE ta_val_ss */
        if (ta_val_ss != null) {
            for (taValSSID in ta_val_ss.indices) {
                localStorage.setItem(StorageKey.TA_VALUE_SaveState + taValSSID, ta_val_ss[taValSSID])
            }
            localStorage.setItem(StorageKey.TA_VALUE_SaveState_Length, ta_val_ss.size.toString())
        }
    }

    /* ----------------- EVENTS ----------------- */

    fun onContentChange(tachangedRows: Map<Int, String>) {
        console.log("onContentChange START ${Date().getTime().milliseconds}")
        val prevLines = vc_rows?.toMutableList()
        prevLines?.let {
            for (line in tachangedRows) {
                prevLines[line.key] = data.getArch().getPreHighlighting(line.value)
            }
        }
        setvc_rows(prevLines)
        console.log("onContentChange END ${Date().getTime().milliseconds}")
    }

    fun onLengthChange(taValue: String) {
        console.log("onLengthChange START ${Date().getTime().milliseconds}")
        val hlTaList = data.getArch().getPostHighlighting(taValue).split("\n")
        setvc_rows(hlTaList)
        console.log("onLengthChange END ${Date().getTime().milliseconds}")
    }

    /* ----------------- DOM ----------------- */

    ReactHTML.div {
        className = ClassName(StyleConst.CLASS_EDITOR)

        ReactHTML.div {
            className = ClassName(StyleConst.CLASS_EDITOR_CONTROLS)

            ReactHTML.a {
                id = "build"
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL)
                ReactHTML.img {
                    src = "icons/cpu-charge.svg"
                }

            }

            ReactHTML.a {
                id = "undo"
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL)
                ref = btnUndoRef

                ReactHTML.img {
                    src = "icons/undo.svg"
                }

                onClick = {

                }
            }

            ReactHTML.a {
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL)
                id = StyleConst.STR_EDITOR_CONTROL_CHECK
                ReactHTML.img {
                    src = "icons/exclamation-mark2.svg"
                }
            }

            ReactHTML.a {
                id = "editor-clear"
                className = ClassName(StyleConst.CLASS_EDITOR_CONTROL)
                ref = btnClearRef
                ReactHTML.img {
                    src = "icons/clear.svg"
                }

                onClick = {
                    textareaRef.current?.let {
                        setta_val("")
                        setvc_rows(listOf(""))

                    }
                }
            }
        }

        ReactHTML.div {
            className = ClassName(StyleConst.CLASS_EDITOR_CONTAINER)


            ReactHTML.div {
                className = ClassName(StyleConst.CLASS_EDITOR_SCROLL_CONTAINER)

                /*onScroll = { event ->
                    val containerRect = event.currentTarget.getBoundingClientRect()
                    val children = event.currentTarget.children
                    val tempVisibleItems: MutableList<Element> = mutableListOf()

                    for (childID in 0 until children.length) {
                            val childRect = children.get(childID)?.getBoundingClientRect()
                            childRect?.let {
                                if (it.top >= containerRect.top && it.bottom <= containerRect.bottom) {
                                    if (children[childID] != null) {
                                        children[childID]?.let { it1 -> tempVisibleItems.add(it1) }
                                    }
                                }
                            }
                    }
                    setVisibleItems(tempVisibleItems)
                }*/

                ReactHTML.div {
                    className = ClassName(StyleConst.CLASS_EDITOR_LINE_NUMBERS)
                    ref = lineNumbersRef
                    ReactHTML.span {

                    }
                }

                ReactHTML.div {
                    className = ClassName(StyleConst.CLASS_EDITOR_INPUT_DIV)
                    ref = inputDivRef

                    ReactHTML.textarea {
                        className = ClassName(StyleConst.CLASS_EDITOR_AREA)
                        ref = textareaRef
                        autoComplete = AutoComplete.off
                        autoCorrect = "off"
                        autoCapitalize = "off"
                        spellCheck = false
                        placeholder = "Enter ${data.getArch().getName()} Assembly ..."


                        onChange = { event ->
                            console.log("Change Event ${timer}: START ${Date().getTime().milliseconds}")
                            timer += 1

                            setta_val(event.currentTarget.value)

                            console.log("Change Event ${timer}: setta_val ${Date().getTime().milliseconds}")

                            // Fire Change Events
                            val lines = event.currentTarget.value.split("\n")
                            if (vc_rows != null) {
                                if (lines.size != vc_rows.size) {
                                    onLengthChange(event.currentTarget.value)
                                } else {
                                    val selStart = event.currentTarget.selectionStart ?: 0
                                    val selEnd =
                                        event.currentTarget.selectionEnd ?: (event.currentTarget.value.length - 1)
                                    val lineIDStart =
                                        event.currentTarget.value.substring(0, selStart).split("\n").size - 1
                                    val lineIDEnd = event.currentTarget.value.substring(0, selEnd).split("\n").size - 1
                                    val editedLines: MutableMap<Int, String> = mutableMapOf()
                                    for (lineID in lineIDStart..lineIDEnd) {
                                        editedLines.put(lineID, lines[lineID])
                                    }
                                    onContentChange(editedLines)
                                }
                            } else {
                                onLengthChange(event.currentTarget.value)
                            }
                            //

                            console.log("Change Event ${timer}: END ${Date().getTime().milliseconds}}")


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


                                }
                            } else if (event.key == "Backspace") {
                                textareaRef.current?.let {

                                }
                            } else if (event.ctrlKey && event.key == "z") {

                            }
                        }
                    }

                    ReactHTML.pre {
                        className = ClassName(StyleConst.CLASS_EDITOR_HIGHLIGHTING)
                        ariaHidden = true

                        ReactHTML.code {
                            className = ClassName(StyleConst.CLASS_EDITOR_HIGHLIGHTING_LANGUAGE)
                            className = ClassName(StyleConst.CLASS_EDITOR_HIGHLIGHTING_CONTENT)
                            ref = codeAreaRef

                            vc_rows?.let {
                                console.log("VC Rerender START ${Date().getTime().milliseconds}")
                                var contentString = ""
                                for (i in it.indices) {
                                    contentString += "${it[i]}\n"
                                }
                                codeAreaRef.current?.let {
                                    it.innerHTML = contentString
                                }
                                console.log("VC Rerender END ${Date().getTime().milliseconds}")
                            }


                        }
                    }

                }
            }
        }
    }


}