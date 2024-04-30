package emulator.kit.assembler.parser

import emulator.kit.assembler.lexer.Token
import kotlin.reflect.KClass

class NodeSeq(val components: Component) {

    companion object {
        inline fun <reified T : Token> getID(vararg tokens: T): String = T::class.simpleName.toString()
    }

    fun matchStart(vararg baseNodes: Node): Result {
        return components.parse(baseNodes.toList())
    }

    sealed class Component {
        abstract fun parse(nodes: List<Node>): Result

        class Any : Component() {
            override fun parse(nodes: List<Node>): Result {
                if (nodes.isEmpty()) return Result(false, listOf(), listOf())
                return Result(true, listOf(nodes.first()), nodes - nodes.first())
            }
        }

        class Or(vararg val comps: Component) : Component() {
            override fun parse(nodes: List<Node>): Result {
                for (comp in comps) {
                    val result = comp.parse(nodes)
                    if (result.matches) {
                        return result
                    }
                }
                return Result(false, listOf(), nodes)
            }
        }

        class Optional(val comp: Component) : Component() {
            override fun parse(nodes: List<Node>): Result {
                val result = comp.parse(nodes)
                if(result.matches){
                    return Result(true, result.matchingTreeNodes, result.remainingNodes)
                }else{
                    return Result(true, listOf(), nodes)
                }
            }
        }

        class Sequence(vararg val comps: Component) : Component() {
            override fun parse(nodes: List<Node>): Result {
                val matchingNodes = mutableListOf<Node>()
                val remainingTreeNodes = nodes.toMutableList()
                for (comp in comps) {
                    val result = comp.parse(remainingTreeNodes)
                    if (!result.matches) return Result(false, listOf(), nodes)
                    matchingNodes.addAll(result.matchingTreeNodes)
                    remainingTreeNodes.clear()
                    remainingTreeNodes.removeAll(result.matchingTreeNodes)
                    if (remainingTreeNodes.isEmpty()) break
                }
                return Result(true, matchingNodes, remainingTreeNodes)
            }
        }

        class Ref<T : Node>(private val typeClass: KClass<T>) : Component() {
            override fun parse(nodes: List<Node>): Result {
                val first = nodes.firstOrNull() ?: return Result(false, listOf(), nodes)
                if (typeClass.isInstance(first)) {
                    return Result(true, listOf(first), nodes - first)
                }
                return Result(false, listOf(), nodes)
            }
        }

        class Repeatable(val comp: Component, val retFalseIfEmpty: Boolean = false) : Component() {
            override fun parse(nodes: List<Node>): Result {
                val remainingNodes = nodes.toMutableList()
                val matchingNodes = mutableListOf<Node>()
                var result: Result? = null
                while (remainingNodes.isNotEmpty()) {
                    result = comp.parse(remainingNodes)
                    if (!result.matches) {
                        val matches = if (retFalseIfEmpty) matchingNodes.isNotEmpty() else true
                        return Result(matches, matchingNodes, remainingNodes)
                    }

                    matchingNodes.addAll(result.matchingTreeNodes)
                    remainingNodes.removeAll(result.matchingTreeNodes)
                }

                val matches = if (retFalseIfEmpty) matchingNodes.isNotEmpty() else true
                return Result(matches, matchingNodes, remainingNodes)
            }
        }
    }


    data class Result(val matches: Boolean, val matchingTreeNodes: List<Node>, val remainingNodes: List<Node>)
}