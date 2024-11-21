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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import cengine.lang.obj.elf.*
import cengine.project.Project
import cengine.vfs.VirtualFile
import kotlinx.coroutines.Job
import ui.uilib.UIState
import ui.uilib.interactable.CToggle

@Composable
fun ObjectEditor(
    file: VirtualFile,
    project: Project,
    codeStyle: TextStyle,
    titleStyle: TextStyle,
    baseStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    val scale = UIState.Scale.value

    val fileContent by remember {
        mutableStateOf(file.getContent())
    }

    var elfReader by remember { mutableStateOf(ELFFile.parse(file.name, fileContent)) }

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
                ELFHeaderInfos(elfReader, fileContent, codeStyle, titleStyle, baseStyle)

                // Draw Sections wrapped in ProgramHeaders
                ELFSectionInfos(elfReader, fileContent, codeStyle, baseStyle)
            }
        }

        if (elfReader == null) ByteRange(fileContent, fileContent.indices, 16, codeStyle, baseStyle)

    }

    LaunchedEffect(fileContent) {
        elfReader = ELFFile.parse(file.name, fileContent)
    }
}

@Composable
fun ELFHeaderInfos(
    elfReader: ELFFile,
    fileContent: ByteArray,
    codeStyle: TextStyle,
    titleStyle: TextStyle,
    baseStyle: TextStyle
) {
    val theme = UIState.Theme.value
    val scale = UIState.Scale.value

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
            fontFamily = titleStyle.fontFamily,
            fontSize = titleStyle.fontSize
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
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
            )

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            Text(
                E_IDENT.getElfClass(e_ident.ei_class),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
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
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
            )

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            Text(
                E_IDENT.getElfData(e_ident.ei_data),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
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
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
            )

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            Text(
                e_ident.ei_version.toString(),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
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
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
            )

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            Text(
                E_IDENT.getOsAbi(e_ident.ei_osabi),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
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
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
            )

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            Text(
                e_ident.ei_abiversion.toString(),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
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
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
            )

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            Text(
                Ehdr.getELFType(ehdr.e_type),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
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
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
            )

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            Text(
                Ehdr.getELFMachine(ehdr.e_machine),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
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
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
            )

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            Text(
                ehdr.e_version.toString(),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
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
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
            )

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            Text(
                when (ehdr) {
                    is ELF32_Ehdr -> "0x" + ehdr.e_entry.toString(16)
                    is ELF64_Ehdr -> "0x" + ehdr.e_entry.toString(16)
                    else -> "(invalid)"
                },
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
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
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
            )

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            Text(
                when (ehdr) {
                    is ELF32_Ehdr -> "0x" + ehdr.e_phoff.toString(16)
                    is ELF64_Ehdr -> "0x" + ehdr.e_phoff.toString(16)
                    else -> "(invalid)"
                } + " (bytes into file)",
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
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
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
            )

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            Text(
                when (ehdr) {
                    is ELF32_Ehdr -> "0x" + ehdr.e_shoff.toString(16)
                    is ELF64_Ehdr -> "0x" + ehdr.e_shoff.toString(16)
                    else -> "(invalid)"
                } + " (bytes into file)",
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
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
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
            )

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            Text(
                "0x" + ehdr.e_flags.toString(16),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
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
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
            )

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            Text(
                ehdr.e_ehsize.toString() + " (bytes)",
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
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
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
            )

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            Text(
                ehdr.e_phentsize.toString() + " (bytes)",
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
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
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
            )

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            Text(
                ehdr.e_phnum.toString(),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
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
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
            )

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            Text(
                ehdr.e_shentsize.toString() + " (bytes)",
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
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
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
            )

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            Text(
                ehdr.e_phnum.toString(),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
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
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
            )

            Spacer(Modifier.width(scale.SIZE_INSET_MEDIUM))

            Text(
                "0x" + ehdr.e_shstrndx.toString(16),
                textAlign = TextAlign.Left,
                modifier = Modifier.weight(0.5f),
                color = theme.COLOR_FG_0,
                fontFamily = codeStyle.fontFamily,
                fontSize = codeStyle.fontSize
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
            }, value = showBytes, icon = if (showBytes) {
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

            ByteRange(fileContent, 0..<ehdr.e_ehsize.toInt(), 16, codeStyle, baseStyle)
        }
    }
}

