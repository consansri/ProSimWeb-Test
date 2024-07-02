package cengine.selection

data class Selection(
    var start: Int? = null,
    var end: Int? = null
) {

    fun select(from: Int, to: Int){

    }

    fun deselect(){
        start = null
        end = null
    }

    fun moveStart(newEnd: Int){
        end = newEnd
    }

    fun moveEnd(newStart: Int){
        start = newStart
    }

}