package ui.uilib.interactable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import ui.uilib.UIState
import ui.uilib.params.IconType

@Composable
fun CButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconType: IconType = IconType.MEDIUM,
    iconTint: Color? = null,
    text: String? = null,
    textAlign: TextAlign = TextAlign.Center,
    textStyle: TextStyle = UIState.BaseStyle.current,
    softWrap: Boolean = false,
    withHoverBg: Boolean = true,
    withPressedBg: Boolean = true,
    active: Boolean = true
) {

    val scaling by UIState.Scale
    val theme by UIState.Theme
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isHovered by interactionSource.collectIsHoveredAsState()

    val backgroundColor = if (isPressed && withPressedBg) {
        theme.COLOR_ICON_BG_ACTIVE
    } else if (isHovered && withHoverBg) {
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
            .wrapContentWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {

        if (icon != null) {
            Icon(
                icon,
                contentDescription = "Icon",
                tint = iconTint ?: UIState.Theme.value.COLOR_FG_0,
                modifier = Modifier.size(iconType.getSize()),
            )
        }

        if (icon != null && text != null) {
            Spacer(Modifier.width(scaling.SIZE_INSET_MEDIUM))
        }

        if (text != null) {
            Text(
                text,
                overflow = TextOverflow.Clip,
                textAlign = textAlign,
                fontFamily = textStyle.fontFamily,
                fontSize = textStyle.fontSize,
                softWrap = softWrap,
                color = if (!active) UIState.Theme.value.COLOR_FG_0.copy(0.5f) else UIState.Theme.value.COLOR_FG_0,
            )
        }
    }


}