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
    Image(Cancel, null)
}

private var _Cancel: ImageVector? = null

public val Cancel: ImageVector
		get() {
			if (_Cancel != null) {
				return _Cancel!!
			}
_Cancel = ImageVector.Builder(
                name = "Cancel",
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
    						stroke = SolidColor(Color(0xFF000000)),
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 6f,
    						strokeLineCap = StrokeCap.Round,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 10f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(19.38f, 19.38f)
							lineTo(71.33f, 71.33f)
}
						path(
    						fill = SolidColor(Color.Black),
    						fillAlpha = 1.0f,
    						stroke = SolidColor(Color(0xFF000000)),
    						strokeAlpha = 1.0f,
    						strokeLineWidth = 6f,
    						strokeLineCap = StrokeCap.Round,
    						strokeLineJoin = StrokeJoin.Miter,
    						strokeLineMiter = 10f,
    						pathFillType = PathFillType.NonZero
						) {
							moveTo(19.38f, 71.33f)
							lineTo(71.33f, 19.38f)
}
}
}
}.build()
return _Cancel!!
		}

