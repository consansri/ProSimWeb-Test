package me.c3.ui.scale.core.components

import java.awt.Dimension
import javax.swing.BorderFactory
import javax.swing.border.Border

data class ControlScale(
    val normalSize: Int,
    val smallSize: Int,
    val normalInset: Int,
    val smallInset: Int,
    val cornerRadius: Int,
    val comboBoxWidth: Int
){
    fun getNormalInsetBorder(): Border = BorderFactory.createEmptyBorder(normalInset, normalInset, normalInset, normalInset)
    fun getSmallInsetBorder(): Border = BorderFactory.createEmptyBorder(smallInset, smallInset, smallInset, smallInset)
    fun getSmallSize() = Dimension(smallSize, smallSize)
    fun getNormalSize() = Dimension(normalSize, normalSize)
}