package ui.ide.analyze

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import cengine.editor.annotation.Severity.*
import cengine.psi.PsiManager
import cengine.psi.core.PsiFile
import ui.uilib.UIState
import ui.uilib.interactable.CButton
import ui.uilib.layout.TabItem
import ui.uilib.layout.TabbedPane
import ui.uilib.layout.VerticalToolBar

@Composable
fun PsiAnalyzerView(
    psiManager: PsiManager<*, *>,
    onOpen: (PsiFile, index: Int) -> Unit,
) {

    val theme = UIState.Theme.value
    val scale = UIState.Scale.value
    val icons = UIState.Icon.value

    val psiService = psiManager.lang.psiService
    val psiCache = remember { psiManager.psiCache }

    val tabs = psiCache.map { TabItem(it, icons.file, it.key.toString()) }

    TabbedPane(tabs, modifier = Modifier.fillMaxSize(), content = {
        Row(Modifier.fillMaxSize()) {
            VerticalToolBar(
                upper = {

                },
                lower = {

                }
            )

            LazyColumn(Modifier.fillMaxWidth()) {

                val (path, psiFile) = tabs[it].value
                val annotations = psiService.annotationsMapped(psiFile).toList().sortedBy { it.first.range.first }.flatMap {
                    val psiPath = psiService.path(it.first).joinToString(" > ") { element -> element.pathName }
                    it.second.map { annotation -> psiPath to annotation }
                }

                items(annotations, key = {
                    it.second.hashCode()
                }) { (psiPath, annotation) ->
                    val iconColor = when (annotation.severity) {
                        INFO -> theme.COLOR_GREEN
                        WARNING -> theme.COLOR_YELLOW
                        ERROR -> theme.COLOR_RED
                    }
                    val icon = when (annotation.severity) {
                        INFO -> icons.statusFine
                        WARNING -> icons.info
                        ERROR -> icons.statusError
                    }

                    CButton(onClick = {
                        onOpen(psiFile, annotation.range.first)
                    }, Modifier.fillMaxWidth(), Arrangement.Start, icon = icon, textAlign = TextAlign.Left, iconTint = iconColor, text = "$psiPath: ${annotation.message}", tooltip = annotation.location(psiFile))
                }
            }
        }
    })


}