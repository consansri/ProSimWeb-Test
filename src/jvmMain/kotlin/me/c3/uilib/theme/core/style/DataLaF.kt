package me.c3.uilib.theme.core.style

import emulator.kit.memory.Memory
import java.awt.Color

data class DataLaF(val getMemInstanceColor: (Memory.InstanceType) -> Color)