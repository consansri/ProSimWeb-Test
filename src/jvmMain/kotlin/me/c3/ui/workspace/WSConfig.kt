package me.c3.ui.workspace

import java.io.File

data class WSConfig(val file: File) {

    private val settings: MutableList<Setable> = mutableListOf()

    init {
        load()
    }

    fun get(type: Type, id: String): String? = search(type, id)?.value

    fun set(type: Type, id: String, value: String) {
        search(type, id)?.let {
            it.value = value
            return
        }

        settings.add(Setable(type, id, value))
    }

    private fun load() {
        settings.clear()
        if (file.exists()) {
            val content = file.readText().split("\n")
            settings.addAll(content.mapNotNull {
                parseln(it)
            })
        }
    }

    private fun store() {
        if (!file.exists()) {
            file.createNewFile()
        }
        val content = settings.joinToString("\n") { "${it.type}.${it.id}=${it.value}" }
        file.writeText(content)
    }

    private fun parseln(line: String): Setable? {
        val result = setableRegex.find(line) ?: return null

        val (typeStr, id, value) = result.groupValues

        val type = Type.entries.firstOrNull { it.toString() == typeStr } ?: return null

        return Setable(type, id, value)
    }

    private fun search(type: Type, id: String): Setable? = settings.firstOrNull { it.type == type && it.id == id }

    inner class Setable(val type: Type, val id: String, value: String) {
        var value: String = value
            set(value) {
                field = value
                store()
            }
    }

    enum class Type {
        IDE,
        ARCH;

        override fun toString(): String {
            return this.name.lowercase()
        }
    }

    companion object {
        val setableRegex = Regex("""\s*(\S+)\.(\S+)\s*=\s*(\S+)\s*""")
    }

}