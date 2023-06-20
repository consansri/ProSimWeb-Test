package views.components

import AppLogic
import StorageKey
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
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
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

        button {

            css {
                display = Display.inlineBlock
                float = Float.right
                paddingLeft = 5.px
                paddingRight = 5.px

                color = Color("#313131")
                borderRadius = 5.px

                if (pin) {
                    backgroundColor = Color("#AAAAAA")
                } else {
                    backgroundColor = Color("#777777")
                }
            }

            type = ButtonType.button

            onClick = {
                setPin(!pin)
            }

            +"pin"
        }

        button {

            css {
                display = Display.inlineBlock
                float = Float.right
                paddingLeft = 5.px
                paddingRight = 5.px

                color = Color("#313131")
                borderRadius = 5.px

                if (scrollDown) {
                    backgroundColor = Color("#AAAAAA")
                } else {
                    backgroundColor = Color("#777777")
                }
            }

            type = ButtonType.button

            onClick = {
                setScrollDown(!scrollDown)
            }

            +"auto scroll"
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