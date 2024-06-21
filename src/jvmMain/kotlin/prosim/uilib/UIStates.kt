package prosim.uilib

import prosim.uilib.resource.BenIcons
import prosim.uilib.resource.Icons
import prosim.uilib.scale.core.Scaling
import prosim.uilib.scale.scalings.StandardScaling
import prosim.uilib.state.Manager
import prosim.uilib.state.WSConfig
import prosim.uilib.theme.core.Theme
import prosim.uilib.theme.themes.LightTheme
import prosim.ui.Keys
import prosim.ui.States

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