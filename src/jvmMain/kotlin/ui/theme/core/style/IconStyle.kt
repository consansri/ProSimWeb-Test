package me.c3.ui.theme.core.style

import java.awt.Color

data class IconStyle(
    val iconFgPrimary: Color,
    val iconFgSecondary: Color,
    val iconBg: Color = Color(0,0,0,0),
    val iconBgHover: Color = Color(0,0,0,0),
    val iconBgActive: Color = Color(0,0,0,0),
    val iconDeactivatedAlpha: Int = 125
)