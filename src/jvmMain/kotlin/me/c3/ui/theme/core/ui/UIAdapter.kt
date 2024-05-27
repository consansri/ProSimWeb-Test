package me.c3.ui.theme.core.ui

import me.c3.ui.scale.ScaleManager
import me.c3.ui.theme.ThemeManager

interface UIAdapter {
    fun setupUI(tm: ThemeManager, sm: ScaleManager)

    fun setDefaults(tm: ThemeManager, sm: ScaleManager)
}