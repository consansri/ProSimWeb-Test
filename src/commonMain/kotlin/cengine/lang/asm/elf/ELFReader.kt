package cengine.lang.asm.elf

/**
 * Throws Exception if the file is not matching the ELF Format.
 */
class ELFReader(val fileContent: ByteArray) {
    val e_ident: E_IDENT = E_IDENT.extractFrom(fileContent)
    val ehdr: Ehdr<*, *> = Ehdr.extractFrom(fileContent, e_ident)


    init {

    }

    fun checkIdent() {

    }

}