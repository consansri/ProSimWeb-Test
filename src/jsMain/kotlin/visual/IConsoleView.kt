package visual

import StorageKey
import StyleAttr
import emotion.react.css
import kotlinx.browser.document
import kotlinx.browser.localStorage
import react.*
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.span
import debug.DebugTools
import emulator.kit.Architecture
import emulator.kit.common.IConsole

import web.html.*
import web.timers.*
import web.cssom.*

external interface IConsoleViewProps : Props {
    var archState: StateInstance<Architecture>
    var footerRef: MutableRefObject<HTMLElement>
    var compileEventState: StateInstance<Boolean>
    var exeEventState: StateInstance<Boolean>
}

val IConsoleView = FC<IConsoleViewProps>() { props ->

    val consoleContainerRef = useRef<HTMLDivElement>()
    val scrollRef = useRef<HTMLDivElement>()
    val footerRef = props.footerRef

    val scrollIVRef = useRef<Timeout>(null)

    val (messages, setMessages) = useState(props.archState.component1().getConsole().getMessages())

    val iConsole = props.archState.component1().getConsole()

    val (shadow, setShadow) = useState(false)
    val (scrollDown, setScrollDown) = useState(localStorage.getItem(StorageKey.CONSOLE_SDOWN)?.toBoolean() ?: true)
    val (pin, setPin) = useState(localStorage.getItem(StorageKey.CONSOLE_PIN)?.toBoolean() ?: false)
    val (showLog, setShowLog) = useState(localStorage.getItem(StorageKey.CONSOLE_SHOWINFO)?.toBoolean() ?: false)

    div {

        ref = consoleContainerRef

        css {
            display = Display.block
            backgroundColor = Color("#313131")
            padding = 10.px
            fontFamily = FontFamily.monospace

            if (pin) {
                position = Position.fixed
                document.body?.let {
                    width = it.clientWidth.px
                }
                left = 0.px
                consoleContainerRef.current?.let {
                    top = 100.vh - it.offsetHeight.px
                }
                zIndex = integer(10)
                boxShadow = BoxShadow(0.px, 0.px, 1.rem, Color("#000"))


            } else {
                borderRadius = 0.3.rem
                position = Position.relative
            }
        }

        a {
            css {
                color = Color("#BBBBBB")
                fontSize = 1.2.rem
            }

            onClick = { event ->

            }

            +iConsole.name
        }

        img {

            css {
                display = Display.inlineBlock
                cursor = Cursor.pointer
                float = Float.right
                padding = 0.1.rem
                width = 1.8.rem
                height = 1.8.rem
                marginLeft = 0.4.rem
                borderRadius = 5.px
                backgroundColor = Color("#AAAAAA")
                filter = invert(100)
                transitionProperty = TransitionProperty.all
                transitionDuration = 0.2.s
                transitionTimingFunction = TransitionTimingFunction.easeInOut

                hover {
                    backgroundColor = Color("#999999")
                }
            }

            onClick = {
                props.archState.component1().getConsole().clear()
                setMessages(emptyList())
            }

            src = StyleAttr.Icons.delete_black
        }

        img {

            css {
                display = Display.inlineBlock
                cursor = Cursor.pointer
                float = Float.right
                padding = 0.1.rem
                width = 1.8.rem
                height = 1.8.rem
                marginLeft = 0.4.rem
                borderRadius = 5.px
                filter = invert(100)

                backgroundColor = if (pin) {
                    Color("#999999")
                } else {
                    Color("#AAAAAA")
                }
            }

            onClick = {
                setPin(!pin)
            }

            src = StyleAttr.Icons.pin
        }

        img {

            css {
                display = Display.inlineBlock
                cursor = Cursor.pointer
                float = Float.right
                padding = 0.1.rem
                width = 1.8.rem
                height = 1.8.rem
                marginLeft = 0.4.rem
                borderRadius = 5.px
                filter = invert(100)

                backgroundColor = if (scrollDown) {
                    Color("#999999")
                } else {
                    Color("#AAAAAA")
                }
            }

            onClick = {
                setScrollDown(!scrollDown)
            }

            src = StyleAttr.Icons.autoscroll
        }

        img {
            css {
                display = Display.inlineBlock
                cursor = Cursor.pointer
                float = Float.right
                padding = 0.1.rem
                width = 1.8.rem
                height = 1.8.rem
                marginLeft = 0.4.rem
                borderRadius = 5.px
                filter = invert(100)

                backgroundColor = if (showLog) {
                    Color("#999999")
                } else {
                    Color("#AAAAAA")
                }
            }
            onClick = {
                setShowLog(!showLog)
            }
            src = StyleAttr.Icons.info
        }


        div {

            ref = scrollRef

            css {
                display = Display.block
                position = Position.relative
                marginTop = 5.px
                height = 15.rem
                overflowX = Overflow.hidden
                overflowY = Overflow.scroll
                scrollBehavior = ScrollBehavior.smooth
                transition = Transition.all
                transitionDuration = 0.3.s

                borderTop = if (shadow) {
                    /*boxShadow = BoxShadow(BoxShadowInset.inset, 0.px, 7.px, 0.px, (-6).px, rgba(255,255,255,0.8))*/
                    Border(1.px, LineStyle.solid, Color("#777777"))
                } else {
                    null
                }
            }

            for (message in messages) {
                val lines = message.message.split("\n")
                for (lineID in lines.indices) {
                    if (message.type == IConsole.MSGType.LOG && !showLog) {
                        continue
                    }

                    span {
                        css {
                            display = Display.block
                            width = scrollRef.current?.clientWidth?.px ?: 100.pc
                            fontSize = 1.rem
                            fontFamily = FontFamily.monospace
                            whiteSpace = WhiteSpace.pre
                            when (message.type) {
                                IConsole.MSGType.INFO -> {
                                    color = Color("#AAAAAA")
                                }

                                IConsole.MSGType.LOG -> {
                                    color = Color("#888888")
                                }

                                IConsole.MSGType.WARNING -> {
                                    color = Color("#FDDA0D")
                                }

                                IConsole.MSGType.ERROR -> {
                                    color = Color("#FF5733")
                                }

                                else -> {
                                    color = Color("#FFF")
                                }
                            }
                        }
                        tabIndex = 4

                        +((if (lineID == 0) "" else "\t") + lines[lineID])
                    }

                }

            }

            var prevScrollPos = scrollRef.current?.scrollTop

            onScroll = { event ->
                val scrollTop = event.currentTarget.scrollTop

                prevScrollPos?.let {
                    if (scrollTop < it) {
                        setScrollDown(false)
                    }
                }

                if (scrollTop > event.currentTarget.scrollHeight - event.currentTarget.clientHeight - 20) {
                    setScrollDown(true)
                }

                if (scrollTop <= 1.0) {
                    setShadow(false)
                } else {
                    setShadow(true)
                }

                prevScrollPos = scrollTop
            }
        }

        useEffect(pin) {
            if (pin) {
                val heightPx: Int
                consoleContainerRef.current?.let {
                    heightPx = it.offsetHeight
                    footerRef.current?.let { it1 ->
                        it1.style.marginBottom = "${heightPx}px"
                    }
                }
            } else {
                footerRef.current?.let { it1 ->
                    it1.style.marginBottom = "0"
                }
            }
            localStorage.setItem(StorageKey.CONSOLE_PIN, pin.toString())
        }

        useEffect(showLog) {
            localStorage.setItem(StorageKey.CONSOLE_PIN, showLog.toString())
        }

        useEffect(scrollDown) {
            if (scrollDown) {
                scrollIVRef.current = setInterval({
                    scrollRef.current?.let {
                        it.scrollTo(0.0, it.scrollHeight.toDouble())
                    }
                }, 500)
            } else {
                scrollIVRef.current?.let {
                    clearInterval(it)
                }
            }
            localStorage.setItem(StorageKey.CONSOLE_SDOWN, scrollDown.toString())
        }

        useEffect(document.body?.onresize) {
            if (DebugTools.REACT_showUpdateInfo) {
                iConsole.log("REACT: Resize ConsoleView updated!")
            }
            scrollRef.current?.let {
                it.scrollTo(0.0, it.scrollHeight.toDouble())
            }
        }

        useEffect(props.exeEventState, props.compileEventState, props.archState.component1().getConsole().getMessages()) {
            setMessages(props.archState.component1().getConsole().getMessages())
        }

    }
}