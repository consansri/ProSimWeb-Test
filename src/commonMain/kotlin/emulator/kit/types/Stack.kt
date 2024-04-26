package emulator.kit.types

class Stack<T> {

    private val elements = mutableListOf<T>()

    fun push(element: T){
        elements.add(element)
    }

    fun pop(): T? {
        if(isEmpty()) return null
        return elements.removeAt(elements.size - 1)
    }

    fun peek(): T?{
        if(elements.isEmpty()) return null
        return elements[elements.size - 1]
    }

    fun isEmpty(): Boolean{
        return elements.isEmpty()
    }

    fun size(): Int{
        return elements.size
    }

}