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
    Image(Asmfile, null)
}

private var _Asmfile: ImageVector? = null

public val Asmfile: ImageVector
		get() {
			if (_Asmfile != null) {
				return _Asmfile!!
			}
_Asmfile = ImageVector.Builder(
                name = "Asmfile",
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
    						strokeLineCap = StrokeCap.Butt,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(29.35f, 23.85f)
							lineToRelative(-5f, 9.74f)
							horizontalLineTo(44.43f)
							lineToRelative(-5f, -9.74f)
							arcTo(5.66f, 5.66f, 0f, isMoreThanHalf = false, isPositiveArc = false, 29.35f, 23.85f)
							close()
}
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
							moveTo(24.35f, 33.59f)
							lineTo(24.35f, 40.87f)
}
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
							moveTo(44.43f, 33.59f)
							lineTo(44.43f, 40.87f)
}
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
							moveTo(47.21f, 71.31f)
							horizontalLineTo(61.33f)
							arcToRelative(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5f, -5f)
							horizontalLineToRelative(0f)
							arcToRelative(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5f, -5f)
							horizontalLineToRelative(-10f)
							arcToRelative(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5f, -5f)
							horizontalLineToRelative(0f)
							arcToRelative(5f, 5f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5f, -5f)
							horizontalLineTo(65.42f)
}
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
							moveTo(50.29f, 20.79f)
							horizontalLineTo(60.68f)
							arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, 5.67f, 5.67f)
							verticalLineTo(40.87f)
}
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
							moveTo(36.93f, 71.31f)
							horizontalLineTo(30f)
							arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = true, -5.67f, -5.67f)
							verticalLineTo(51.23f)
}
}
}
}.build()
return _Asmfile!!
		}

