package cengine.system

actual fun getSystemLineBreak(): String = "\n"
actual fun isAbsolutePathValid(path: String): Boolean {
    TODO("Not yet implemented")
}

actual fun appTarget(): AppTarget = AppTarget.WEB
actual fun downloadDesktopApp(fileNameSuffix: String) {
}