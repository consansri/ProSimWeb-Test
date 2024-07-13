package cengine.util.text

interface CharFilter {

    fun accept(ch: Char): Boolean

}