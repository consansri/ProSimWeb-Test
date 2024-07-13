package prosim.uilib

import java.awt.Color

/**
 * @return [Color] modified with a certain 0-255 [alpha] value.
 */
fun Color.alpha(alpha: Int): Color{
    // Ensure alpha is within the valid range [0, 255]
    val validAlpha = alpha.coerceIn(0, 255)

    // Create and return a new Color object with the specified alpha value
    return Color(red, green, blue, validAlpha)
}