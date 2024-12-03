import config.BuildConfig

data object Constants {
    const val NAME = BuildConfig.NAME
    const val VERSION = BuildConfig.VERSION
    const val TITLE = "$NAME - $VERSION"
}