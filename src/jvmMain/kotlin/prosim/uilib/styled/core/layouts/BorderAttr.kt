package prosim.uilib.styled.core.layouts

sealed class BorderAttr:Attribute() {

    data object CENTER: BorderAttr()
    data object NORTH: BorderAttr()
    data object SOUTH: BorderAttr()
    data object EAST: BorderAttr()
    data object WEST: BorderAttr()

}