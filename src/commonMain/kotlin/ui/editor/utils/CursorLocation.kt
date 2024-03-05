package ui.editor.utils

import io.nacular.doodle.geometry.Point
import io.nacular.doodle.geometry.Vector2D

data class CursorLocation(val pos: Int, val lineID: Int, val columnID: Int, val coords: Pair<Point, Point>) {
    companion object {

        fun CursorLocation.moveUp(amount: Int, text: String, charWidth: Double, lineHeight: Double): CursorLocation {
            val lines = text.split("\n")
            var newLineID = this.lineID - amount
            if (newLineID < 0 || newLineID >= lines.size) {

            }
            val columnID = lines.getOrNull(newLineID)?.let {

            } ?: {

            }

            TODO()
        }

        fun getCursorLocation(position: Int, text: String, charWidth: Double, lineHeight: Double): CursorLocation {
            val textUntilChar = text.take(position)
            val lines = textUntilChar.split("\n")
            val lineID = lines.size - 1
            val columnID = lines.last().length - 1
            val coords = Vector2D(charWidth * columnID.toDouble(), lineHeight * lineID.toDouble()) to Vector2D(charWidth * columnID.toDouble(), lineHeight * (lineID + 1).toDouble())
            return CursorLocation(position, lineID, columnID, coords)
        }

        fun getCursorLocation(point: Point, text: String, charWidth: Double, lineHeight: Double): CursorLocation {
            val lines = text.split("\n")
            val lineID = (point.y / lineHeight).toInt().takeIf { it < lines.size } ?: (lines.size - 1)
            val theoreticalColumnID = ((point.x + charWidth / 2) / charWidth).toInt()
            val columnID = if (theoreticalColumnID in 0..lines[lineID].length) theoreticalColumnID else lines[lineID].length
            val actualLines = lines.take(lineID + 1)
            val position = actualLines.mapIndexed() { index, s ->
                when {
                    index < lineID -> s
                    index == lineID -> s.substring(0, columnID)
                    else -> {
                        ""
                    }
                }
            }.joinToString("\n") { it }.length - 1

            val coords = Vector2D(charWidth * columnID.toDouble(), lineHeight * lineID.toDouble()) to Vector2D(charWidth * columnID.toDouble(), lineHeight * (lineID + 1).toDouble())
            return CursorLocation(position, lineID, columnID, coords)
        }
    }

}
