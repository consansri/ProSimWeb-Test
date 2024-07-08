package cengine.editor.selection

data class Selection(
    var start: Int? = null,
    var end: Int? = null
) {

    fun select(from: Int?, to: Int?){
        start = from
        end = to
    }

    fun deselect(){
        select(null,null)
    }

    fun select(pair: Pair<Int?, Int?>){
        select(pair.first, pair.second)
    }

    fun valid(): Boolean{
        return start != null && end != null
    }

    fun moveStart(newEnd: Int){
        end = newEnd
    }

    fun moveEnd(newStart: Int){
        start = newStart
    }
}