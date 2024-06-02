package me.c3.ui

import Constants
import java.io.File

data object Keys {

    val CONFIG_DIR = ".ide"

    val CONFIG_NAME = "setup.${Constants.NAME.lowercase()}"

    val IDE = "ide"

    val IDE_ARCH = "arch"
    val IDE_THEME = "theme"
    val IDE_SCALE = "scale"
    val IDE_ICONS = "icons"

    val ARCH_FEATURE = "feature"
    val ARCH_SETTING = "setting"

    fun getConfigFile(root: File): File {
        val configDir = File(root, CONFIG_DIR)
        if (!configDir.exists()) configDir.mkdir()
        val configFile = File(configDir, CONFIG_NAME)
        if (!configFile.exists()) configFile.createNewFile()
        return configFile
    }
}