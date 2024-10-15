package cengine.lang.asm.elf

object ELFTool {

    init {

    }

    fun linkRelToExe(elfReader: ELFReader): ByteArray {
        if (elfReader.ehdr.e_type != Ehdr.ET_REL) throw ELFLinkerException("Expected Relocatable ELF file! (received: ${Ehdr.getELFType(elfReader.ehdr.e_type)})")


        return ByteArray(0)
    }

    class ELFLinkerException(message: String) : Exception(message)

}