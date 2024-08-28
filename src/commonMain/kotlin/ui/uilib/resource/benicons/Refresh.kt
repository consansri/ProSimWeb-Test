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
    Image(Refresh, null)
}

private var _Refresh: ImageVector? = null

public val Refresh: ImageVector
		get() {
			if (_Refresh != null) {
				return _Refresh!!
			}
_Refresh = ImageVector.Builder(
                name = "Refresh",
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
							moveTo(0f, 90.71f)
							lineTo(0f, 0f)
							lineTo(90.71f, 0f)
							lineTo(90.71f, 90.71f)
}
}
					group {
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
							moveTo(19.96f, 28.47f)
							lineTo(19.95f, 28.47f)
							lineTo(19.95f, 11.44f)
							lineTo(19.95f, 28.47f)
							lineTo(36.98f, 28.47f)
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
							moveTo(75.25f, 45.35f)
							arcToRelative(29.9f, 29.9f, 0f, isMoreThanHalf = false, isPositiveArc = false, -51f, -21.14f)
							lineTo(20f, 28.47f)
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
							moveTo(70.75f, 62.25f)
							lineTo(70.75f, 62.24f)
							lineTo(70.75f, 62.24f)
							lineTo(70.75f, 79.27f)
							lineTo(70.75f, 62.24f)
							lineTo(53.73f, 62.24f)
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
							moveTo(15.45f, 45.35f)
							arcTo(29.91f, 29.91f, 0f, isMoreThanHalf = false, isPositiveArc = false, 66.5f, 66.5f)
							lineToRelative(4.25f, -4.25f)
}
}
}
}.build()
return _Refresh!!
		}

