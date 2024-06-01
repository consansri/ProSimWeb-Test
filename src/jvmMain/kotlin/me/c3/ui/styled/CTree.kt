package me.c3.ui.styled

import me.c3.ui.manager.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.manager.ThemeManager
import me.c3.ui.resources.icons.ProSimIcons
import javax.swing.*
import javax.swing.tree.TreeModel

class CTree(  treeModel: TreeModel, fontType: FontType) : JTree(treeModel) {

    init {
        setUI(CTreeUI(  fontType))
    }
}