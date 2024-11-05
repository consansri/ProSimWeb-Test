package cengine.system

expect fun getSystemLineBreak(): String
expect fun isAbsolutePathValid(path: String): Boolean

expect fun downloadDesktopApp(fileNameSuffix: String)

expect fun appTarget(): AppTarget

enum class AppTarget{
    WEB,
    DESKTOP;
}
