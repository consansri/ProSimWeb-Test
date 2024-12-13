package cengine.util.integer

interface UnsignedExtension {

    operator fun compareTo(other: UInt): Int
    operator fun compareTo(other: ULong): Int

}