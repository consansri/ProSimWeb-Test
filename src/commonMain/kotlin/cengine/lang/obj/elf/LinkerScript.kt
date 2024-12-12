package cengine.lang.obj.elf

import cengine.util.newint.BigInt

interface LinkerScript {

    val textStart: BigInt?
    val dataStart: BigInt?
    val rodataStart: BigInt?
    val segmentAlign: UInt

}