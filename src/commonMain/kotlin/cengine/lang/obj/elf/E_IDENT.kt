package cengine.lang.obj.elf

import cengine.lang.obj.elf.Ehdr.Companion.EV_CURRENT
import cengine.util.Endianness
import cengine.util.buffer.ByteBuffer

/**
 * @param ei_mag0 File identification
 * @param ei_mag1 File identification
 * @param ei_mag2 File identification
 * @param ei_mag3 File identification
 * @param ei_class File class
 * @param ei_data Data encoding
 * @param ei_version File version
 * @param ei_osabi
 * @param ei_abiversion
 * @param ei_pad Start of padding bytes
 * @param ei_nident Size of [E_IDENT.build]
 */
data class E_IDENT(
    val ei_mag0: cengine.lang.obj.elf.Elf_Byte = ELFMAG0,
    val ei_mag1: cengine.lang.obj.elf.Elf_Byte = ELFMAG1,
    val ei_mag2: cengine.lang.obj.elf.Elf_Byte = ELFMAG2,
    val ei_mag3: cengine.lang.obj.elf.Elf_Byte = ELFMAG3,
    val ei_class: cengine.lang.obj.elf.Elf_Byte,
    val ei_data: cengine.lang.obj.elf.Elf_Byte,
    val ei_version: cengine.lang.obj.elf.Elf_Byte = EV_CURRENT.toUByte(),
    val ei_osabi: cengine.lang.obj.elf.Elf_Byte = ZERO,
    val ei_abiversion: cengine.lang.obj.elf.Elf_Byte = ZERO,
    val ei_pad: cengine.lang.obj.elf.Elf_Byte = ZERO,
    val ei_nident: cengine.lang.obj.elf.Elf_Byte = EI_NIDENT
) : BinaryProvider {

    val endianness: Endianness get() = when(ei_data){
        ELFDATA2MSB -> Endianness.BIG
        else -> Endianness.LITTLE
    }

    companion object {

        fun getOsAbi(ei_osabi: cengine.lang.obj.elf.Elf_Byte): String{
            return when(ei_osabi){
                ELFOSABI_SYSV -> "UNIX - System V"
                else -> "?"
            }
        }

        fun getElfClass(ei_class: cengine.lang.obj.elf.Elf_Byte): String{
            return when(ei_class){
                ELFCLASS32 -> "ELF32"
                ELFCLASS64 -> "ELF64"
                ELFCLASSNONE -> "NONE"
                else -> "INVALID"
            }
        }

        fun getElfData(ei_data: cengine.lang.obj.elf.Elf_Byte): String{
            return when(ei_data){
                ELFDATA2MSB -> "2's complement, big endian"
                ELFDATA2LSB -> "2's complement, little endian"
                ELFDATANONE -> "NONE"
                else -> "INVALID"
            }
        }

        const val ZERO: cengine.lang.obj.elf.Elf_Byte = 0U

        const val EI_NIDENT: cengine.lang.obj.elf.Elf_Byte = 16U

        const val ELFMAG0: cengine.lang.obj.elf.Elf_Byte = 0x7fU

        /**
         * E in 7 bit ascii
         */
        const val ELFMAG1: cengine.lang.obj.elf.Elf_Byte = 0x45U

        /**
         * L in 7 bit ascii
         */
        const val ELFMAG2: cengine.lang.obj.elf.Elf_Byte = 0x4CU

        /**
         * F in 7 bit ascii
         */
        const val ELFMAG3: cengine.lang.obj.elf.Elf_Byte = 0x46U

        /**
         * Invalid class
         */
        const val ELFCLASSNONE: cengine.lang.obj.elf.Elf_Byte = 0U

        /**
         * 32-bit objects
         */
        const val ELFCLASS32: cengine.lang.obj.elf.Elf_Byte = 1U

        /**
         * 64-bit objects
         */
        const val ELFCLASS64: cengine.lang.obj.elf.Elf_Byte = 2U

        /**
         * Invalid data encoding
         */
        const val ELFDATANONE: cengine.lang.obj.elf.Elf_Byte = 0U

        /**
         * Least significant byte occupying the lowest address.
         */
        const val ELFDATA2LSB: cengine.lang.obj.elf.Elf_Byte = 1U

        /**
         * The most significant byte occupying the lowest address.
         */
        const val ELFDATA2MSB: cengine.lang.obj.elf.Elf_Byte = 2U

        /**
         * This indicates that the ELF file follows the System V ABI.
         */
        const val ELFOSABI_SYSV: cengine.lang.obj.elf.Elf_Byte = 0U

        fun extractFrom(byteArray: ByteArray): E_IDENT {
            val ei_mag0 = byteArray.getOrNull(0)?.toUByte() ?: throw cengine.lang.obj.elf.NotInELFFormatException
            if (ei_mag0 != ELFMAG0) throw cengine.lang.obj.elf.NotInELFFormatException

            val ei_mag1 = byteArray.getOrNull(1)?.toUByte() ?: throw cengine.lang.obj.elf.NotInELFFormatException
            if (ei_mag1 != ELFMAG1) throw cengine.lang.obj.elf.NotInELFFormatException

            val ei_mag2 = byteArray.getOrNull(2)?.toUByte() ?: throw cengine.lang.obj.elf.NotInELFFormatException
            if (ei_mag2 != ELFMAG2) throw cengine.lang.obj.elf.NotInELFFormatException

            val ei_mag3 = byteArray.getOrNull(3)?.toUByte() ?: throw cengine.lang.obj.elf.NotInELFFormatException
            if (ei_mag3 != ELFMAG3) throw cengine.lang.obj.elf.NotInELFFormatException

            val ei_class = byteArray.getOrNull(4)?.toUByte() ?: throw cengine.lang.obj.elf.NotInELFFormatException
            if (!(ei_class == ELFCLASSNONE || ei_class == ELFCLASS32 || ei_class == ELFCLASS64)) throw Exception("Incompatible ELF Class $ei_class!")

            val ei_data = byteArray.getOrNull(5)?.toUByte() ?: throw cengine.lang.obj.elf.NotInELFFormatException
            if (!(ei_data == ELFDATANONE || ei_data == ELFDATA2MSB || ei_data == ELFDATA2LSB)) throw Exception("Incompatible ELF Data $ei_class!")

            val ei_version = byteArray.getOrNull(6)?.toUByte() ?: throw cengine.lang.obj.elf.NotInELFFormatException
            if (ei_version != EV_CURRENT.toUByte()) throw Exception("Invalid ELF Version $ei_version!")

            val ei_osabi = byteArray.getOrNull(7)?.toUByte() ?: throw cengine.lang.obj.elf.NotInELFFormatException

            val ei_abiversion = byteArray.getOrNull(8)?.toUByte() ?: throw cengine.lang.obj.elf.NotInELFFormatException

            val ei_pad = byteArray.getOrNull(9)?.toUByte() ?: throw cengine.lang.obj.elf.NotInELFFormatException

            var paddIndex = 10
            while (byteArray.getOrNull(paddIndex) == 0.toByte()) {
                paddIndex++
            }
            val ei_nident = byteArray.getOrNull(paddIndex)?.toUByte() ?: throw cengine.lang.obj.elf.NotInELFFormatException

            return E_IDENT(
                ei_mag0,
                ei_mag1,
                ei_mag2,
                ei_mag3,
                ei_class,
                ei_data,
                ei_version,
                ei_osabi,
                ei_abiversion,
                ei_pad,
                ei_nident
            )
        }

    }

    override fun build(endianness: Endianness): Array<Byte> {
        val buffer = ByteBuffer(endianness)

        buffer.put(ei_mag0)
        buffer.put(ei_mag1)
        buffer.put(ei_mag2)
        buffer.put(ei_mag3)
        buffer.put(ei_class)
        buffer.put(ei_data)
        buffer.put(ei_version)
        buffer.put(ei_osabi)
        buffer.put(ei_abiversion)
        buffer.put(ei_pad)
        try {
            buffer.putAll(ByteArray(ei_nident.toInt() - buffer.size) {
                ZERO.toByte()
            })
        }catch (e: Exception){
            buffer.putAll(ByteArray(EI_NIDENT.toInt() - buffer.size){
                ZERO.toByte()
            })
        }

        return buffer.toArray()
    }

    override fun byteSize(): Int = ei_nident.toInt()

    override fun toString(): String {
        return "ELF Class:${getElfClass(ei_class)} Data:${getElfData(ei_data)} Version:$ei_version"
    }
}