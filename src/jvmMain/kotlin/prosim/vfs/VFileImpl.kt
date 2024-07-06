package prosim.vfs

class VFileImpl(
    override val name: String,
    override val path: String,
    override val isDirectory: Boolean,
    override val parent: VFile
): VFile {
    val children = mutableListOf<VFile>()
    private var content: ByteArray = ByteArray(0)

    override fun getChildren(): List<VFile> = if(isDirectory) children.toList() else emptyList()
    override fun getContent(): ByteArray = if(isDirectory) ByteArray(0) else content
    override fun setContent(content: ByteArray) {
        if(!isDirectory) this.content = content
    }
}