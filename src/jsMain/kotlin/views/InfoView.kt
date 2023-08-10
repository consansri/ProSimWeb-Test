package views

import AppLogic
import emotion.react.css
import web.html.*
import react.*
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.html
import tools.DebugTools
import views.components.IConsoleView
import web.cssom.*
import StyleConst.Main.InfoView

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
                padding = StyleConst.paddingSize
            }
            div {
                className = ClassName(InfoView.CLASS_MD_STYLE)
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