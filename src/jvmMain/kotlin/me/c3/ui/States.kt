package me.c3.ui

import emulator.Link
import emulator.kit.Architecture
import emulator.kit.nativeLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.c3.ui.Res.icons
import me.c3.ui.Res.scalings
import me.c3.ui.Res.themes
import me.c3.ui.state.Manager
import me.c3.ui.resources.icons.BenIcons
import me.c3.ui.resources.icons.ProSimIcons
import me.c3.ui.scale.core.Scaling
import me.c3.ui.scale.scalings.LargeScaling
import me.c3.ui.scale.scalings.LargerScaling
import me.c3.ui.scale.scalings.SmallScaling
import me.c3.ui.scale.scalings.StandardScaling
import me.c3.ui.theme.core.Theme
import me.c3.ui.theme.themes.DarkTheme
import me.c3.ui.theme.themes.LightTheme
import me.c3.ui.workspace.WSConfig
import me.c3.ui.workspace.WSEditor
import me.c3.ui.workspace.WSLogger
import me.c3.ui.workspace.Workspace
import java.nio.file.Paths

object States {
    val ws = object : Manager<Workspace?>(null) {
        override fun loadFromConfig(wsConfig: WSConfig) {
            // nothing
        }

        override fun updateConfig(value: Workspace?) {
            // nothing
        }

        override fun onChange() {
            val config = get()?.config ?: return
            loadConfig(config)
        }
    }

    val arch = object : Manager<Architecture>(Link.RV32I.load()) {
        override fun loadFromConfig(wsConfig: WSConfig) {
            wsConfig.get(WSConfig.Type.IDE, Keys.IDE_ARCH)?.let { value ->
                setConfigNotChanged(Link.entries.firstOrNull {
                    it.classType().simpleName.toString() == value
                }?.load() ?: Link.RV32I.load())
            }
        }

        override fun updateConfig(value: Architecture) {
            ws.get()?.config?.set(WSConfig.Type.IDE, Keys.IDE_ARCH, value::class.simpleName.toString())
        }
    }

    val icon = object : Manager<ProSimIcons>(BenIcons()) {
        override fun loadFromConfig(wsConfig: WSConfig) {
            wsConfig.get(WSConfig.Type.IDE, Keys.IDE_ICONS)?.let { value ->
                icons.firstOrNull { it.name == value }?.let {
                    setConfigNotChanged(it)
                }
            }
        }

        override fun updateConfig(value: ProSimIcons) {
            ws.get()?.config?.set(WSConfig.Type.IDE, Keys.IDE_ICONS, value.name)
        }
    }

    val theme = object : Manager<Theme>(LightTheme()) {
        override fun loadFromConfig(wsConfig: WSConfig) {
            wsConfig.get(WSConfig.Type.IDE, Keys.IDE_THEME)?.let { value ->
                themes.firstOrNull { it.name == value }?.let {
                    setConfigNotChanged(it)
                }
            }
        }

        override fun updateConfig(value: Theme) {
            ws.get()?.config?.set(WSConfig.Type.IDE, Keys.IDE_THEME, value.name)
        }
    }

    val scale = object : Manager<Scaling>(StandardScaling()) {
        override fun loadFromConfig(wsConfig: WSConfig) {
            wsConfig.get(WSConfig.Type.IDE, Keys.IDE_SCALE)?.let { value ->
                scalings.firstOrNull { it.name == value }?.let {
                    setConfigNotChanged(it)
                }
            }
        }

        override fun updateConfig(value: Scaling) {
            ws.get()?.config?.set(WSConfig.Type.IDE, Keys.IDE_SCALE, value.name)
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


}