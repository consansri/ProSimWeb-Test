package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
import prosim.uilib.styled.params.IconSize
import java.awt.BorderLayout
import javax.swing.SwingConstants

class CNumberPicker<T : Number>(private val model: Model<T>, private val onChange: (T) -> Unit = {}) : CPanel(roundCorners = true) {

    private val currValue = CLabel(model.default.toString(), FontType.CODE).apply {
        horizontalAlignment = SwingConstants.CENTER
        verticalAlignment = SwingConstants.CENTER
    }

    private val increase = CIconButton(UIStates.icon.get().increase, IconSize.PRIMARY_SMALL).apply {
        addActionListener {
            value = model.next(value)
        }
    }

    private val decrease = CIconButton(UIStates.icon.get().decrease, IconSize.PRIMARY_SMALL).apply {
        addActionListener {
            value = model.prev(value)
        }
    }

    var value: T = model.default
        private set(value) {
            if (model.valid(value)) {
                field = value
                currValue.text = value.toString()
                onChange(value)
            }
        }

    init {
        layout = BorderLayout()

        add(decrease, BorderLayout.WEST)
        add(currValue, BorderLayout.CENTER)
        add(increase, BorderLayout.EAST)
    }

    sealed class Model<T : Number>(val min: T, val max: T, val step: T, val default: T) {
        /**
         * @return next value = [curr] + [step].
         */
        abstract fun next(curr: T): T

        /**
         * @return prev value = [curr] - [step].
         */
        abstract fun prev(curr: T): T

        /**
         * Check if [value] is valid.
         *
         * @return [value] is valid
         */
        abstract fun valid(value: T): Boolean
    }

    class IntModel(min: Int, max: Int, step: Int, default: Int) : Model<Int>(min, max, step, default) {
        override fun next(curr: Int): Int = curr + step

        override fun prev(curr: Int): Int = curr - step

        override fun valid(value: Int): Boolean {
            if (value !in min..max) return false

            if ((value - min) % step != 0) return false

            return true
        }
    }

}