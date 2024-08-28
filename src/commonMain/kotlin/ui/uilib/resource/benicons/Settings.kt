
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


@Composable
private fun VectorPreview() {
    Image(SettingsIcon, null)
}

private var _SettingsIcon: ImageVector? = null

public val SettingsIcon: ImageVector
		get() {
			if (_SettingsIcon != null) {
				return _SettingsIcon!!
			}
			_SettingsIcon = ImageVector.Builder(
                name = "Settings",
                defaultWidth = 90.71.dp,
                defaultHeight = 90.71.dp,
                viewportWidth = 90.71f,
                viewportHeight = 90.71f
            ).apply {
				group {
					group {
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
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
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
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
}
					group {
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = SolidColor(Color(0xFF000000)),
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 6f,
    						strokeLineCap = StrokeCap.Butt,
    						strokeLineJoin = StrokeJoin.Round,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(79.3f, 51.65f)
							arcToRelative(34.35f, 34.35f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, -12.3f)
							lineTo(72.68f, 37.9f)
							arcToRelative(28.07f, 28.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, -2.63f, -6.34f)
							lineToRelative(3.66f, -5.72f)
							arcToRelative(34.75f, 34.75f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8.7f, -8.7f)
							lineTo(59.3f, 20.8f)
							arcTo(28.07f, 28.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 53f, 18.17f)
							lineToRelative(-1.45f, -6.62f)
							arcToRelative(34.81f, 34.81f, 0f, isMoreThanHalf = false, isPositiveArc = false, -12.3f, 0f)
							lineToRelative(-1.45f, 6.62f)
							arcToRelative(27.65f, 27.65f, 0f, isMoreThanHalf = false, isPositiveArc = false, -6.34f, 2.64f)
							lineTo(25.7f, 17.14f)
							arcToRelative(34.75f, 34.75f, 0f, isMoreThanHalf = false, isPositiveArc = false, -8.7f, 8.7f)
							lineToRelative(3.67f, 5.72f)
							arcTo(27.19f, 27.19f, 0f, isMoreThanHalf = false, isPositiveArc = false, 18f, 37.9f)
							lineToRelative(-6.63f, 1.45f)
							arcToRelative(34.81f, 34.81f, 0f, isMoreThanHalf = false, isPositiveArc = false, 0f, 12.3f)
							lineTo(18f, 53.1f)
							arcToRelative(26.94f, 26.94f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.63f, 6.33f)
							lineTo(17f, 65.15f)
							arcToRelative(34.58f, 34.58f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8.7f, 8.7f)
							lineToRelative(5.71f, -3.66f)
							arcToRelative(27.73f, 27.73f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.35f, 2.64f)
							lineToRelative(1.45f, 6.62f)
							arcToRelative(34.81f, 34.81f, 0f, isMoreThanHalf = false, isPositiveArc = false, 12.3f, 0f)
							lineTo(53f, 72.83f)
							arcToRelative(27.65f, 27.65f, 0f, isMoreThanHalf = false, isPositiveArc = false, 6.34f, -2.64f)
							lineTo(65f, 73.85f)
							arcToRelative(34.58f, 34.58f, 0f, isMoreThanHalf = false, isPositiveArc = false, 8.7f, -8.7f)
							lineToRelative(-3.66f, -5.71f)
							arcToRelative(28.07f, 28.07f, 0f, isMoreThanHalf = false, isPositiveArc = false, 2.63f, -6.34f)
							close()
							moveTo(58.61f, 45.51f)
							arcToRelative(13.18f, 13.18f, 0f, isMoreThanHalf = false, isPositiveArc = true, -3.88f, 9.36f)
							horizontalLineToRelative(0f)
							arcToRelative(13.21f, 13.21f, 0f, isMoreThanHalf = false, isPositiveArc = true, -9.35f, 3.87f)
							horizontalLineToRelative(0f)
							arcTo(13.23f, 13.23f, 0f, isMoreThanHalf = false, isPositiveArc = true, 36f, 54.88f)
							horizontalLineToRelative(0f)
							arcToRelative(13.25f, 13.25f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, -18.74f)
							horizontalLineToRelative(0f)
							arcToRelative(13.23f, 13.23f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.36f, -3.87f)
							horizontalLineToRelative(0f)
							arcToRelative(13.21f, 13.21f, 0f, isMoreThanHalf = false, isPositiveArc = true, 9.35f, 3.87f)
							horizontalLineToRelative(0f)
							arcToRelative(13.18f, 13.18f, 0f, isMoreThanHalf = false, isPositiveArc = true, 3.88f, 9.36f)
							horizontalLineToRelative(0f)
}
}
}
}.build()
return _SettingsIcon!!
		}

