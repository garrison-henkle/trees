package dev.henkle.trees.radix

import kotlin.math.min

class ReversedRadixTree : IRadixTree<ReversedRadixTreeNode, ReversedRadixTreeEdge>(){
    override val root = ReversedRadixTreeNode(isTerminal = false)

    override fun addString(string: String){
        root.addEdge(string)
    }

    override fun removeString(string: String): Boolean =
        root.removeEdge(label = string) != null

    private fun exists(string: String, exact: Boolean): Boolean{
        var currentCharIndex = string.lastIndex
        var currentNode: ReversedRadixTreeNode? = root
        var currentEdge: ReversedRadixTreeEdge? = null
        var currentLabel: String?
        while(currentCharIndex >= 0){
            currentEdge = currentNode?.getEdge(string[currentCharIndex--])
            currentLabel = currentEdge?.label
            if(currentLabel != null){
                for(i in 1 until min(currentLabel.length, string.length)){
                    if(currentCharIndex < 0) break
                    if(currentLabel[i] != string[currentCharIndex]) return false
                    currentCharIndex -= 1
                }
            }
            currentNode = currentEdge?.target
            if(currentNode == null) break
        }
        return !exact || currentEdge?.target?.isTerminal == true
    }

    override fun exists(string: String): Boolean = exists(string = string, exact = true)
    fun suffixExists(string: String): Boolean = exists(string = string, exact = false)

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
    ){ terminal -> ReversedRadixTreeNode(isTerminal = terminal) }
}