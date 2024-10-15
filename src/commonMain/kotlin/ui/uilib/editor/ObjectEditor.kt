package ui.uilib.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import cengine.lang.asm.elf.ELFReader
import cengine.lang.asm.elf.E_IDENT
import cengine.lang.asm.elf.Ehdr
import cengine.lang.asm.elf.Shdr
import cengine.lang.asm.elf.elf32.ELF32_Ehdr
import cengine.lang.asm.elf.elf32.ELF32_Shdr
import cengine.lang.asm.elf.elf64.ELF64_Ehdr
import cengine.lang.asm.elf.elf64.ELF64_Shdr
import cengine.project.Project
import cengine.vfs.VirtualFile
import emulator.kit.nativeLog
import kotlinx.coroutines.Job
import ui.uilib.UIState
import ui.uilib.interactable.CToggle
import ui.uilib.params.FontType

@Composable
fun ObjectEditor(
    file: VirtualFile,
    project: Project,
    modifier: Modifier = Modifier
) {
    val scale = UIState.Scale.value

    val fileContent by remember {
        mutableStateOf(file.getContent())
    }

    var elfReader by remember {
        mutableStateOf(
            try {
                ELFReader(file.name, fileContent)
            } catch (e: Exception) {
                null
            }
        )
    }

    val scrollVertical = rememberScrollState()
    val scrollHorizontal = rememberScrollState()

    val coroutineScope = rememberCoroutineScope()
    var processJob by remember { mutableStateOf<Job?>(null) }

    Box(
        modifier
            .fillMaxSize()
            .verticalScroll(scrollVertical)
    ) {
        elfReader?.let { elfReader ->
            Column(
                Modifier
                    .padding(scale.SIZE_INSET_MEDIUM)
                    .fillMaxWidth()
            ) {

                // Draw Header Info
                ELFHeaderInfos(elfReader, fileContent)

                // Draw Sections wrapped in ProgramHeaders
                ELFSectionInfos(elfReader, fileContent)
            }
        }

        if (elfReader == null) ByteRange(fileContent, fileContent.indices, 16)

    }

    LaunchedEffect(fileContent) {
        elfReader = try {
            ELFReader(file.name, fileContent)
        } catch (e: Exception) {
            null
        }
    }
}

@Composable
fun ELFHeaderInfos(elfReader: ELFReader, fileContent: ByteArray) {
    val theme = UIState.Theme.value
    val scale = UIState.Scale.value

    val codeStyle = FontType.CODE.getFamily()
    val titleStyle = FontType.LARGE.getFamily()
    val baseStyle = FontType.MEDIUM.getFamily()

    val ehdr = elfReader.ehdr
    val e_ident = ehdr.e_ident

    var showBytes by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.COLOR_BG_1, RoundedCornerShape(scale.SIZE_CORNER_RADIUS))
    ) {

        Text(
            "Header",
            modifier = Modifier
                .fillMaxWidth()
                .padding(scale.SIZE_INSET_MEDIUM),
            textAlign = TextAlign.Center,
            color = theme.COLOR_FG_0,
            fontFamily = titleStyle
        )

        Box(
            Modifier
                .background(theme.COLOR_BORDER)
                .fillMaxWidth()
                .height(scale.SIZE_BORDER_THICKNESS)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Class:",
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_1,
                fontFamily = codeStyle
            )

            Text(
                E_IDENT.getElfClass(e_ident.ei_class),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Data:",
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_1,
                fontFamily = codeStyle
            )

            Text(
                E_IDENT.getElfData(e_ident.ei_data),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Version:",
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_1,
                fontFamily = codeStyle
            )

            Text(
                e_ident.ei_version.toString(),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "OS/ABI:",
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_1,
                fontFamily = codeStyle
            )

            Text(
                E_IDENT.getOsAbi(e_ident.ei_osabi),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "ABI Version:",
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_1,
                fontFamily = codeStyle
            )

            Text(
                e_ident.ei_abiversion.toString(),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Type:",
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_1,
                fontFamily = codeStyle
            )

            Text(
                Ehdr.getELFType(ehdr.e_type),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Machine:",
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_1,
                fontFamily = codeStyle
            )

            Text(
                Ehdr.getELFMachine(ehdr.e_machine),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Version:",
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_1,
                fontFamily = codeStyle
            )

            Text(
                ehdr.e_version.toString(),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Entry point address:",
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_1,
                fontFamily = codeStyle
            )

            Text(
                when (ehdr) {
                    is ELF32_Ehdr -> "0x" + ehdr.e_entry.toString(16)
                    is ELF64_Ehdr -> "0x" + ehdr.e_entry.toString(16)
                    else -> "(invalid)"
                },
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Start of program headers:",
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_1,
                fontFamily = codeStyle
            )

            Text(
                when (ehdr) {
                    is ELF32_Ehdr -> "0x" + ehdr.e_phoff.toString(16)
                    is ELF64_Ehdr -> "0x" + ehdr.e_phoff.toString(16)
                    else -> "(invalid)"
                } + " (bytes into file)",
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Start of section headers:",
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_1,
                fontFamily = codeStyle
            )

            Text(
                when (ehdr) {
                    is ELF32_Ehdr -> "0x" + ehdr.e_shoff.toString(16)
                    is ELF64_Ehdr -> "0x" + ehdr.e_shoff.toString(16)
                    else -> "(invalid)"
                } + " (bytes into file)",
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Flags:",
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_1,
                fontFamily = codeStyle
            )

            Text(
                "0x" + ehdr.e_flags.toString(16),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Size of this header:",
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_1,
                fontFamily = codeStyle
            )

            Text(
                ehdr.e_ehsize.toString() + " (bytes)",
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Size of program headers:",
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_1,
                fontFamily = codeStyle
            )

            Text(
                ehdr.e_phentsize.toString() + " (bytes)",
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Number of program headers:",
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_1,
                fontFamily = codeStyle
            )

            Text(
                ehdr.e_phnum.toString(),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Size of section headers:",
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_1,
                fontFamily = codeStyle
            )

            Text(
                ehdr.e_shentsize.toString() + " (bytes)",
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Number of section headers:",
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_1,
                fontFamily = codeStyle
            )

            Text(
                ehdr.e_phnum.toString(),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Section header string table index:",
                textAlign = TextAlign.Right,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_1,
                fontFamily = codeStyle
            )

            Text(
                "0x" + ehdr.e_shstrndx.toString(16),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle
            )
        }

        Box(
            Modifier
                .background(theme.COLOR_BORDER)
                .fillMaxWidth()
                .height(scale.SIZE_BORDER_THICKNESS)
        )

        CToggle(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                showBytes = it
            }, initialToggle = showBytes, icon = if (showBytes) {
                UIState.Icon.value.close
            } else {
                UIState.Icon.value.add
            }
        )

        if (showBytes) {
            Box(
                Modifier
                    .background(theme.COLOR_BORDER)
                    .fillMaxWidth()
                    .height(scale.SIZE_BORDER_THICKNESS)
            )

            ByteRange(fileContent, 0..<ehdr.e_ehsize.toInt(), 16)
        }
    }
}

