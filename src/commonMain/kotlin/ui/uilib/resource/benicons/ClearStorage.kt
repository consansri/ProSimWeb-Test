import androidx.compose.runtime.Composable
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp




@Composable
private fun VectorPreview() {
    Image(ClearStorage, null)
}

private var _ClearStorage: ImageVector? = null

public val ClearStorage: ImageVector
		get() {
			if (_ClearStorage != null) {
				return _ClearStorage!!
			}
_ClearStorage = ImageVector.Builder(
                name = "ClearStorage",
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
							moveTo(90.71f, 0f)
							lineTo(90.71f, 90.71f)
							lineTo(0f, 90.71f)
							lineTo(0f, 0f)
}
}
					group {
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = null,
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 1.0f,
    						strokeLineCap = StrokeCap.Round,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(48.38f, 78.16f)
							lineTo(75.21f, 78.16f)
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
							moveTo(31.25f, 12.4f)
							horizontalLineToRelative(17f)
							arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.67f, 5.67f)
							verticalLineTo(69.18f)
							arcTo(11.34f, 11.34f, 0f, isMoreThanHalf = false, isPositiveArc = true, 42.58f, 80.52f)
							horizontalLineTo(36.92f)
							arcTo(11.34f, 11.34f, 0f, isMoreThanHalf = false, isPositiveArc = true, 25.58f, 69.18f)
							verticalLineTo(18.07f)
							arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.67f, -5.67f)
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
							moveTo(24.27f, 44.02f)
							lineTo(47.71f, 59.96f)
}
}
}
}.build()
return _ClearStorage!!
		}

