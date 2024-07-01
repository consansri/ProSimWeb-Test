package cengine.structures

class StringModel(private var text: String) : Code {
    override val length: Int get() =  text.length

    override fun insert(index: Int, new: String) {
        text = text.substring(0, index) + new + text.substring(index)
    }

    override fun delete(start: Int, end: Int) {
        text = text.substring(0, start) + text.substring(end)
    }

    override fun substring(start: Int, end: Int): String {
        return text.substring(start, end)
    }

    override fun charAt(index: Int): Char {
        return text[index]
    }

    override fun toString(): String = text
}