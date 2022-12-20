package dev.henkle.trees.ad

import java.util.*
import kotlin.math.min

class FilterTree {
    private val root = FilterTreeNode(isTerminal = false)

    fun addFilter(filter: String, exceptions: FilterTree?){
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
        var currentLabel: String? = null
        while(currentCharIndex >= 0){
            currentEdge = currentNode?.getEdge(domain[currentCharIndex--])
            currentLabel = currentEdge?.label
            if(currentLabel != null){
                for(i in 1 until min(currentLabel.length, domain.length)){
                    if(currentCharIndex < 0) break
                    if(currentLabel[i] != domain[currentCharIndex]){
                        return false
                    }
                    currentCharIndex -= 1
                }
            }
            currentNode = currentEdge?.target
            if(currentNode == null) break
        }
        return if(!exact || currentEdge?.target?.isTerminal == true){
//            println("checking if $url matches ${currentEdge?.target?.exceptions?.serialize()}")
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
        separatorChar: Char = ',',
        nonTerminalChar: Char = '}',
        layerSeparator: String = "|,",
        leafChar: Char = ']',
        exceptionsSeparator: Char = '!',
    ): String = root.sprint(
        depth = 0,
        separatorChar = separatorChar,
        nonTerminalChar = nonTerminalChar,
        layerSeparator = layerSeparator,
        leafChar = leafChar,
        exceptionsSeparator = exceptionsSeparator,
    )

    fun deserialize(
        serializedTree: String,
        separatorChar: Char = ',',
        leafChar: Char = ']',
        nonTerminalChar: Char = '}',
        layerSeparatorChar: Char = '|',
        exceptionsSeparator: Char = '!',
    ){
        val stack = Stack<FilterTreeNode>().apply{ push(root) }
        val edges = serializedTree.split(separatorChar)
        var split: List<String>
        var label: String
        var exceptionTree: FilterTree?
        for(edge in edges){
            split = edge.split(exceptionsSeparator)
            label = split[1]
            exceptionTree = FilterTree().apply{
                if(split[0].isNotEmpty()){
                    deserialize(split[0])
                }
            }
            when(label.last()){
                leafChar -> {
                    stack.peek().addEdge(
                        label = label.substring(0, label.lastIndex),
                        target = FilterTreeNode(isTerminal = true, exceptions = exceptionTree)
                    )
                }
                nonTerminalChar -> {
                    val node = FilterTreeNode(isTerminal = false)
                    stack.apply{
                        peek().addEdge(
                            label = label.substring(0, label.lastIndex),
                            target = node
                        )
                        push(node)
                    }
                }
                layerSeparatorChar -> stack.pop()
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
}
