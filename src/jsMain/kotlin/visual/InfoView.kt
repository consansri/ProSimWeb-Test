package visual

import emotion.react.css
import web.html.*
import react.*
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import web.cssom.*
import StyleAttr.Main.InfoView
import emulator.kit.common.Docs
import emulator.kit.optional.FileHandler
import js.core.asList
import js.promise.await
import kotlinx.coroutines.*
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.li
import visual.StyleExt.render
import web.events.EventType
import web.http.fetch
import web.window.window

external interface InfoViewProps : Props {
    var archState: StateInstance<emulator.kit.Architecture>
    var fileState: StateInstance<FileHandler>
    var footerRef: MutableRefObject<HTMLElement>
    var compileEventState: StateInstance<Boolean>
    var exeEventState: StateInstance<Boolean>
    var fileChangeEvent: StateInstance<Boolean>

}

@OptIn(DelicateCoroutinesApi::class)
val InfoView = FC<InfoViewProps> { props ->

    val docDiv = useRef<HTMLDivElement>()

    val arch = props.archState.component1()
    val (currMDID, setCurrMDID) = useState<Int>()

    div {
        IConsoleView {
            this.archState = props.archState
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
                flexWrap = FlexWrap.wrap
            }
            for (docID in arch.getDescription().docs.files.indices) {
                val doc = arch.getDescription().docs.files[docID]
                a {
                    css {
                        cursor = Cursor.pointer
                        borderRadius = StyleAttr.borderRadius
                        background = InfoView.Colors.base07.get()
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

                media(StyleAttr.responsiveQuery) {
                    alignItems = AlignItems.start
                }
            }
            ReactHTML.div {
                css {
                    whiteSpace = WhiteSpace.preWrap

                    ReactHTML.h1 {
                        fontSize = InfoView.fontSizeH1
                        marginTop = important(InfoView.marginTopH1)
                        marginBottom = important(InfoView.marginBottom)
                        marginLeft = important(0.rem)
                        color = StyleAttr.Main.InfoView.Colors.base03.get()
                    }
                    ReactHTML.h2 {
                        fontSize = InfoView.fontSizeH2
                        marginTop = important(InfoView.marginTopH1)
                        marginBottom = important(InfoView.marginBottom)
                        marginLeft = important(0.rem)
                        textAlign = TextAlign.center
                        color = StyleAttr.Main.InfoView.Colors.base02.get()
                    }
                    ReactHTML.h3 {
                        fontSize = InfoView.fontSizeH3
                        marginTop = important(InfoView.marginTop)
                        marginBottom = important(InfoView.marginBottom)
                        marginLeft = important(0.rem)
                        color = StyleAttr.Main.InfoView.Colors.base01.get()
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
                            textAlign = TextAlign.center
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
                        flexWrap = FlexWrap.wrap
                        overflowWrap = OverflowWrap.breakWord
                    }
                }
                ref = docDiv

                currMDID?.let { id ->
                    arch.getDescription().docs.files.getOrNull(id)?.let { file ->
                        if (file is Docs.DocFile.DefinedFile) {
                            file.chapters.forEach {
                                val fc = it.render(props.archState.component1(), props.fileState.component1(), props.fileChangeEvent)
                                fc {

                                }
                            }
                        }
                    }
                }
            }
        }
    }

    useEffect(currMDID) {
        if (currMDID != null) {
            val file = arch.getDescription().docs.files[currMDID]
            if (file is Docs.DocFile.SourceFile) {
                GlobalScope.launch {
                    val snippet = fetch(file.src).text()
                    docDiv.current?.let { input ->
                        input.innerHTML = snippet.await()
                        val codeChilds = input.getElementsByTagName("code").asList()
                        for (child in codeChilds) {
                            child.addEventListener(EventType("click"), { event ->
                                child.textContent?.let { text ->
                                    if (props.fileState.component1().getAllFiles().none { it.getName() == "example" }) {
                                        props.fileState.component1().import(FileHandler.File("example", text))
                                        window.scrollTo(0, 0)
                                        arch.getConsole().info("Successfully imported 'example'!")
                                        props.fileChangeEvent.component2().invoke(!props.fileChangeEvent.component1())
                                    } else {
                                        child.classList.add(StyleAttr.ANIM_SHAKERED)
                                        web.timers.setTimeout({
                                            child.classList.remove(StyleAttr.ANIM_SHAKERED)
                                        }, 100)
                                        arch.getConsole().warn("Documentation couldn't import code example cause filename 'example' already exists!")
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