@Composable
fun ELFSectionInfos(elfReader: ELFFile, fileContent: ByteArray, codeStyle: TextStyle, baseStyle: TextStyle) {

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value

    elfReader.segmentToSectionGroup.forEach { group ->

        Spacer(
            Modifier.height(scale.SIZE_INSET_MEDIUM)
        )

        when (group) {
            is ELFFile.Section -> {
                val section = group.section
                val index = group.index

                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(theme.COLOR_BG_1, RoundedCornerShape(scale.SIZE_CORNER_RADIUS))
                ) {

                    val name = elfReader.nameOfSection(index)
                    val type = Shdr.getSectionType(section.sh_type)
                    val flags = Shdr.getSectionFlags(
                        when (section) {
                            is ELF32_Shdr -> section.sh_flags.toULong()
                            is ELF64_Shdr -> section.sh_flags
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
                            fontFamily = baseStyle.fontFamily,
                            fontSize = baseStyle.fontSize,
                            modifier = Modifier
                                .padding(scale.SIZE_INSET_MEDIUM),
                        )
                    }

                    val range = when (section) {
                        is ELF32_Shdr -> {
                            section.sh_offset.toInt()..<(section.sh_offset + section.sh_size).toInt()
                        }

                        is ELF64_Shdr -> {
                            section.sh_offset.toInt()..<(section.sh_offset + section.sh_size).toInt()
                        }
                    }

                    if (!range.isEmpty()) {

                        Box(
                            Modifier
                                .background(theme.COLOR_BORDER)
                                .fillMaxWidth()
                                .height(scale.SIZE_BORDER_THICKNESS)
                        )

                        ByteRange(fileContent, range, 16, codeStyle, baseStyle)
                    }
                }
            }

            is ELFFile.Segment -> {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val phdr = group.phdr
                    Column(
                        Modifier
                            .background(theme.COLOR_SELECTION, RoundedCornerShape(scale.SIZE_CORNER_RADIUS))
                            .padding(scale.SIZE_INSET_MEDIUM),
                        verticalArrangement = Arrangement.Center

                    ) {
                        Text(
                            "${Phdr.getProgramHeaderType(phdr.p_type)} - ${Phdr.getProgramHeaderFlags(phdr.p_flags)}",
                            color = theme.COLOR_FG_0,
                            fontFamily = baseStyle.fontFamily,
                            fontSize = baseStyle.fontSize
                        )
                        Text(
                            "offset: 0x${
                                when (phdr) {
                                    is ELF32_Phdr -> phdr.p_offset.toString(16)
                                    is ELF64_Phdr -> phdr.p_offset.toString(16)
                                }
                            }",
                            color = theme.COLOR_FG_0,
                            fontFamily = codeStyle.fontFamily,
                            fontSize = codeStyle.fontSize
                        )
                        Text(
                            "vaddr: 0x${
                                when (phdr) {
                                    is ELF32_Phdr -> phdr.p_vaddr.toString(16)
                                    is ELF64_Phdr -> phdr.p_vaddr.toString(16)
                                }
                            }",
                            color = theme.COLOR_FG_0,
                            fontFamily = codeStyle.fontFamily,
                            fontSize = codeStyle.fontSize
                        )
                        Text(
                            "paddr: 0x${
                                when (phdr) {
                                    is ELF32_Phdr -> phdr.p_paddr.toString(16)
                                    is ELF64_Phdr -> phdr.p_paddr.toString(16)
                                }
                            }",
                            color = theme.COLOR_FG_0,
                            fontFamily = codeStyle.fontFamily,
                            fontSize = codeStyle.fontSize
                        )
                        Text(
                            "filesz: ${
                                when (phdr) {
                                    is ELF32_Phdr -> phdr.p_filesz
                                    is ELF64_Phdr -> phdr.p_filesz
                                }
                            }",
                            color = theme.COLOR_FG_0,
                            fontFamily = codeStyle.fontFamily,
                            fontSize = codeStyle.fontSize
                        )
                        Text(
                            "memsz: ${
                                when (phdr) {
                                    is ELF32_Phdr -> phdr.p_memsz
                                    is ELF64_Phdr -> phdr.p_memsz
                                }
                            }",
                            color = theme.COLOR_FG_0,
                            fontFamily = codeStyle.fontFamily,
                            fontSize = codeStyle.fontSize
                        )

                        Text(
                            "align: 0x${
                                when (phdr) {
                                    is ELF32_Phdr -> phdr.p_align.toString(16)
                                    is ELF64_Phdr -> phdr.p_align.toString(16)
                                }
                            }",
                            color = theme.COLOR_FG_0,
                            fontFamily = codeStyle.fontFamily,
                            fontSize = codeStyle.fontSize
                        )
                    }

                    Spacer(
                        Modifier.width(scale.SIZE_INSET_MEDIUM)
                    )

                    Column(
                        Modifier.weight(0.9f),
                    ) {
                        group.sections.forEach { section ->
                            val index = elfReader.sectionHeaders.indexOf(section)

                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .background(theme.COLOR_BG_1, RoundedCornerShape(scale.SIZE_CORNER_RADIUS))
                            ) {

                                val name = elfReader.nameOfSection(index)
                                val type = Shdr.getSectionType(section.sh_type)
                                val flags = Shdr.getSectionFlags(
                                    when (section) {
                                        is ELF32_Shdr -> section.sh_flags.toULong()
                                        is ELF64_Shdr -> section.sh_flags
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
                                        fontFamily = baseStyle.fontFamily,
                                        fontSize = baseStyle.fontSize,
                                        modifier = Modifier
                                            .padding(scale.SIZE_INSET_MEDIUM),
                                    )
                                }

                                val range = when (section) {
                                    is ELF32_Shdr -> {
                                        section.sh_offset.toInt()..<(section.sh_offset + section.sh_size).toInt()
                                    }

                                    is ELF64_Shdr -> {
                                        section.sh_offset.toInt()..<(section.sh_offset + section.sh_size).toInt()
                                    }
                                }

                                if (!range.isEmpty()) {

                                    Box(
                                        Modifier
                                            .background(theme.COLOR_BORDER)
                                            .fillMaxWidth()
                                            .height(scale.SIZE_BORDER_THICKNESS)
                                    )

                                    ByteRange(fileContent, range, 16, codeStyle, baseStyle)
                                }
                            }
                        }
                    }


                }
            }
        }
    }

}

@Composable
fun ByteRange(byteArray: ByteArray, range: IntRange, chunkSize: Int, codeStyle: TextStyle, baseStyle: TextStyle) {

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value

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
                    fontFamily = codeStyle.fontFamily,
                    fontSize = codeStyle.fontSize,
                    color = theme.COLOR_FG_1,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.weight(0.5f)
                )

                Spacer(modifier = Modifier.width(scale.SIZE_INSET_LARGE))

                // Hexadecimal representation
                Text(
                    text = chunk.joinToString(" ") { it.toUByte().toString(16).padStart(2, '0') },
                    fontFamily = codeStyle.fontFamily,
                    fontSize = codeStyle.fontSize,
                    color = theme.COLOR_FG_0,
                    textAlign = TextAlign.Left,
                    modifier = Modifier.weight(2f)
                )

                Spacer(modifier = Modifier.width(scale.SIZE_INSET_LARGE))

                // ASCII representation
                Text(
                    text = chunk.map { if (it in 32..126) it.toInt().toChar() else '.' }
                        .joinToString(""),
                    fontFamily = codeStyle.fontFamily,
                    fontSize = codeStyle.fontSize,
                    color = theme.COLOR_FG_1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}


