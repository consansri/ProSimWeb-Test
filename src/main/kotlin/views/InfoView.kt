package views

import AppLogic
import StyleConst
import csstype.ClassName
import csstype.Display
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
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

external interface InfoViewProps : Props {
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
    var updateParent: (newData: AppLogic) -> Unit

}

data class ViewState(val key: String, val id: Int) {
    val useRefButton: MutableRefObject<HTMLButtonElement> = useRef()
    val useRefScreen: MutableRefObject<HTMLDivElement> = useRef()
    var visible = false
}

val InfoView = FC<InfoViewProps> { props ->

    val appLogic by useState(props.appLogic)


    div {





    }

}