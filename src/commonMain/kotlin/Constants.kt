data object Constants {
    const val NAME = "ProSim"
    const val WEBNAME = "${NAME}Web"
    const val JVMNAME= "${NAME}JVM"
    const val VERSION = "0.2.1"
    const val YEAR = "2023"
    const val ORG = "Universität Stuttgart IKR"
    const val DEV = "Constantin Birkert"

    data object WebStorageKey{
        const val ARCH_TYPE = "arch-type"

        // settings
        const val ARCH_SETTING = "setting"
        const val ARCH_FEATURE = "feature"
        const val THEME = "theme"

        // files
        const val FILE_CURR = "file-current"
        const val FILE_COUNT = "file-count"
        const val FILE_NAME = "filename"
        const val FILE_CONTENT = "filecontent"
        const val FILE_UNDO_LENGTH = "file-undo-length"
        const val FILE_REDO_LENGTH = "file-redo-length"
        const val FILE_UNDO = "file-undo"
        const val FILE_REDO = "file-redo"

        // memory
        const val MEM_LENGTH = "mem-length"
        const val MSTEP_VALUE = "m-step-value"
        const val MIO_ACTIVE = "mem-io-active"
        const val MIO_START = "mem-io-start"
        const val MIO_AMOUNT = "mem-io-amount"


        /* Console */
        const val CONSOLE_SDOWN = "console-sdown"
        const val CONSOLE_PIN = "console-pin"
        const val CONSOLE_SHOWINFO = "console-showInfo"
    }

}