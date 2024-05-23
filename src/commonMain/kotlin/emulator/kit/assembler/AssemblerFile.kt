package emulator.kit.assembler

data class AssemblerFile(val name: String, val content: String) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AssemblerFile) return false

        if (name != other.name) return false
        if (content != other.content) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + content.hashCode()
        return result
    }
}