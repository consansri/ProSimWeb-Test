package prosim.vfs

class RootDirectory : VFile {
    override val name: String = ""
    override val path: String = "/"
    override val isDirectory: Boolean = true
    override val parent: VFile? = null

    private val children = mutableListOf<VFile>()

    override fun getChildren(): List<VFile> = children.toList()

    override fun getContent(): ByteArray = ByteArray(0)

    override fun setContent(content: ByteArray) {
        throw UnsupportedOperationException()
    }
}