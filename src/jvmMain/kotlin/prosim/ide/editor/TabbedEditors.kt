package prosim.ide.editor

import prosim.uilib.styled.CAdvancedTabPane

class TabbedEditors: CAdvancedTabPane(true, true, emptyMessage = "Open File through File Tree.") {

    val editors = mutableListOf<EditorComponent>()

    init {

    }


}