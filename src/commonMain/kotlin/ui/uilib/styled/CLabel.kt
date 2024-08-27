package ui.uilib.styled

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import ui.uilib.UIState
import ui.uilib.params.TextSize

@Composable
fun CLabel(text: String? = null, customColor: Color? = null, textSize: TextSize = TextSize.MEDIUM, textAlign: TextAlign = TextAlign.Center, modifier: Modifier = Modifier) {

    Row(
        modifier.padding(UIState.Scale.value.SIZE_INSET_MEDIUM)
    ) {
        if (text != null) {
            Text(
                text,
                color = customColor ?: UIState.Theme.value.COLOR_FG_0,
                textAlign = textAlign,
                fontSize = textSize.get()
            )
        }
    }

}