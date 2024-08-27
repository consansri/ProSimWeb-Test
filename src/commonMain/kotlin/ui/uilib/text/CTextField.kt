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
import androidx.compose.ui.text.TextStyle
import ui.uilib.UIState


@Composable
fun CTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    readonly: Boolean = false,
    backgroundColor: Color = Color.Transparent,
    textColor: Color = UIState.Theme.value.COLOR_FG_0,
    borderColor: Color = UIState.Theme.value.COLOR_BORDER,
    error: Boolean = false,
    decorationBox: @Composable ( @Composable () -> Unit)-> Unit = {innerTextField ->
        innerTextField()
    }
) {
    val scale = UIState.Scale.value

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.background(backgroundColor)
            .border(scale.SIZE_BORDER_THICKNESS, if (!error) borderColor else UIState.Theme.value.COLOR_RED, RoundedCornerShape(scale.SIZE_CORNER_RADIUS))
            .padding(scale.SIZE_INSET_MEDIUM),
        textStyle = TextStyle(fontSize = UIState.Scale.value.FONTSCALE_MEDIUM, color = textColor),
        singleLine = singleLine,
        readOnly = readonly,
        cursorBrush = SolidColor(textColor),
        decorationBox = decorationBox
    )
}
