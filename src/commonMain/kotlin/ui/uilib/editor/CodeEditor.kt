package ui.uilib.editor

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Text
import androidx.compose.runtime.*


@Composable
fun CodeEditor(codeEditorState: CodeEditorState) {

    var state by remember { mutableStateOf(codeEditorState) }


    val lines = (0..<state.textModel.lines).map { lineID -> LineInfo(state.textModel.indexOf(lineID, 0), state.textModel.indexOf(lineID + 1, 0)) }

    Row {

        LazyColumn {

            items(state.textModel.lines) { lineID ->

                Text(
                    (lineID + 1).toString(),
                )
            }

        }



        LazyColumn {

            items(state.textModel.lines) { lineID ->
                val startIndex = state.textModel.indexOf(lineID, 0)
                val endIndex = state.textModel.indexOf(lineID + 1, 0)
                val text = state.textModel.substring(startIndex, endIndex)
                Text(text)
            }

        }

    }


}

data class LineInfo(val from: Int, val until: Int)

