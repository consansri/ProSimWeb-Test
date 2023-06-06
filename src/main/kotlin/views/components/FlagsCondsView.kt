package views.components

import AppLogic
import extendable.ArchConst
import react.FC
import react.Props
import react.StateInstance
import react.dom.html.ReactHTML.a
import react.dom.html.ReactHTML.div
import react.useState

external interface FlagsCondsViewProps : Props {
    var name: String
    var appLogic: AppLogic
    var update: StateInstance<Boolean>
    var updateParent: (newData: AppLogic) -> Unit // Only update parent from a function which isn't changed from update prop (Infinite Loop)


}

val FlagsCondsView = FC<FlagsCondsViewProps>() { props ->
    val appLogic by useState(props.appLogic)
    val name by useState(props.name)
    val update = props.update

    if (appLogic.getArch().getFlagsConditions() != null) {
        div {

            div {
                a {
                    +props.name
                }
            }

            div {
                a {
                    +"Flags"
                }


            }

            div {
                a {
                    +"Conditions"
                }
            }


        }
    }

}