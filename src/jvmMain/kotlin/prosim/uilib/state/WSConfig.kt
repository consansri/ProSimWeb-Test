package prosim.uilib.state

import debug.DebugTools
import emulator.kit.nativeLog
import java.io.File

data class WSConfig(val file: File, val onChange: (File) -> Unit) {

    private val settings: MutableList<Setable> = mutableListOf()

    init {
        load()
    }

    fun get(type: String, id: String): String? = search(type, id)?.value

    fun set(type: String, id: String, value: String) {
        search(type, id)?.let {
            it.value = value
            store()
            return
        }

        settings.add(Setable(type, id, value))
        store()
    }

    private fun load() {
        settings.clear()
        if (file.exists()) {
            val content = file.readText().split("\n")
            settings.addAll(content.mapNotNull {
                parseln(it)
            })
        }
        if (DebugTools.JVM_showWSConfigInfo) nativeLog("LoadConfig: {\n${settings.toContentString()}\n}")
    }

    private fun store() {
        if (!file.exists()) {
            file.createNewFile()
        }
        val content = settings.toContentString()
        if (DebugTools.JVM_showWSConfigInfo) nativeLog("StoreConfig: {\n$content\n}")
        file.writeText(content)
        onChange(file)
    }

    private fun parseln(line: String): Setable? {
        val result = setableRegex.find(line) ?: return null

        val (all, typeStr, id, value) = result.groupValues

        if (DebugTools.JVM_showWSConfigInfo) nativeLog("Parsing: $line -> type: $typeStr, id: $id, val: $value")

        return Setable(typeStr, id, value)
    }

    private fun search(type: String, id: String): Setable? = settings.firstOrNull { it.type == type && it.id == id }

    private fun MutableList<Setable>.toContentString(): String = this.joinToString("\n") { "${it.type}.${it.id}=${it.value}" }

    inner class Setable(val type: String, val id: String, var value: String)

    companion object {
        val setableRegex = Regex("""\s*(\S+)\.(\S+)\s*=\s*(\S+)\s*""")
    }

}