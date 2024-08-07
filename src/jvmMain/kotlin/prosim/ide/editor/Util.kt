package prosim.ide.editor

import java.awt.Color
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

fun copyToClipboard(text: String) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(StringSelection(text), null)
}

fun getClipboardContent(): String? {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val clipboardData = clipboard.getContents(null)
    return if (clipboardData != null && clipboardData.isDataFlavorSupported(DataFlavor.stringFlavor)) {
        clipboardData.getTransferData(DataFlavor.stringFlavor) as String
    } else {
        null
    }
}

fun Int?.toColor(foreground: Color): Color {
    return if (this == null) foreground else Color(this)
}


