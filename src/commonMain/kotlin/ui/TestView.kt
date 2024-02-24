package ui

import io.nacular.doodle.core.Display
import io.nacular.doodle.core.View
import io.nacular.doodle.core.renderProperty
import io.nacular.doodle.drawing.Canvas
import io.nacular.doodle.drawing.Color
import io.nacular.doodle.drawing.TextMetrics
import io.nacular.doodle.drawing.text
import io.nacular.doodle.geometry.Point

class TestView(display: Display,textMetrics: TextMetrics, message: String): View() {
    var message by renderProperty(message)

    private val messageHeight = textMetrics.height(message)

    init {
        size = display.size
    }

    override fun render(canvas: Canvas) {
        canvas.text("message: $message", color = Color.Black)
        canvas.text("next line!", at = Point(y = messageHeight), color = Color.Blue)

        println("render ${this::class.simpleName}")
    }


}