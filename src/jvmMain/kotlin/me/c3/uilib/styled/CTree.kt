package me.c3.uilib.styled

import me.c3.uilib.styled.params.FontType
import javax.swing.*
import javax.swing.tree.TreeModel

class CTree(  treeModel: TreeModel, fontType: FontType) : JTree(treeModel) {

    init {
        setUI(CTreeUI(  fontType))
    }
}