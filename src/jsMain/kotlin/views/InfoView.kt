package views

import AppLogic
import emotion.react.css
import web.html.*
import react.*
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import tools.DebugTools
import views.components.IConsoleView
import web.cssom.*
import StyleConst.Main.InfoView
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.br
import react.dom.html.ReactHTML.img
import react.dom.html.ReactHTML.li
import react.dom.html.ReactHTML.pre
import react.dom.html.ReactHTML.ul

external interface InfoViewProps : Props {
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
    var updateParent: () -> Unit
    var footerRef: MutableRefObject<HTMLElement>
}

val InfoView = FC<InfoViewProps> { props ->

    val docDiv = useRef<HTMLDivElement>()

    val appLogic by useState(props.appLogic)
    var (update, setUpdate) = useState(props.update)
    var (internalUpdate, setIUpdate) = useState(false)
    val (currMDID, setCurrMDID) = useState<Int>()


    div {
        IConsoleView {
            this.appLogic = appLogic
            this.updateParent = props.updateParent
            this.update = update
            this.footerRef = props.footerRef
        }
        div {
            css {
                display = Display.flex
                flexDirection = FlexDirection.row
                padding = StyleConst.paddingSize
                justifyContent = JustifyContent.center
                alignItems = AlignItems.center
                gap = StyleConst.paddingSize
            }
            for (docID in appLogic.getArch().getDocs().files.indices) {
                val doc = appLogic.getArch().getDocs().files[docID]
                a {
                    css {
                        cursor = Cursor.pointer
                        borderRadius = StyleConst.borderRadius
                        background = InfoView.Colors.base06.get()
                        padding = StyleConst.paddingSize
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
                display = Display.flex
                flexDirection = FlexDirection.column
                justifyContent = JustifyContent.start
                alignItems = AlignItems.center
                flexWrap = FlexWrap.wrap
                padding = StyleConst.paddingSize
                overflowX = Overflow.scroll

                media(StyleConst.responsiveQuery, {
                    alignItems = AlignItems.start
                })
            }
            div {
                css {
                    whiteSpace = WhiteSpace.pre
                    overflowX = Overflow.scroll

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
                        gap = StyleConst.paddingSize
                        paddingLeft = InfoView.tabSize

                        li {
                            display = Display.flex
                            flexDirection = FlexDirection.row
                            alignItems = AlignItems.center
                            justifyContent = JustifyContent.start
                            gap = StyleConst.paddingSize
                            marginLeft = important(-InfoView.tabSize)

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
                        borderRadius = StyleConst.borderRadius
                        padding = StyleConst.paddingSize
                    }

                    ReactHTML.code {
                        StyleConst.codeFont
                    }

                    ReactHTML.table {
                        background = important(InfoView.Colors.TableBg.get())
                        color = StyleConst.Main.FgColor.get()
                        width = important(Length.maxContent)

                        ReactHTML.td {
                            padding = StyleConst.paddingSize
                        }
                        ReactHTML.th {
                            paddingRight = StyleConst.paddingSize
                        }
                    }

                    img {
                        width = StyleConst.iconSize
                        height = StyleConst.iconSize
                        borderRadius = StyleConst.borderRadius
                        filter = important(InfoView.iconFilter.get())
                    }

                    "> *" {
                        marginLeft = InfoView.tabSize
                    }

                }
                ref = docDiv
            }
        }
    }

    useEffect(update) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(update) InfoView")
        }
    }

    useEffect(currMDID) {
        docDiv.current?.let {
            if (currMDID != null) {
                it.innerHTML = appLogic.getArch().getDocs().files[currMDID].htmlContent
            } else {
                it.innerHTML = ""
            }
        }
    }

}