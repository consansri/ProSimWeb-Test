package prosim.uilib.styled.tree

import com.formdev.flatlaf.extras.FlatSVGIcon

interface NodeInformationProvider<T> {
    fun getIcon(userObject: T): FlatSVGIcon?
    fun getName(userObject: T): String?
}