package ui.uilib.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import ui.uilib.UIState
import ui.uilib.label.CLabel
import ui.uilib.params.FontType
import ui.uilib.params.IconType


@Composable
fun AppBar(
    icon: ImageVector,
    title: String,
    name: String? = null,
    actions: @Composable (RowScope) -> Unit
) {
    val theme = UIState.Theme.value
    val scale = UIState.Scale.value
// Row for the Top Bar
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(theme.COLOR_BG_0)
            .padding(horizontal = scale.SIZE_INSET_MEDIUM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        // Title on the left
        Row(verticalAlignment = Alignment.CenterVertically) {
            CLabel(
                icon = icon,
                text = title,
                modifier = Modifier.align(Alignment.CenterVertically),
                fontType = FontType.MEDIUM,
                iconType = IconType.MEDIUM
            )
            if (name != null) {
                CLabel(
                    icon = UIState.Icon.value.chevronRight,
                    text = name,
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
