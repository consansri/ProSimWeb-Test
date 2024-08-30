package ui.uilib.label

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import ui.uilib.UIState
import ui.uilib.params.TextSize

@Composable
fun CLabel(modifier: Modifier = Modifier, icon: ImageVector? = null, text: String? = null, textAlign: TextAlign = TextAlign.Center, textSize: TextSize = TextSize.MEDIUM, active: Boolean = true) {

    val scaling by UIState.Scale
    val theme by UIState.Theme

    Row(
        modifier
            .background(Color.Transparent, shape = RoundedCornerShape(scaling.SIZE_CORNER_RADIUS))
            .padding(scaling.SIZE_INSET_MEDIUM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        if (icon != null) {
            Image(
                icon,
                colorFilter = ColorFilter.tint(theme.COLOR_FG_0),
                modifier = Modifier.size(scaling.SIZE_CONTROL_MEDIUM),
                contentDescription = "Add Icon"
            )
        }

        if (icon != null && text != null) {
            Spacer(Modifier.width(scaling.SIZE_INSET_MEDIUM))
        }

        if (text != null) {
            Text(
                text,
                textAlign = textAlign,
                fontSize = textSize.get(),
                color = if (!active) UIState.Theme.value.COLOR_FG_0.copy(0.5f) else UIState.Theme.value.COLOR_FG_0,
            )
        }
    }
}
