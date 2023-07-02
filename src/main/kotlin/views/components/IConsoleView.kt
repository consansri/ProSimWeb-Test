package views.components

import AppLogic
import StorageKey
import StyleConst
import csstype.*
import emotion.react.css
import extendable.components.connected.IConsole
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.js.timers.Timeout
import kotlinx.js.timers.clearInterval
import kotlinx.js.timers.setInterval
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import react.*
import react.dom.html.ButtonType
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.span

external interface IConsoleViewProps : Props {
    var appLogic: AppLogic
    var updateParent: (appLogic: AppLogic) -> Unit
    var update: StateInstance<Boolean>
    var footerRef: MutableRefObject<HTMLElement>

}

val IConsoleView = FC<IConsoleViewProps>() { props ->

    val consoleContainerRef = useRef<HTMLDivElement>()
    val scrollRef = useRef<HTMLDivElement>()
    val footerRef = props.footerRef

    val scrollIVRef = useRef<Timeout>(null)
    val contentIVRef = useRef<Timeout>(null)

    val appLogic by useState(props.appLogic)
    val update = props.update
    val (internalUpdate, setIUpdate) = useState(false)

    val (shadow, setShadow) = useState(false)
    val (scrollDown, setScrollDown) = useState(localStorage.getItem(StorageKey.CONSOLE_SDOWN)?.toBoolean() ?: true)
    val (pin, setPin) = useState(localStorage.getItem(StorageKey.CONSOLE_PIN)?.toBoolean() ?: false)

    val iConsole = appLogic.getArch().getConsole()

    contentIVRef.current?.let {
        clearInterval(it)
    }
    contentIVRef.current = setInterval({
        setIUpdate(!internalUpdate)
    }, 200)

    div {

        ref = consoleContainerRef

        css {
            display = Display.block
            backgroundColor = Color("#313131")
            padding = 10.px

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
                setIUpdate(!internalUpdate)
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

                backgroundColor = Color("#11dddd")

                filter = invert(100)

                transitionProperty = TransitionProperty.all
                transitionDuration = 0.2.s
                transitionTimingFunction = TransitionTimingFunction.easeInOut

                hover {
                    backgroundColor = Color("#00AAAA")
                }
            }

            onClick = {
                iConsole.clear()
            }

            src = StyleConst.Icons.delete


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



                if (pin) {
                    backgroundColor = Color("#777777")
                } else {
                    backgroundColor = Color("#AAAAAA")
                }
            }

            onClick = {
                setPin(!pin)
            }

            src = StyleConst.Icons.pin
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



                if (scrollDown) {
                    backgroundColor = Color("#777777")
                } else {
                    backgroundColor = Color("#AAAAAA")
                }
            }

            onClick = {
                setScrollDown(!scrollDown)
            }

            src = StyleConst.Icons.autoscroll
        }


        div {

            ref = scrollRef

            css {
                display = Display.block
                position = Position.relative
                marginTop = 5.px

                height = 10.rem
                overflowX = Overflow.hidden
                overflowY = Overflow.scroll

                scrollBehavior = ScrollBehavior.smooth

                transition = Transition.all
                transitionDuration = 0.3.s

                if (shadow) {
                    /*boxShadow = BoxShadow(BoxShadowInset.inset, 0.px, 7.px, 0.px, (-6).px, rgba(255,255,255,0.8))*/
                    borderTop = Border(1.px, LineStyle.solid, Color("#777777"))
                } else {
                    borderTop = null
                }
            }


            for (message in appLogic.getArch().getConsole().getMessages()) {
                for (line in message.message.split("\n")) {

                    span {
                        css {
                            display = Display.block
                            width = scrollRef.current?.clientWidth?.px ?: 100.pc
                            fontSize = 1.rem
                            fontFamily = FontFamily.monospace
                            when (message.type) {
                                StyleConst.MESSAGE_TYPE_INFO -> {
                                    color = Color("#777777")
                                }

                                StyleConst.MESSAGE_TYPE_LOG -> {
                                    color = Color("#AAAAAA")
                                }

                                StyleConst.MESSAGE_TYPE_WARN -> {
                                    color = Color("#FDDA0D")
                                }

                                StyleConst.MESSAGE_TYPE_ERROR -> {
                                    color = Color("#FF5733")
                                }

                                else -> {
                                    color = Color("#FFF")
                                }
                            }
                        }

                        +"> ${line}"
                    }
                }

            }



            onScroll = { event ->
                if (event.currentTarget.scrollTop <= 1.0) {
                    setShadow(false)
                } else {
                    setShadow(true)
                }
            }
        }

        document.body?.onresize = { event ->
            setIUpdate(!internalUpdate)
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

        useEffect(update) {
            console.log("(update) ConsoleView")
            scrollRef.current?.let {
                it.scrollTo(0.0, it.scrollHeight.toDouble())
            }

        }

        useEffect(internalUpdate) {
            // console.log("(update-internal) ConsoleView")
        }

    }
}