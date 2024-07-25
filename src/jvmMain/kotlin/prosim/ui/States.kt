package prosim.ui

import emulator.Link
import emulator.kit.Architecture
import emulator.kit.optional.SetupSetting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import prosim.ui.impl.WSBehaviourImpl
import prosim.uilib.UIStates
import prosim.uilib.state.Manager
import prosim.uilib.state.WSConfig
import prosim.uilib.state.WSEditor
import prosim.uilib.state.WSLogger
import prosim.uilib.workspace.Workspace

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
                    it.classType.simpleName.toString() == value
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
            set(Workspace(path, WSBehaviourImpl.ASM, editor, logger))
            logger?.log("")
        }
    }

    fun loadConfig(wsConfig: WSConfig) {
        arch.loadFromConfig(wsConfig)
        UIStates.icon.loadFromConfig(wsConfig)
        UIStates.theme.loadFromConfig(wsConfig)
        UIStates.scale.loadFromConfig(wsConfig)
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