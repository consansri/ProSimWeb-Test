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
    Image(ProcessorBold, null)
}

private var _ProcessorBold: ImageVector? = null

public val ProcessorBold: ImageVector
		get() {
			if (_ProcessorBold != null) {
				return _ProcessorBold!!
			}
_ProcessorBold = ImageVector.Builder(
                name = "ProcessorBold",
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
							moveTo(67.23f, 17.81f)
							horizontalLineTo(23.48f)
							arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, -5.67f, 5.67f)
							verticalLineTo(67.23f)
							arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.67f, 5.67f)
							horizontalLineTo(67.23f)
							arcToRelative(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, 5.67f, -5.67f)
							verticalLineTo(23.48f)
							arcTo(5.67f, 5.67f, 0f, isMoreThanHalf = false, isPositiveArc = false, 67.23f, 17.81f)
							close()
							moveTo(62.5f, 59.66f)
							arcToRelative(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2.84f, 2.84f)
							horizontalLineTo(31.05f)
							arcToRelative(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, -2.84f, -2.84f)
							verticalLineTo(31.05f)
							arcToRelative(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2.84f, -2.84f)
							horizontalLineTo(59.66f)
							arcToRelative(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 2.84f, 2.84f)
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
							moveTo(31.05f, 28.21f)
							horizontalLineTo(59.66f)
							arcTo(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 62.5f, 31.05f)
							verticalLineTo(59.66f)
							arcTo(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 59.66f, 62.5f)
							horizontalLineTo(31.05f)
							arcTo(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28.21f, 59.66f)
							verticalLineTo(31.05f)
							arcTo(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 31.05f, 28.21f)
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
							moveTo(31.05f, 28.21f)
							horizontalLineTo(59.66f)
							arcTo(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 62.5f, 31.05f)
							verticalLineTo(59.66f)
							arcTo(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 59.66f, 62.5f)
							horizontalLineTo(31.05f)
							arcTo(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28.21f, 59.66f)
							verticalLineTo(31.05f)
							arcTo(2.84f, 2.84f, 0f, isMoreThanHalf = false, isPositiveArc = true, 31.05f, 28.21f)
							close()
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = null,
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 4f,
    						strokeLineCap = StrokeCap.Round,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(72.9f, 31.48f)
							lineTo(84.11f, 31.48f)
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = null,
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 4f,
    						strokeLineCap = StrokeCap.Round,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(72.9f, 44.17f)
							lineTo(84.11f, 44.17f)
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = null,
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 4f,
    						strokeLineCap = StrokeCap.Round,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(72.9f, 56.85f)
							lineTo(84.11f, 56.85f)
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = null,
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 4f,
    						strokeLineCap = StrokeCap.Round,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(6.59f, 33.86f)
							lineTo(17.81f, 33.86f)
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = null,
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 4f,
    						strokeLineCap = StrokeCap.Round,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(6.59f, 46.54f)
							lineTo(17.81f, 46.54f)
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = null,
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 4f,
    						strokeLineCap = StrokeCap.Round,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(6.59f, 59.23f)
							lineTo(17.81f, 59.23f)
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = null,
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 4f,
    						strokeLineCap = StrokeCap.Round,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(31.48f, 17.81f)
							lineTo(31.48f, 6.59f)
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = null,
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 4f,
    						strokeLineCap = StrokeCap.Round,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(44.17f, 17.81f)
							lineTo(44.17f, 6.59f)
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = null,
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 4f,
    						strokeLineCap = StrokeCap.Round,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(56.85f, 17.81f)
							lineTo(56.85f, 6.59f)
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = null,
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 4f,
    						strokeLineCap = StrokeCap.Round,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(33.86f, 84.11f)
							lineTo(33.86f, 72.9f)
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = null,
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 4f,
    						strokeLineCap = StrokeCap.Round,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(46.54f, 84.11f)
							lineTo(46.54f, 72.9f)
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = null,
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 4f,
    						strokeLineCap = StrokeCap.Round,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 1.0f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(59.23f, 84.11f)
							lineTo(59.23f, 72.9f)
}
}
}
}.build()
return _ProcessorBold!!
		}

