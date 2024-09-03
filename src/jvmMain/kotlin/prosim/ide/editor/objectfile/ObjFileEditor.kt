package prosim.ide.editor.objectfile

import cengine.lang.asm.elf.ELFReader
import cengine.vfs.FPath
import cengine.vfs.VirtualFile
import com.formdev.flatlaf.extras.FlatSVGIcon
import prosim.ide.editor.EditorComponent
import prosim.uilib.styled.CScrollPane
import prosim.uilib.styled.CSplitPane
import prosim.uilib.styled.CTextArea
import prosim.uilib.styled.params.FontType
import java.awt.Component
import javax.swing.JSplitPane

class ObjFileEditor(val file: VirtualFile) : EditorComponent() {

    companion object {
        val fileSuffix = ".o"
    }

    val byteArea = CTextArea(FontType.CODE, true)
    val infoArea = CTextArea(FontType.CODE, true)
    val splitPane = CSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, byteArea, infoArea, primary = true)

    override val component: Component = createScrollPane()
    override val title: String = file.name
    override val icon: FlatSVGIcon? = null
    override val tooltip: String = file.path.toString(FPath.DELIMITER)
    var chunkSize = 8
    val mode: Mode? = Mode.identify(file)


    init {
        byteArea.isEditable = false
        infoArea.isEditable = false
        formatContent()
        component.revalidate()
        component.repaint()
    }

    private fun formatContent() {

        when (mode) {
            Mode.ELF -> {
                val content = file.getContent().toList().chunked(chunkSize).mapIndexed { index, bytes -> index to bytes }
                byteArea.text = content.joinToString("\n") { indexedLine ->
                    indexedLine.first.toULong().toString(16).padStart(8, '0') +
                            "    " +
                            indexedLine.second.joinToString(" ") { it.toUByte().toString(16).padStart(2, '0') }
                }

                val reader = ELFReader(file.getContent())
                infoArea.text = reader.ehdr.toString()
            }

            null -> {
                val content = file.getContent().toList().chunked(chunkSize).mapIndexed { index, bytes -> index to bytes }
                byteArea.text = content.joinToString("\n") { indexedLine ->
                    indexedLine.first.toULong().toString(16).padStart(8, '0') +
                            "    " +
                            indexedLine.second.joinToString(" ") { it.toUByte().toString(16).padStart(2, '0') }
                }
                infoArea.text = content.joinToString("\n") { it.second.toByteArray().decodeToString() }
            }
        }
    }


    private fun createScrollPane(): CScrollPane {
        return CScrollPane(splitPane, true)
    }


    enum class Mode {
        ELF;

        companion object {
            fun identify(file: VirtualFile): Mode? {
                try {
                    ELFReader(file.getContent())
                    return ELF
                } catch (e: Exception) {

                }

                return null
            }
        }
    }

}