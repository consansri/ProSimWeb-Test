package cengine.lang.asm.elf

interface ELFFile {

    val name: String
    val e_ident: E_IDENT
    val ehdr: Ehdr
    val sectionHeaders: List<Shdr>
    val programHeaders: List<Phdr>
    val symbolTable: List<Sym>?
    val dynamicSection: List<Dyn>?
    val relocationTables: Map<String, List<Rel>>
    val relocationTablesWithAddend: Map<String, List<Rela>>
    val noteHeaders: List<Nhdr>?

}