package ui.uilib.interactable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import cengine.util.integer.Value
import ui.uilib.UIState
import ui.uilib.params.FontType
import ui.uilib.text.CTextField

@Composable
fun CInput(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onFocusLost: (TextFieldValue) -> Unit,
    numberFormat: Value.Types,
    showBorder: Boolean = false,
    fontStyle: TextStyle = FontType.CODE.getStyle(),
) {

    val filterChars: (Char) -> Boolean = { char ->
        when (numberFormat) {
            Value.Types.Bin -> char in '0'..'1'
            Value.Types.Hex -> char.isDigit() || char.uppercaseChar() in 'A'..'F'
            Value.Types.Dec, Value.Types.UDec -> char.isDigit()
        }
    }

    CTextField(modifier = modifier, value = value, fontStyle = fontStyle, textColor = UIState.Theme.value.COLOR_FG_0, showBorder = showBorder, onValueChange = { newVal ->
        val filtered = newVal.copy(text = newVal.text.filter(filterChars))
        onValueChange(filtered)
    }, onFocusLost = onFocusLost)
}