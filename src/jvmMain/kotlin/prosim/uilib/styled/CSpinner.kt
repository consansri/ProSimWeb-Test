package prosim.uilib.styled

import emulator.kit.nativeLog
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import javax.swing.JSpinner
import javax.swing.SpinnerModel
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class CSpinner(model: SpinnerModel, fontType: FontType = FontType.BASIC,val borderMode: BorderMode = BorderMode.HORIZONTAL) : JSpinner(model) {

    var fontType: FontType = fontType
        set(value) {
            field = value
            (ui as? CSpinnerUI)?.setDefaults(this)
        }

    init {
        setUI(CSpinnerUI())
        val editor = editor

        if(editor is DefaultEditor){
            editor.textField.document.addDocumentListener(object : DocumentListener{
                override fun insertUpdate(e: DocumentEvent?) {
                    commitEditorValue()
                }

                override fun removeUpdate(e: DocumentEvent?) {
                    commitEditorValue()
                }

                override fun changedUpdate(e: DocumentEvent?) {
                    commitEditorValue()
                }
            })
        }
    }

    fun commitEditorValue(){
        try {
            nativeLog("Committing Edit!")
            commitEdit()
        }catch (e: Exception){
            // parse exception handling not needed
        }
    }

}