package ui.uilib.button

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import ui.uilib.UIState
import ui.uilib.params.TextSize

@Composable
fun CButton(onClick: () -> Unit, icon: ImageVector? = null, text: String? = null, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Center, textSize: TextSize = TextSize.MEDIUM, active: Boolean = true, content: @Composable (RowScope.() -> Unit) = {}) {

    val scaling by UIState.Scale
    val theme by UIState.Theme
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor = if (isPressed) {
        theme.COLOR_ICON_BG_ACTIVE
    } else if (isHovered) {
        theme.COLOR_ICON_BG_HOVER
    } else {
        Color.Transparent
    }

    Row(
        modifier
            .background(backgroundColor, shape = RoundedCornerShape(scaling.SIZE_CORNER_RADIUS))
            .clickable(interactionSource, indication = null, onClick = if (active) {
                onClick
            } else {
                {}
            })
            .hoverable(interactionSource)
            .padding(scaling.SIZE_INSET_MEDIUM)
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

        content()
    }


}