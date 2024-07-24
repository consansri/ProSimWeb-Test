package prosim.uilib.styled

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import prosim.uilib.styled.params.FontType

/**
 * A generic chooser component that allows selection from a list of entries.
 *
 * @param T The type of the entries.
 * @property model The model containing the entries, default entry, and display name.
 * @property fontType The font type used for displaying text.
 * @property onSelect A lambda function that is called when an entry is selected.
 */
open class CChooser<T : Any>(val model: Model<T>, fontType: FontType, val onSelect: suspend (T) -> Unit = {}, primary: Boolean = true) : CTextButton(model.getUIName(model.default), fontType, primary = primary) {

    private var openDialog: Pair<CDialog, Deferred<T?>>? = null

    /**
     * The currently selected entry.
     */
    var value: T = model.default
        set(value) {
            field = value
            text = model.getUIName(value)
        }

    init {
        text = model.getUIName(value)
        addActionListener {
            val currOpenDialog = openDialog
            if (currOpenDialog != null) {
                openDialog = null
                currOpenDialog.first.dispose()
                try {
                    currOpenDialog.second.cancel()
                } catch (_: Exception) {                }
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    val (dialog, result) = COptionPane.showSelector(this@CChooser, "Select ${this@CChooser.model.name}", this@CChooser.model.entries)
                    openDialog = dialog to result
                    result.await()?.let { resultNotNull ->
                        openDialog = null
                        this@CChooser.value = resultNotNull
                        onSelect(value)
                    }
                }
            }
        }
    }

    /**
     * Data model for the chooser containing the entries, default entry, and name.
     *
     * @param T The type of the entries.
     * @property entries List of entries to choose from.
     * @property default The default selected entry.
     * @property name The name used for displaying in the chooser.
     */
    data class Model<T : Any>(val entries: List<T>, val default: T, val name: String? = null) {

        /**
         * Gets the display name for a given entry.
         *
         * @param value The entry to get the display name for.
         * @return The display name of the entry.
         */
        fun getUIName(value: T): String = if (name != null) "$name: $value" else value.toString()
    }
}