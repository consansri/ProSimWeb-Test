
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


@Composable
private fun VectorPreview() {
    Image(Add, null)
}

private var _Add: ImageVector? = null

public val Add: ImageVector
    get() {
        if (_Add != null) {
            return _Add!!
        }
        _Add = ImageVector.Builder(
            name = "Add",
            defaultWidth = 90.71.dp,
            defaultHeight = 90.71.dp,
            viewportWidth = 90.71f,
            viewportHeight = 90.71f
        ).apply {
            group {
                /*group {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 0.0f,
                        stroke = null,
                        strokeAlpha = 1.0f,
                        strokeLineWidth = 1.0f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Miter,
                        strokeLineMiter = 1.0f,
                        pathFillType = PathFillType.NonZero
                    ) {
                        moveTo(0f, 90.71f)
                        lineTo(0f, 0f)
                        lineTo(90.71f, 0f)
                        lineTo(90.71f, 90.71f)
                    }
                }*/
                group {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 0.0f,
                        stroke = SolidColor(Color(0xFF000000)),
                        strokeAlpha = 1.0f,
                        strokeLineWidth = 6f,
                        strokeLineCap = StrokeCap.Round,
                        strokeLineJoin = StrokeJoin.Round,
                        strokeLineMiter = 1.0f,
                        pathFillType = PathFillType.NonZero
                    ) {
                        moveTo(23.41f, 11.41f)
                        horizontalLineTo(67.3f)
                        arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 79.3f, 23.41f)
                        verticalLineTo(67.3f)
                        arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 67.3f, 79.3f)
                        horizontalLineTo(23.41f)
                        arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 11.41f, 67.3f)
                        verticalLineTo(23.41f)
                        arcTo(12f, 12f, 0f, isMoreThanHalf = false, isPositiveArc = true, 23.41f, 11.41f)
                        close()
                    }
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 0.0f,
                        stroke = SolidColor(Color(0xFF000000)),
                        strokeAlpha = 1.0f,
                        strokeLineWidth = 6f,
                        strokeLineCap = StrokeCap.Round,
                        strokeLineJoin = StrokeJoin.Round,
                        strokeLineMiter = 1.0f,
                        pathFillType = PathFillType.NonZero
                    ) {
                        moveTo(45.35f, 26.99f)
                        lineTo(45.35f, 63.72f)
                    }
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1.0f,
                        stroke = SolidColor(Color(0xFF000000)),
                        strokeAlpha = 1.0f,
                        strokeLineWidth = 6f,
                        strokeLineCap = StrokeCap.Round,
                        strokeLineJoin = StrokeJoin.Round,
                        strokeLineMiter = 1.0f,
                        pathFillType = PathFillType.NonZero
                    ) {
                        moveTo(26.99f, 45.35f)
                        lineTo(63.72f, 45.35f)
                    }
                }
            }
        }.build()
        return _Add!!
    }