@Composable
fun ELFSectionInfos(elfReader: ELFReader, fileContent: ByteArray) {

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value
    val codeStyle = FontType.CODE.getFamily()
    val baseStyle = FontType.MEDIUM.getFamily()

    nativeLog("SectionHeaders: ${elfReader.sectionHeaders}")

    elfReader.sectionHeaders.forEach {
        Spacer(
            Modifier.height(scale.SIZE_INSET_MEDIUM)
        )

        Column(
            Modifier
                .fillMaxWidth()
                .background(theme.COLOR_BG_1, RoundedCornerShape(scale.SIZE_CORNER_RADIUS))
        ) {

            val name = elfReader.getSectionName(it)
            val type = Shdr.getSectionType(it.sh_type)
            val flags = Shdr.getSectionFlags(
                when (it) {
                    is ELF32_Shdr -> it.sh_flags.toULong()
                    is ELF64_Shdr -> it.sh_flags
                    else -> 0U
                }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "$name - $type - $flags",
                    color = theme.COLOR_FG_0,
                    fontFamily = baseStyle,
                    modifier = Modifier
                        .padding(scale.SIZE_INSET_MEDIUM),
                )
            }

            val range = when (it) {
                is ELF32_Shdr -> {
                    it.sh_offset.toInt()..<(it.sh_offset + it.sh_size).toInt()
                }

                is ELF64_Shdr -> {
                    it.sh_offset.toInt()..<(it.sh_offset + it.sh_size).toInt()
                }

                else -> null
            }

            if (range != null) {

                Box(
                    Modifier
                        .background(theme.COLOR_BORDER)
                        .fillMaxWidth()
                        .height(scale.SIZE_BORDER_THICKNESS)
                )

                ByteRange(fileContent, range, 16)
            }
        }
    }
}

@Composable
fun ByteRange(byteArray: ByteArray, range: IntRange, chunkSize: Int) {

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value
    val codeStyle = FontType.CODE.getFamily()
    val baseStyle = FontType.MEDIUM.getFamily()

    val chunks = range.chunked(chunkSize).map { byteArray.slice(it) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        for ((chunkIndex, chunk) in chunks.withIndex()) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Address representation (e.g., "00000000:")
                val address = (range.first + chunkIndex * chunkSize).toString(16).uppercase()
                Text(
                    text = address,
                    fontFamily = codeStyle,
                    color = theme.COLOR_FG_1,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.weight(0.5f)
                )

                Spacer(modifier = Modifier.width(scale.SIZE_INSET_LARGE))

                // Hexadecimal representation
                Text(
                    text = chunk.joinToString(" ") { it.toUByte().toString(16).padStart(2, '0') },
                    fontFamily = codeStyle,
                    color = theme.COLOR_FG_0,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.weight(2f)
                )

                Spacer(modifier = Modifier.width(scale.SIZE_INSET_LARGE))

                // ASCII representation
                Text(
                    text = chunk.map { if (it in 32..126) it.toInt().toChar() else '.' }
                        .joinToString(""),
                    fontFamily = codeStyle,
                    color = theme.COLOR_FG_1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}