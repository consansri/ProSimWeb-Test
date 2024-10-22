package ui.uilib.text

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.TextFieldValue
import ui.uilib.UIState
import ui.uilib.params.FontType


@Composable
fun CTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    readonly: Boolean = false,
    fontType: FontType = FontType.MEDIUM,
    backgroundColor: Color = Color.Transparent,
    textColor: Color = UIState.Theme.value.COLOR_FG_0,
    borderColor: Color = UIState.Theme.value.COLOR_BORDER,
    error: Boolean = false,
    showBorder: Boolean = true,
    decorationBox: @Composable (@Composable () -> Unit) -> Unit = { innerTextField ->
        innerTextField()
    }
) {
    val scale = UIState.Scale.value
    val theme = UIState.Theme.value

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = if (showBorder) {
            modifier.background(backgroundColor)
                .border(scale.SIZE_BORDER_THICKNESS, if (!error) borderColor else UIState.Theme.value.COLOR_RED, RoundedCornerShape(scale.SIZE_CORNER_RADIUS))
                .padding(scale.SIZE_INSET_MEDIUM)
        } else {
            modifier.background(backgroundColor)
                .padding(scale.SIZE_INSET_MEDIUM)
        },
        textStyle = fontType.getStyle().copy(color = theme.COLOR_FG_0),
        singleLine = singleLine,
        readOnly = readonly,
        cursorBrush = SolidColor(textColor),
        decorationBox = decorationBox
    )
}
