package dev.henkle.trees.ad

import java.util.*
import kotlin.math.min

class FilterTree {
    private val root = FilterTreeNode(isTerminal = false)
    val id get() = root.id

    fun addFilter(filter: String, exceptions: FilterTree? = null){
        root.addEdge(label = filter, exceptions = exceptions)
    }

    fun removeFilter(filter: String){
        root.removeEdge(label = filter)
    }

    fun matchesFilter(url: String, exact: Boolean): Boolean{
        val domain = getDomain(url = url)
        var currentCharIndex = domain.lastIndex
        var currentNode: FilterTreeNode? = root
        var currentEdge: FilterTreeEdge? = null
        var currentLabel: String?
        while(currentCharIndex >= 0){
            currentEdge = currentNode?.getEdge(domain[currentCharIndex--])
            currentLabel = currentEdge?.label
            if(currentLabel != null){
                for(i in 1 until min(currentLabel.length, domain.length)){
                    if(currentCharIndex < 0) break
                    if(currentLabel[i] != domain[currentCharIndex]) return false
                    currentCharIndex -= 1
                }
            }
            currentNode = currentEdge?.target
            if(currentNode == null) break
        }
        return if(!exact || currentEdge?.target?.isTerminal == true){
            println("would return true, but got ${currentEdge?.target?.exceptions?.matchesFilter(url = url, exact = exact)?.not() ?: false}")
            currentEdge?.target?.exceptions?.matchesFilter(url = url, exact = exact)?.not() ?: false
        } else false
    }

    private fun getDomain(url: String): String{
        var slashCount = 0
        var lastSlashIndex = 0
        url.forEachIndexed{ i, char ->
            if(char == '/'){
                if(slashCount == 2){
                    return url.substring(lastSlashIndex + 1, i)
                }
                lastSlashIndex = i
                slashCount += 1
            }
        }
        return if(slashCount == 2){
            url.substring(lastSlashIndex + 1)
        } else url
    }

    fun print() = root.print()

    fun serialize(
        chars: SerializationCharacters = SerializationCharacters()
    ): String = root.sprint(
        depth = 0,
        chars = chars
    )

    fun deserialize(
        serializedTree: String,
        chars: SerializationCharacters = SerializationCharacters()
    ){
        val stack = Stack<FilterTreeNode>().apply{ push(root) }
        val edges = serializedTree.split(chars.separator)
        var split: List<String>
        var label: String
        var exceptionTree: FilterTree?
        for(edge in edges){
//            todo println("${if(chars == SerializationCharacters.exceptions) "   " else ""}deserializing edge: '$edge'. Stack: $stack")
            if(edge.isEmpty()){
                stack.pop()
                continue
            }
            split = edge.split(chars.divider)
            label = split[1]
            exceptionTree = split[0].ifEmpty { null }?.let{ exceptions ->
                FilterTree().apply{
                    deserialize(serializedTree = exceptions, chars = SerializationCharacters.exceptions)
                }
            }
            when(label.last()){
                chars.leaf -> {
                    stack.peek().addEdge(
                        label = label.substring(0, label.lastIndex),
                        target = FilterTreeNode(isTerminal = true, exceptions = exceptionTree)
                    )
                }
                chars.nonTerminal -> {
                    val node = FilterTreeNode(isTerminal = false)
                    stack.apply{
                        peek().addEdge(
                            label = label.substring(0, label.lastIndex),
                            target = node
                        )
                        push(node)
                    }
                }
                else -> {
                    val node = FilterTreeNode(isTerminal = true, exceptions = exceptionTree)
                    stack.apply{
                        peek().addEdge(
                            label = label,
                            target = node
                        )
                        push(node)
                    }
                }
            }
        }
    }

    data class SerializationCharacters(
        val separator: Char = ',',
        val nonTerminal: Char = ')',
        val leaf: Char = ']',
        val divider: Char = '!'
    ){
        companion object{
            val exceptions = SerializationCharacters(
                separator = ';',
                nonTerminal = '(',
                leaf = '[',
                divider = '#'
            )
        }
    }
}
