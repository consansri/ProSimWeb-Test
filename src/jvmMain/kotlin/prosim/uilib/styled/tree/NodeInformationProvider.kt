package prosim.uilib.styled.tree

import com.formdev.flatlaf.extras.FlatSVGIcon
import java.awt.Color

interface NodeInformationProvider<T> {

    val expandedBranchIcon: FlatSVGIcon?
    val collapsedBranchIcon: FlatSVGIcon?
    val defaultLeafIcon: FlatSVGIcon?

    fun getIcon(userObject: T): FlatSVGIcon?
    fun getName(userObject: T): String?
    fun getFgColor(userObject: T): Color?
}