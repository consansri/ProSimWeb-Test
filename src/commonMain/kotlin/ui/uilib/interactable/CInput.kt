package ui.uilib.interactable

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import cengine.util.integer.Value
import ui.uilib.params.FontType
import ui.uilib.text.CTextField

@Composable
fun CInput(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    numberFormat: Value.Types,
    showBorder: Boolean = false,
    fontType: FontType = FontType.CODE,
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue(value)) }

    val filterChars: (Char) -> Boolean = { char ->
        when (numberFormat) {
            Value.Types.Bin -> char in '0'..'1'
            Value.Types.Hex -> char.isDigit() || char.uppercaseChar() in 'A'..'F'
            Value.Types.Dec, Value.Types.UDec -> char.isDigit()
        }
    }

    CTextField(modifier = modifier,value = textFieldValue, fontType = fontType, showBorder = showBorder, onValueChange = { newVal ->
        val filtered = newVal.copy(text = newVal.text.filter(filterChars))
        textFieldValue = filtered
        onValueChange(filtered.text)
    })
}