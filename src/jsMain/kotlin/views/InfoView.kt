package views

import AppLogic
import web.html.*
import react.*
import react.dom.html.ReactHTML.div
import tools.DebugTools
import views.components.IConsoleView

external interface InfoViewProps : Props {
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
    var updateParent: () -> Unit
    var footerRef: MutableRefObject<HTMLElement>
}

data class ViewState(val key: String, val id: Int) {
    val useRefButton: MutableRefObject<HTMLButtonElement> = useRef()
    val useRefScreen: MutableRefObject<HTMLDivElement> = useRef()
    var visible = false
}

val InfoView = FC<InfoViewProps> { props ->

    val appLogic by useState(props.appLogic)
    var (update, setUpdate) = useState(props.update)
    var (internalUpdate, setIUpdate) = useState(false)


    div {
        IConsoleView {
            this.appLogic = appLogic
            this.updateParent = props.updateParent
            this.update = update
            this.footerRef = props.footerRef
        }
    }

    useEffect(update) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("(update) InfoView")
        }
    }

}