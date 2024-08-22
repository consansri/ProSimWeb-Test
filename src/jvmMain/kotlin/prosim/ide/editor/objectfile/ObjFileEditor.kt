package prosim.ide.editor.objectfile

import cengine.lang.asm.elf.ELFReader
import cengine.lang.asm.elf.elf32.ELF32_Ehdr
import cengine.lang.asm.elf.elf32.ELF32_Shdr
import cengine.lang.asm.elf.elf64.ELF64_Ehdr
import cengine.lang.asm.elf.elf64.ELF64_Shdr
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
    override val tooltip: String = file.path
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
                val reader = ELFReader(file.getContent())
                val content = file.getContent().toList().chunked(chunkSize).mapIndexed { index, bytes -> index to bytes }
                byteArea.text = content.joinToString("\n") { indexedLine ->
                    indexedLine.first.toULong().toString(16).padStart(8, '0') +
                            "    " +
                            indexedLine.second.joinToString(" ") { it.toUByte().toString(16).padStart(2, '0') } +
                            "    " +
                            indexedLine.second.toByteArray().decodeToString()
                }
                val phoff: ULong
                val shoff: ULong

                val elfStructure: Map<ULong, String> = when (reader.ehdr) {
                    is ELF32_Ehdr -> {
                        phoff = reader.ehdr.e_phoff.toULong()
                        shoff = reader.ehdr.e_shoff.toULong()
                        mapOf(
                            0UL to "ELF32 Header",
                            phoff to "Program Headers",
                            shoff to "Section Headers"
                        )
                    }

                    is ELF64_Ehdr -> {
                        phoff = reader.ehdr.e_phoff
                        shoff = reader.ehdr.e_shoff
                        mapOf(
                            0UL to "ELF64 Header",
                            phoff to "Program Headers",
                            shoff to "Section Headers"
                        )
                    }

                    else -> mapOf()
                }

                val sections: Map<ULong, String> = reader.sectionHeaders.associate {
                    when (it) {
                        is ELF32_Shdr -> {
                            it.sh_offset.toULong() to reader.getSectionName(it)
                        }

                        is ELF64_Shdr -> {
                            it.sh_offset to reader.getSectionName(it)
                        }

                        else -> 0UL to "Invalid Section"
                    }
                }

                var infoContent = ""
                for (line in (0UL until reader.buffer.size.toULong()).chunked(chunkSize)) {
                    val lineContent = "${elfStructure.toList().filter { it.first in line }.joinToString(", ") { it.second }}${sections.toList().filter { it.first in line }.joinToString(", ") { it.second }}\n"
                    infoContent += lineContent
                }
                infoArea.text = infoContent
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