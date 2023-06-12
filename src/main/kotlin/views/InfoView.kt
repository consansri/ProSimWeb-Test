package views

import AppLogic
import StyleConst
import csstype.ClassName
import csstype.Display
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.HTMLElement
import react.*
import react.dom.html.ButtonType
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.button
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.tbody
import react.dom.html.ReactHTML.th
import react.dom.html.ReactHTML.thead
import react.dom.html.ReactHTML.tr
import views.components.ConsoleView

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
    var (update, setUpdate) = useState( props.update)
    var (internalUpdate, setIUpdate) = useState(false)


    div {
        ConsoleView{
            this.appLogic = appLogic
            this.updateParent = props.updateParent
            this.update = update
            this.footerRef = props.footerRef
        }
    }

    useEffect(update){
        console.log("(update) InfoView")
    }

}