package dev.henkle.trees.radix

import kotlin.math.min

class RadixTree {
    private val root = RadixTreeNode(isTerminal = false)

    fun addString(string: String){
        root.addEdge(string)
    }

    fun removeString(string: String): Boolean =
        root.removeEdge(label = string) != null

    private fun exists(string: String, exact: Boolean): Boolean{
        var currentCharIndex = 0
        var currentNode: RadixTreeNode? = root
        var currentEdge: RadixTreeEdge? = null
        var currentLabel: String?
        while(currentCharIndex < string.length){
            currentEdge = currentNode?.getEdge(string[currentCharIndex++])
            if(currentCharIndex >= string.length) break
            currentLabel = currentEdge?.label
            if(currentLabel != null){
                for(i in 1 until min(currentLabel.length, string.length)){
                    if(currentLabel[i] != string[currentCharIndex]) return false
                    currentCharIndex += 1
                }
            }
            currentNode = currentEdge?.target
        }
        return !exact || currentEdge?.target?.isTerminal == true
    }

    fun exists(string: String): Boolean = exists(string = string, exact = true)
    fun prefixExists(string: String): Boolean = exists(string = string, exact = false)

    fun print() = root.print()
    fun sprint(): String = root.sprint()
}