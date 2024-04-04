package me.c3.ui.components.editor

import javax.swing.text.*

class NoWrapEditorKit: StyledEditorKit() {
    override fun getViewFactory(): ViewFactory {
        return NoWrapViewFactory()
    }

    inner class NoWrapViewFactory : ViewFactory {
        override fun create(elem: Element): View {
            val elementName = elem.name
            return when {
                elementName != null && elementName == AbstractDocument.ContentElementName -> NoWrapParagraphView(elem)
                elementName != null && elementName == AbstractDocument.SectionElementName -> BoxView(elem, View.Y_AXIS)
                elementName != null && elementName == StyleConstants.ComponentElementName -> ComponentView(elem)
                elementName != null && elementName == StyleConstants.IconElementName -> IconView(elem)
                else -> LabelView(elem)
            }
        }
    }

    inner class NoWrapParagraphView(elem: Element) : ParagraphView(elem) {
        override fun layout(width: Int, height: Int) {
            super.layout(Int.MAX_VALUE, height)
        }
    }

}