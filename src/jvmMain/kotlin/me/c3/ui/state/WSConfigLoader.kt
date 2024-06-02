package me.c3.ui.state

import me.c3.ui.workspace.WSConfig

interface WSConfigLoader<T> {
    fun loadFromConfig(wsConfig: WSConfig)

    fun updateConfig(value: T)
}