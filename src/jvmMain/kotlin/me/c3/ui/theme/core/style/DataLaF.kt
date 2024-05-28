package me.c3.ui.theme.core.style

import emulator.kit.common.memory.Memory
import java.awt.Color

data class DataLaF(val getMemInstanceColor: (Memory.InstanceType) -> Color)