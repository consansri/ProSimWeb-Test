package prosim.vfs

/**
 *
 */
interface VFile {
    val name: String
    val path: String
    val isDirectory: Boolean
    val parent: VFile?
    fun getChildren(): List<VFile>
    fun getContent(): ByteArray
    fun setContent(content: ByteArray)
}