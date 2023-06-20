package views

import AppLogic
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import react.*
import react.dom.html.ReactHTML.div
import views.components.IConsoleView

external interface InfoViewProps : Props {
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
    var updateParent: (newData: AppLogic) -> Unit
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
        console.log("(update) InfoView")
    }

}