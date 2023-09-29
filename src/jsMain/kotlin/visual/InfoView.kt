package visual

import emulator.Emulator
import emotion.react.css
import web.html.*
import react.*
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import debug.DebugTools
import web.cssom.*
import StyleAttr.Main.InfoView
import emulator.kit.common.Docs
import emulator.kit.common.FileHandler
import js.core.asList
import js.promise.await
import kotlinx.coroutines.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.li
import web.events.EventType
import web.http.fetch
import web.window.window

external interface InfoViewProps : Props {
    var emulator: Emulator
    var update: StateInstance<Boolean>
    var updateParent: () -> Unit
    var footerRef: MutableRefObject<HTMLElement>
}

@OptIn(DelicateCoroutinesApi::class)
val InfoView = FC<InfoViewProps> { props ->

    val docDiv = useRef<HTMLDivElement>()

    val appLogic by useState(props.emulator)
    val (update, setUpdate) = useState(props.update)
    val (currMDID, setCurrMDID) = useState<Int>()

    var job: Job? = null

    div {
        IConsoleView {
            this.emulator = appLogic
            this.updateParent = props.updateParent
            this.update = update
            this.footerRef = props.footerRef
        }
        div {
            css {
                display = Display.flex
                flexDirection = FlexDirection.row
                padding = StyleAttr.paddingSize
                justifyContent = JustifyContent.center
                alignItems = AlignItems.center
                gap = StyleAttr.paddingSize
            }
            for (docID in appLogic.getArch().getDescription().docs.files.indices) {
                val doc = appLogic.getArch().getDescription().docs.files[docID]
                a {
                    css {
                        cursor = Cursor.pointer
                        borderRadius = StyleAttr.borderRadius
                        background = InfoView.Colors.base06.get()
                        padding = StyleAttr.paddingSize
                        color = important(InfoView.Colors.base00.get())
                    }

                    if (currMDID == docID) +"["
                    +doc.title
                    if (currMDID == docID) +"]"
                    onClick = {
                        if (currMDID != docID) {
                            setCurrMDID(docID)
                        } else {
                            setCurrMDID(null)
                        }

                    }
                }
            }
        }

        div {
            css {
                position = Position.relative
                display = Display.flex
                flexDirection = FlexDirection.column
                justifyContent = JustifyContent.start
                alignItems = AlignItems.center
                flexWrap = FlexWrap.wrap
                padding = StyleAttr.paddingSize
                overflowX = Overflow.scroll

                media(StyleAttr.responsiveQuery, {
                    alignItems = AlignItems.start
                })
            }
            ReactHTML.div {
                css {
                    whiteSpace = WhiteSpace.pre

                    ReactHTML.h1 {
                        fontSize = InfoView.fontSizeH1
                        marginTop = important(InfoView.marginTop)
                        marginBottom = important(InfoView.marginBottom)
                        marginLeft = important(0.rem)
                    }
                    ReactHTML.h2 {
                        fontSize = InfoView.fontSizeH2
                        marginTop = important(InfoView.marginTop)
                        marginBottom = important(InfoView.marginBottom)
                        marginLeft = important(0.rem)
                    }
                    ReactHTML.h3 {
                        fontSize = InfoView.fontSizeH3
                        marginTop = important(InfoView.marginTop)
                        marginBottom = important(InfoView.marginBottom)
                        marginLeft = important(0.rem)
                    }
                    ReactHTML.h4 {
                        fontSize = InfoView.fontSizeH4
                        marginTop = important(InfoView.marginTop)
                        marginBottom = important(InfoView.marginBottom)
                        marginLeft = important(0.rem)
                    }
                    ReactHTML.ul {
                        display = Display.flex
                        flexDirection = FlexDirection.column
                        justifyContent = JustifyContent.spaceEvenly
                        alignItems = AlignItems.stretch
                        gap = StyleAttr.paddingSize
                        paddingLeft = InfoView.tabSize

                        li {
                            display = Display.flex
                            flexDirection = FlexDirection.row
                            alignItems = AlignItems.center
                            justifyContent = JustifyContent.start
                            gap = StyleAttr.paddingSize
                            marginLeft = important(-InfoView.tabSize)
                            flexWrap = FlexWrap.nowrap

                            before {
                                content = Content("•")
                            }
                        }
                    }

                    ReactHTML.p {
                        fontWeight = FontWeight.lighter
                    }

                    ReactHTML.pre {
                        background = important(InfoView.Colors.Bg.get())
                        borderRadius = StyleAttr.borderRadius
                        padding = StyleAttr.paddingSize
                    }

                    ReactHTML.code {
                        StyleAttr.codeFont
                        cursor = Cursor.pointer
                    }

                    ReactHTML.table {
                        background = important(InfoView.Colors.TableBg.get())
                        color = StyleAttr.Main.FgColor.get()
                        width = important(Length.maxContent)

                        ReactHTML.td {
                            padding = StyleAttr.paddingSize
                        }
                        ReactHTML.th {
                            paddingRight = StyleAttr.paddingSize
                        }
                    }

                    img {
                        width = StyleAttr.iconSize
                        height = StyleAttr.iconSize
                        borderRadius = StyleAttr.borderRadius
                        filter = important(InfoView.iconFilter.get())
                    }

                    "> *" {
                        marginLeft = InfoView.tabSize
                    }

                    StyleAttr.layoutSwitchMediaQuery {
                        width = 100.pct
                        flexWrap = FlexWrap.wrap
                        overflowWrap = OverflowWrap.breakWord
                    }
                }
                ref = docDiv

                currMDID?.let { id ->
                    appLogic.getArch().getDescription().docs.files.getOrNull(id)?.let { file ->
                        if (file is Docs.HtmlFile.DefinedFile) {
                            file.fc {

                            }
                        }
                    }
                }
            }
        }
    }

    useEffect(update) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(update) InfoView")
        }
    }

    useEffect(currMDID) {
        if (currMDID != null) {
            val file = appLogic.getArch().getDescription().docs.files[currMDID]
            if (file is Docs.HtmlFile.SourceFile) {
                job = GlobalScope.launch {
                    val snippet = fetch(file.src).text()
                    docDiv.current?.let {
                        it.innerHTML = snippet.await()
                        val codeChilds = it.getElementsByTagName("code").asList()
                        for (child in codeChilds) {
                            child.addEventListener(EventType("click"), { event ->
                                child.textContent?.let { text ->
                                    if (appLogic.getArch().getFileHandler().getAllFiles().filter { it.getName() == "example" }.isEmpty()) {
                                        appLogic.getArch().getFileHandler().import(FileHandler.File("example", text))
                                        window.scrollTo(0, 0)
                                        props.updateParent()
                                        appLogic.getArch().getConsole().info("Successfully imported 'example'!")
                                    } else {
                                        child.classList.add(StyleAttr.ANIM_SHAKERED)
                                        web.timers.setTimeout({
                                            child.classList.remove(StyleAttr.ANIM_SHAKERED)
                                        }, 100)
                                        appLogic.getArch().getConsole().warn("Documentation couldn't import code example cause filename 'example' already exists!")
                                    }
                                }
                            })
                        }
                    }
                }
            }
        } else {
            docDiv.current?.let {
                it.innerHTML = ""
            }
        }

    }

}