package me.c3.ui.theme.core.style

import java.awt.Color

data class IconStyle(
    val iconFgPrimary: Color,
    val iconFgSecondary: Color,
    val iconBgPrimary: Color? = null,
    val iconBgSecondary: Color? = null
)