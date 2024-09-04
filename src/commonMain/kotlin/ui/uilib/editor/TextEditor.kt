package ui.uilib.editor

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import cengine.vfs.VirtualFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ui.uilib.UIState
import ui.uilib.params.FontType

@Composable
fun TextEditor(file: VirtualFile, modifier: Modifier = Modifier) {

    var textModel by remember { mutableStateOf<String>(file.name) }

    val fileScope = CoroutineScope(Dispatchers.Default)




    BasicTextField(
        value = textModel,
        onValueChange = {
            textModel = it
            fileScope.launch {
                file.setAsUTF8String(it)
            }
        },
        textStyle = TextStyle(
            color = UIState.Theme.value.COLOR_FG_0,
            fontSize = FontType.CODE.getSize(),
            fontFamily = FontType.CODE.getFamily()
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Default,
            keyboardType = KeyboardType.Text
        ),
        modifier = modifier
    )

}