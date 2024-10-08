package ui.uilib.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cengine.lang.asm.elf.ELFReader
import cengine.lang.asm.elf.E_IDENT
import cengine.lang.asm.elf.Ehdr
import cengine.project.Project
import cengine.vfs.VirtualFile
import kotlinx.coroutines.Job
import ui.uilib.UIState
import ui.uilib.params.FontType

@Composable
fun ObjectEditor(
    file: VirtualFile,
    project: Project,
    modifier: Modifier = Modifier
) {
    val elfReader by remember {
        mutableStateOf<ELFReader?>(
            try {
                ELFReader(file.getContent())
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
            .horizontalScroll(scrollHorizontal)
    ) {
        elfReader?.let {elfReader ->
            Column(
                Modifier
                    .fillMaxWidth()
            ) {

                // Draw Header Info
                ELFHeaderInfos(elfReader)


                // Draw Sections wrapped in ProgramHeaders

            }
        }
    }
}

@Composable
fun ELFHeaderInfos(elfReader: ELFReader) {
    val theme = UIState.Theme.value
    val scale = UIState.Scale.value

    val codeStyle = FontType.CODE.getFamily()
    val baseStyle = FontType.MEDIUM.getFamily()

    val ehdr = elfReader.ehdr
    val e_ident = ehdr.e_ident

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            E_IDENT.getElfClass(e_ident.ei_class),
            color = theme.COLOR_FG_0,
            fontFamily = baseStyle,
            modifier = Modifier.padding(scale.SIZE_INSET_MEDIUM),
        )
        Spacer(Modifier
            .fillMaxHeight()
            .width(scale.SIZE_BORDER_THICKNESS)
            .background(theme.COLOR_BORDER))
        Text(
            E_IDENT.getElfData(e_ident.ei_data),
            color = theme.COLOR_FG_0,
            fontFamily = baseStyle,
            modifier = Modifier.padding(scale.SIZE_INSET_MEDIUM),
        )
        Spacer(Modifier
            .fillMaxHeight()
            .width(scale.SIZE_BORDER_THICKNESS)
            .background(theme.COLOR_BORDER))
        Text(
            Ehdr.getELFType(elfReader.ehdr.e_type),
            color = theme.COLOR_FG_0,
            fontFamily = baseStyle,
            modifier = Modifier.padding(scale.SIZE_INSET_MEDIUM)
        )
    }
}