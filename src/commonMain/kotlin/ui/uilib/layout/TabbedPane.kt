package ui.uilib.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import ui.uilib.UIState
import ui.uilib.interactable.CButton
import ui.uilib.label.CLabel

data class TabItem<T : Any>(
    val value: T,
    val icon: ImageVector? = null,
    val title: String
)

@Composable
fun <T : Any> TabbedPane(
    tabs: List<TabItem<T>>,
    closeable: Boolean = false,
    content: @Composable (Int) -> Unit,
    baseStyle: TextStyle,
    modifier: Modifier = Modifier,
    onCloseTab: (TabItem<T>) -> Unit = {}
) {
    var selectedTabIndex by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
    ) {
        // Tab Row
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { index, tabItem ->
                // Display each tab with a close Button

                Column(
                    Modifier
                        .weight(1f)
                        .clickable {
                            selectedTabIndex = index
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CLabel(
                            modifier = Modifier.weight(1f),
                            icon = tabItem.icon,
                            text = tabItem.title,
                            textStyle = baseStyle
                        )

                        if (closeable) {
                            CButton(
                                icon = UIState.Icon.value.close,
                                onClick = {
                                    onCloseTab(tabItem)

                                    // If closing the selected tab, adjust the selected index
                                    if (index == selectedTabIndex) {
                                        selectedTabIndex = (selectedTabIndex - 1).coerceAtLeast(0)
                                    }
                                }
                            )
                        }
                    }

                    Spacer(
                        Modifier
                            .fillMaxWidth()
                            .height(UIState.Scale.value.SIZE_BORDER_THICKNESS_MARKED)
                            .background(if (index == selectedTabIndex) UIState.Theme.value.COLOR_SELECTION else Color.Transparent, RoundedCornerShape(UIState.Scale.value.SIZE_CORNER_RADIUS))
                    )
                }
            }
        }

        Spacer(
            Modifier
                .fillMaxWidth()
                .height(UIState.Scale.value.SIZE_BORDER_THICKNESS)
                .background(UIState.Theme.value.COLOR_BORDER)
        )

        if (selectedTabIndex in tabs.indices) {
            // Display the content of the selected tab
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                content(selectedTabIndex)
            }
        } else {
            // Display a message when no tabs are open
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Text(
                    "No tabs open",
                    color = UIState.Theme.value.COLOR_FG_1,
                    fontFamily = baseStyle.fontFamily,
                    fontSize = baseStyle.fontSize
                )
            }
        }
    }
}