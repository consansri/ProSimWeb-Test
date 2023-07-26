package extendable.components.connected

import tools.DebugTools

class FileHandler(val fileEnding: String) {

    private val files = mutableListOf<File>()
    private var currentID: Int = 0

    fun initFiles(files: List<File>) {
        if (files.isEmpty()) {
            this.files.add(File("common." + fileEnding, ""))
        } else {
            this.files.addAll(files)
        }
    }

    fun import(file: File) {
        this.files.add(file)
        if (DebugTools.ARCH_showFileHandlerInfo) {
            console.log("FileHandler: import file ${file.getName()}\n\t${file.getContent().replace("\n", "\n\t")}")
        }
        refreshLocalStorage()
    }

    fun remove(file: File) {
        this.files.remove(file)
        refreshLocalStorage()
    }

    fun edit(content: String) {
        files[currentID].setContent(content)
        refreshLocalStorage()
    }

    fun setCurrent(index: Int) {
        currentID = if (index in files.indices) index else 0
    }

    fun getCurrent(): File {
        return files[currentID]
    }

    fun getAllFiles(): List<File> {
        return files
    }

    private fun refreshLocalStorage() {

    }

    data class File(private var name: String, private var content: String) {
        fun rename(newName: String) {
            name = newName
        }

        fun getName(): String {
            return name
        }

        fun setContent(content: String) {
            this.content = content
        }

        fun getContent(): String {
            return this.content
        }

    }

}