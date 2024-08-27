package ui.uilib.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ui.uilib.UIState


@Composable
fun BorderLayout(
    modifier: Modifier = Modifier,
    topContent: (@Composable RowScope.() -> Unit)? = null,
    leftContent: (@Composable ColumnScope.() -> Unit)? = null,
    centerContent: @Composable BoxScope.() -> Unit,
    rightContent: (@Composable ColumnScope.() -> Unit)? = null,
    bottomContent: (@Composable RowScope.() -> Unit)? = null,
    topBg: Color = Color.Transparent,
    leftBg: Color = Color.Transparent,
    centerBg: Color = Color.Transparent,
    rightBg: Color = Color.Transparent,
    bottomBg: Color = Color.Transparent
) {
    val theme = UIState.Theme.value
    val scale = UIState.Scale.value

    Column(modifier = modifier) {
        topContent?.let {
            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .background(topBg)
                    .padding(UIState.Scale.value.SIZE_INSET_MEDIUM)
            ) {
                it()
            }
            Spacer(modifier = Modifier
                .height(scale.SIZE_BORDER_THICKNESS)
                .fillMaxWidth()
                .background(theme.COLOR_BORDER)
            )
        }

        Row(modifier = Modifier.weight(1f)) {
            leftContent?.let {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(leftBg)
                        .padding(UIState.Scale.value.SIZE_INSET_MEDIUM)
                ) {
                    it()
                }
                Spacer(modifier = Modifier
                    .width(scale.SIZE_BORDER_THICKNESS)
                    .fillMaxHeight()
                    .background(theme.COLOR_BORDER)
                )
            }

            Box(modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .fillMaxHeight()
                .background(centerBg)
            ) {
                centerContent()
            }

            rightContent?.let {
                Spacer(modifier = Modifier
                    .width(scale.SIZE_BORDER_THICKNESS)
                    .fillMaxHeight()
                    .background(theme.COLOR_BORDER)
                )
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(rightBg)
                        .padding(UIState.Scale.value.SIZE_INSET_MEDIUM)
                ) {
                    it()
                }
            }
        }

        bottomContent?.let {
            Spacer(modifier = Modifier
                .height(scale.SIZE_BORDER_THICKNESS)
                .fillMaxWidth()
                .background(theme.COLOR_BORDER)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bottomBg)
                    .padding(UIState.Scale.value.SIZE_INSET_MEDIUM)

            ) {
                it()
            }
        }
    }
}