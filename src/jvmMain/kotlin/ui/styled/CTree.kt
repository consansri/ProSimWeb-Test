package me.c3.ui.components.styled

import me.c3.ui.spacing.ScaleManager
import me.c3.ui.styled.CTreeUI
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.icons.ProSimIcons
import javax.swing.*
import javax.swing.tree.TreeModel

class CTree(themeManager: ThemeManager, scaleManager: ScaleManager, icons: ProSimIcons, treeModel: TreeModel, fontType: FontType) : JTree(treeModel) {

    init {
        setUI(CTreeUI(themeManager, scaleManager, icons, fontType))
    }
}