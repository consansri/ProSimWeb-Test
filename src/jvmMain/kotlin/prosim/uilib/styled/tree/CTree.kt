package prosim.uilib.styled.tree

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
        return UIStates.theme.get().COLOR_BG_1
    }

    override fun getForeground(): Color {
        return UIStates.theme.get().COLOR_FG_0
    }

    override fun getBorder(): Border {
        return UIStates.scale.get().BORDER_INSET_MEDIUM
    }
}