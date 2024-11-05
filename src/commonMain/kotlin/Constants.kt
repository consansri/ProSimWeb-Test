import config.BuildConfig

data object Constants {
    const val NAME = BuildConfig.NAME
    val WEBNAME = "${NAME}Web"
    val JVMNAME= "${NAME}JVM"
    const val VERSION = BuildConfig.VERSION
    const val YEAR = BuildConfig.YEAR
    const val ORG = BuildConfig.ORG
    const val DEV = BuildConfig.DEV
    const val TITLE = "$NAME - $VERSION"
}