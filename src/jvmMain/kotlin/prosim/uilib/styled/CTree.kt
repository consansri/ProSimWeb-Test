package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Font
import javax.swing.JTree
import javax.swing.border.Border
import javax.swing.tree.TreeModel

class CTree(treeModel: TreeModel, val fontType: FontType) : JTree(treeModel) {

    init {
        setUI(CTreeUI())
    }

    override fun getFont(): Font {
        return try {
            fontType.getFont()
        } catch (e: NullPointerException){
            super.getFont()
        }
    }

    override fun getBackground(): Color {
        return UIStates.theme.get().globalLaF.bgSecondary
    }

    override fun getForeground(): Color {
        return UIStates.theme.get().textLaF.base
    }

    override fun getBorder(): Border {
        return UIStates.scale.get().borderScale.getInsetBorder()
    }
}