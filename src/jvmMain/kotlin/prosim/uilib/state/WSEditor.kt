package prosim.uilib.state

import java.io.File

interface WSEditor {

    fun openFile(file: File)

    fun updateFile(file: File)

}