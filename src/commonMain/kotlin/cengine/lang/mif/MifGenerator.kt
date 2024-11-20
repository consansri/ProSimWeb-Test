package cengine.lang.mif

import cengine.lang.asm.ast.AsmCodeGenerator
import cengine.lang.obj.elf.LinkerScript
import cengine.lang.obj.elf.Shdr
import cengine.util.buffer.Buffer
import cengine.util.integer.Hex
import cengine.util.integer.Size
import cengine.util.integer.toValue

class MifGenerator<T : Buffer<*>>(linkerScript: LinkerScript, val addrSize: Size, val bufferInit: () -> T) : AsmCodeGenerator<AsmCodeGenerator.Section>(linkerScript) {
    override val fileSuffix: String
        get() = ".mif"

    override val sections: MutableList<Section> = mutableListOf()

    private val text = getOrCreateSection(".text", Shdr.SHT_text, Shdr.SHF_text.toULong())

    override var currentSection: Section = text

    override fun orderSectionsAndResolveAddresses() {
        linkerScript.textStart?.let {
            // .text
            var addr = it

            sections.filter { section ->
                section.isText()
            }.forEach { section ->
                section.address = addr
                addr += section.content.size.toULong().toValue()
            }
        }

        linkerScript.dataStart?.let {
            // .data, .bss
            var addr = it

            sections.filter { section ->
                section.isData()
            }.forEach { section ->
                section.address = addr
                addr += section.content.size.toULong().toValue()
            }
        }

        linkerScript.rodataStart?.let {
            // .rodata
            var addr = it

            sections.filter { section ->
                section.isRoData()
            }.forEach { section ->
                section.address = addr
                addr += section.content.size.toULong().toValue()
            }
        }
    }

    override fun writeFile(): ByteArray {
        val builder = MifConverter(text.content.wordWidth, addrSize, this::class.simpleName.toString())

        builder.setAddrRadix(MifConverter.Radix.HEX)
        builder.setDataRadix(MifConverter.Radix.HEX)

        sections.filter {
            it.isProg()
        }.forEach { section ->
            var addr = section.address
            section.content.toHexList().forEachIndexed { index, hex ->
                builder.addContent(addr, listOf(hex))
                addr += 1U.toValue(Size.Bit8)
            }
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
}