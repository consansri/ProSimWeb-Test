package me.c3.uilib

import me.c3.ui.Keys
import me.c3.ui.States
import me.c3.uilib.resource.BenIcons
import me.c3.uilib.resource.Icons
import me.c3.uilib.scale.core.Scaling
import me.c3.uilib.scale.scalings.StandardScaling
import me.c3.uilib.state.Manager
import me.c3.uilib.state.WSConfig
import me.c3.uilib.theme.core.Theme
import me.c3.uilib.theme.themes.LightTheme

object UIStates {
    val icon = object : Manager<Icons>(BenIcons()) {
        override fun loadFromConfig(wsConfig: WSConfig) {
            wsConfig.get(Keys.IDE, Keys.IDE_ICONS)?.let { value ->
                UIResource.icons.firstOrNull { it.name == value }?.let {
                    setConfigNotChanged(it)
                }
            }
        }

        override fun updateConfig(value: Icons) {
            States.ws.get()?.config?.set(Keys.IDE, Keys.IDE_ICONS, value.name)
        }
    }

    val theme = object : Manager<Theme>(LightTheme()) {
        override fun loadFromConfig(wsConfig: WSConfig) {
            wsConfig.get(Keys.IDE, Keys.IDE_THEME)?.let { value ->
                UIResource.themes.firstOrNull { it.name == value }?.let {
                    setConfigNotChanged(it)
                }
            }
        }

        override fun updateConfig(value: Theme) {
            States.ws.get()?.config?.set(Keys.IDE, Keys.IDE_THEME, value.name)
        }
    }

    val scale = object : Manager<Scaling>(StandardScaling()) {
        override fun loadFromConfig(wsConfig: WSConfig) {
            wsConfig.get(Keys.IDE, Keys.IDE_SCALE)?.let { value ->
                UIResource.scalings.firstOrNull { it.name == value }?.let {
                    setConfigNotChanged(it)
                }
            }
        }

        override fun updateConfig(value: Scaling) {
            States.ws.get()?.config?.set(Keys.IDE, Keys.IDE_SCALE, value.name)
        }
    }
}