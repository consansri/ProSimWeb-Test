package ui.uilib.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import ui.uilib.UIState
import ui.uilib.label.CLabel
import ui.uilib.params.FontType
import ui.uilib.params.IconType


@Composable
fun AppBar(
    icon: ImageVector,
    title: String,
    name: String? = null,
    type: String? = null,
    actions: @Composable RowScope.() -> Unit
) {
    val theme = UIState.Theme.value
    val scale = UIState.Scale.value

    var titleBounds by remember { mutableStateOf(Rect(0f, 0f, 0f, 0f)) }

    // Row for the Top Bar
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.radialGradient(
                    colors = listOf(theme.COLOR_ORANGE.copy(alpha = 0.4f), theme.COLOR_BG_1),
                    titleBounds.center,
                    radius = titleBounds.width
                )
            )
            .padding(scale.SIZE_INSET_MEDIUM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        // Title on the left
        Row(verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .onGloballyPositioned {
                    val bounds = it.boundsInParent()
                    titleBounds = bounds
                }
        ) {
            CLabel(
                icon = icon,
                text = title,
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.CenterVertically),
                fontType = FontType.MEDIUM,
                iconType = IconType.MEDIUM
            )
            if (name != null) {
                CLabel(
                    modifier = Modifier
                        .wrapContentSize(),
                    icon = UIState.Icon.value.chevronRight,
                    text = name,
                    fontType = FontType.MEDIUM
                )
            }

            if (type != null) {
                CLabel(
                    modifier = Modifier
                        .wrapContentSize(),
                    icon = UIState.Icon.value.chevronRight,
                    text = type,
                    fontType = FontType.MEDIUM
                )
            }
        }


        // Actions on the right
        Row(verticalAlignment = Alignment.CenterVertically) {
            actions(this)
        }
    }


}
