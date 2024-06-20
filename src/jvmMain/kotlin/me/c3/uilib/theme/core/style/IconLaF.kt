package me.c3.uilib.theme.core.style

import java.awt.Color

data class IconLaF(
    val iconFgPrimary: Color,
    val iconFgSecondary: Color,
    val iconBg: Color = Color(0,0,0,0),
    val iconBgHover: Color = Color(0,0,0,0),
    val iconBgActive: Color = Color(0,0,0,0),
    val iconFgInactive: Color = Color(0x77777733, true)
)