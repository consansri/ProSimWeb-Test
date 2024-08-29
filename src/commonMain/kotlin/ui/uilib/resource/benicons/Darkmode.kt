
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


@Composable
private fun VectorPreview() {
    Image(Darkmode, null)
}

private var _Darkmode: ImageVector? = null

public val Darkmode: ImageVector
    get() {
        if (_Darkmode != null) {
            return _Darkmode!!
        }
        _Darkmode = ImageVector.Builder(
            name = "Darkmode",
            defaultWidth = 90.71.dp,
            defaultHeight = 90.71.dp,
            viewportWidth = 90.71f,
            viewportHeight = 90.71f
        ).apply {
            group {
                group {
                    path(
                        stroke = null,
                        strokeAlpha = 1.0f,
                        strokeLineWidth = 1.0f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Miter,
                        strokeLineMiter = 1.0f,
                        pathFillType = PathFillType.NonZero
                    ) {
                        moveTo(0f, 0f)
                        horizontalLineTo(90.71f)
                        verticalLineTo(90.71f)
                        horizontalLineTo(0f)
                        verticalLineTo(0f)
                        close()
                    }
                    path(
                        stroke = SolidColor(Color(0xFF000000)),
                        strokeAlpha = 1.0f,
                        strokeLineWidth = 6f,
                        strokeLineCap = StrokeCap.Round,
                        strokeLineJoin = StrokeJoin.Round,
                        strokeLineMiter = 1.0f,
                        pathFillType = PathFillType.NonZero
                    ) {
                        moveTo(40.91f, 48.86f)
                        curveToRelative(-7.51f, -10.21f, -8.58f, -23.63f, -5.38f, -32.79f)
                        arcTo(31f, 31f, 0f, isMoreThanHalf = true, isPositiveArc = false, 74.12f, 62.5f)
                        curveTo(63.56f, 64.84f, 49.5f, 60.51f, 40.91f, 48.86f)
                        close()
                    }
                }
            }
        }.build()
        return _Darkmode!!
    }

