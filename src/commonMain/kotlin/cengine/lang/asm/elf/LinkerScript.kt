package cengine.lang.asm.elf

import cengine.util.integer.Hex

interface LinkerScript {

    val textStart: Hex?
    val dataStart: Hex?
    val rodataStart: Hex?
    val segmentAlign: UInt

}