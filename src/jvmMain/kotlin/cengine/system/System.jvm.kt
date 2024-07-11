package cengine.system

actual fun getSystemLineBreak(): String {
    val osName = System.getProperty("os.name").lowercase()
    return when{
        osName.contains("win") -> "\r\n" // windows
        osName.contains("mac") -> "\n" // macOS
        osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> "\n" // Unix/Linux
        else -> "\n"
    }
}