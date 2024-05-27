package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.CTreeUI
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import me.c3.ui.resources.icons.ProSimIcons
import javax.swing.*
import javax.swing.tree.TreeModel

class CTree(tm: ThemeManager, sm: ScaleManager, icons: ProSimIcons, treeModel: TreeModel, fontType: FontType) : JTree(treeModel) {

    init {
        setUI(CTreeUI(tm, sm, icons, fontType))
    }
}