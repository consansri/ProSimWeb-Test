package me.c3.uilib.state

interface WSConfigLoader<T> {
    fun loadFromConfig(wsConfig: WSConfig)

    fun updateConfig(value: T)
}