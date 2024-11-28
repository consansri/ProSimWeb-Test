package cengine.lang.mif

import cengine.lang.asm.ast.AsmCodeGenerator
import cengine.lang.obj.elf.LinkerScript
import cengine.lang.obj.elf.Shdr
import cengine.util.buffer.Buffer
import cengine.util.integer.*
import cengine.util.integer.Value.Companion.toValue

class MifGenerator<T : Buffer<*>>(linkerScript: LinkerScript, val addrSize: Size, val bufferInit: () -> T) : AsmCodeGenerator<AsmCodeGenerator.Section>(linkerScript) {
    override val fileSuffix: String
        get() = ".mif"

    override val sections: MutableList<Section> = mutableListOf()

    private val text = getOrCreateSection(".text", Shdr.SHT_text, Shdr.SHF_text.toULong())

    override var currentSection: Section = text

    override fun orderSectionsAndResolveAddresses() {
        var globalAddress = 0U.toValue(addrSize)

        // .text
        var addr = linkerScript.textStart ?: globalAddress

        sections.filter { section ->
            section.isText()
        }.forEach { section ->
            section.address = addr
            addr += section.content.size.toULong().toValue(addrSize)
        }

        globalAddress = addr

        // .data, .bss
        addr = linkerScript.dataStart ?: globalAddress

        sections.filter { section ->
            section.isData()
        }.forEach { section ->
            section.address = addr
            addr += section.content.size.toULong().toValue(addrSize)
        }

        globalAddress = addr

        // .rodata
        addr = linkerScript.rodataStart ?: globalAddress

        sections.filter { section ->
            section.isRoData()
        }.forEach { section ->
            section.address = addr
            addr += section.content.size.toULong().toValue(addrSize)
        }
    }

    override fun writeFile(): ByteArray {
        val builder = MifConverter(text.content.wordWidth, addrSize, this::class.simpleName.toString())

        builder.setAddrRadix(Radix.HEX)
        builder.setDataRadix(Radix.HEX)

        sections.filter {
            it.isProg()
        }.forEach { section ->
            val addr = section.address
            builder.addContent(addr, section.content.toHexList())
        }

        return builder.build().encodeToByteArray()
    }

    override fun createNewSection(name: String, type: UInt, flags: ULong, link: Section?, info: String?): Section {
        return object : Section {
            override val name: String = name
            override var type: UInt = type
            override var flags: ULong = flags
            override var link: Section? = link
            override var info: String? = info
            override var address: Hex = Hex("0")
            override val content: Buffer<*> = bufferInit()
            override val reservations: MutableList<InstrReservation> = mutableListOf()
        }
    }

    companion object {
        fun String.rdx(radix: Radix, size: Size): Value {
            return when (radix) {
                Radix.HEX -> Hex(this, size)
                Radix.OCT -> Oct(this, size)
                Radix.BIN -> Bin(this, size)
                Radix.DEC -> Dec(this, size)
            }
        }

        fun Value.rdx(radix: Radix): String {
            return when (radix) {
                Radix.HEX -> toHex().rawInput
                Radix.OCT -> toOct().rawInput
                Radix.BIN -> toBin().rawInput
                Radix.DEC -> toUDec().rawInput
            }
        }
    }

    enum class Radix(val radix: Int) {
        HEX(16),
        OCT(8),
        BIN(2),
        DEC(10);

        companion object {
            fun getRadix(string: String): Radix {
                return entries.firstOrNull { it.name == string.uppercase() } ?: HEX
            }
        }
    }
}