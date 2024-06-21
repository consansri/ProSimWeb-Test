package prosim.uilib.styled

import prosim.uilib.styled.params.FontType
import javax.swing.*
import javax.swing.tree.TreeModel

class CTree(  treeModel: TreeModel, fontType: FontType) : JTree(treeModel) {

    init {
        setUI(prosim.uilib.styled.CTreeUI(fontType))
    }
}