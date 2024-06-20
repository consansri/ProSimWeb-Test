package me.c3.ui

import emulator.Link
import emulator.kit.Architecture
import emulator.kit.optional.SetupSetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.c3.ui.Res.icons
import me.c3.ui.Res.scalings
import me.c3.ui.Res.themes
import me.c3.uilib.resource.BenIcons
import me.c3.uilib.resource.Icons
import me.c3.uilib.scale.core.Scaling
import me.c3.uilib.scale.scalings.StandardScaling
import me.c3.uilib.state.Manager
import me.c3.uilib.theme.core.Theme
import me.c3.uilib.theme.themes.LightTheme
import me.c3.uilib.state.WSConfig
import me.c3.uilib.state.WSEditor
import me.c3.uilib.state.WSLogger
import me.c3.ui.workspace.Workspace

object States {
    val ws = object : Manager<Workspace?>(null) {
        override fun loadFromConfig(wsConfig: WSConfig) {
            // nothing
        }

        override fun updateConfig(value: Workspace?) {
            // nothing
        }

        override fun onChange(value: Workspace?) {
            value ?: return
            loadConfig(value.config)
        }
    }

    val arch = object : Manager<Architecture>(Link.RV32I.load()) {
        init {
            get().loadArchSettings()
        }

        override fun loadFromConfig(wsConfig: WSConfig) {
            wsConfig.get(Keys.IDE, Keys.IDE_ARCH)?.let { value ->
                setConfigNotChanged(Link.entries.firstOrNull {
                    it.classType().simpleName.toString() == value
                }?.load() ?: Link.RV32I.load())
            }
        }

        override fun updateConfig(value: Architecture) {
            ws.get()?.config?.set(Keys.IDE, Keys.IDE_ARCH, value::class.simpleName.toString())
        }

        override fun onChange(value: Architecture) {
            value.loadArchSettings()
        }
    }

    val icon = object : Manager<Icons>(BenIcons()) {
        override fun loadFromConfig(wsConfig: WSConfig) {
            wsConfig.get(Keys.IDE, Keys.IDE_ICONS)?.let { value ->
                icons.firstOrNull { it.name == value }?.let {
                    setConfigNotChanged(it)
                }
            }
        }

        override fun updateConfig(value: Icons) {
            ws.get()?.config?.set(Keys.IDE, Keys.IDE_ICONS, value.name)
        }
    }

    val theme = object : Manager<Theme>(LightTheme()) {
        override fun loadFromConfig(wsConfig: WSConfig) {
            wsConfig.get(Keys.IDE, Keys.IDE_THEME)?.let { value ->
                themes.firstOrNull { it.name == value }?.let {
                    setConfigNotChanged(it)
                }
            }
        }

        override fun updateConfig(value: Theme) {
            ws.get()?.config?.set(Keys.IDE, Keys.IDE_THEME, value.name)
        }
    }

    val scale = object : Manager<Scaling>(StandardScaling()) {
        override fun loadFromConfig(wsConfig: WSConfig) {
            wsConfig.get(Keys.IDE, Keys.IDE_SCALE)?.let { value ->
                scalings.firstOrNull { it.name == value }?.let {
                    setConfigNotChanged(it)
                }
            }
        }

        override fun updateConfig(value: Scaling) {
            ws.get()?.config?.set(Keys.IDE, Keys.IDE_SCALE, value.name)
        }
    }

    /**
     * Extension Functions
     */
    /**
     * Sets the current workspace to a new path and updates the bottom bar.
     * @param path The new workspace path.
     */
    fun Manager<Workspace?>.setFromPath(path: String, editor: WSEditor?, logger: WSLogger?) {
        logger?.log("Switching Workspace ($path)")
        CoroutineScope(Dispatchers.Default).launch {
            set(Workspace(path, editor, logger))
            logger?.log("")
        }
    }

    fun loadConfig(wsConfig: WSConfig) {
        arch.loadFromConfig(wsConfig)
        icon.loadFromConfig(wsConfig)
        theme.loadFromConfig(wsConfig)
        scale.loadFromConfig(wsConfig)
    }

    fun SetupSetting<*>.save(arch: Architecture) {
        ws.get()?.config?.set(arch::class.simpleName.toString(), this.trimmedName, this.valueToString())
    }

    fun Architecture.loadArchSettings() {
        ws.get()?.config?.let { ws ->
            this.settings.forEach {
                val result = ws.get(arch.get()::class.simpleName.toString(), it.trimmedName)
                if (result != null) {
                    it.loadFromString(this, result)
                }
            }
        }
    }


}