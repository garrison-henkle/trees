package dev.henkle.trees.radix

import kotlin.math.min

class RadixTree : IRadixTree<RadixTreeNode, RadixTreeEdge>(){
    override val root = RadixTreeNode(isTerminal = false)

    override fun addString(string: String){
        root.addEdge(string)
    }

    override fun removeString(string: String): Boolean =
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

    override fun exists(string: String): Boolean = exists(string = string, exact = true)
    fun prefixExists(string: String): Boolean = exists(string = string, exact = false)

    override fun print() = root.print()

    fun deserialize(
        serializedTree: String,
        separatorChar: Char = ',',
        leafChar: Char = ']',
        nonTerminalChar: Char = '}',
        layerSeparatorChar: Char = '|',
    ) = deserialize(
        serializedTree,
        separatorChar,
        leafChar,
        nonTerminalChar,
        layerSeparatorChar
    ){ terminal -> RadixTreeNode(isTerminal = terminal) }
}