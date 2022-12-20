package dev.henkle.trees.radix

import java.util.*

abstract class IRadixTree<N: IRadixTreeNode<N, E>, E: IRadixTreeEdge<E, N>>{
    internal abstract val root: N
    abstract fun addString(string: String)
    abstract fun removeString(string: String): Boolean
    abstract fun exists(string: String): Boolean
    abstract fun print()

    fun serialize(
        separatorChar: Char = ',',
        nonTerminalChar: Char = '}',
        layerSeparator: String = "|,",
        leafChar: Char = ']'
    ): String = root.sprint(
        depth = 0,
        separatorChar = separatorChar,
        nonTerminalChar = nonTerminalChar,
        layerSeparator = layerSeparator,
        leafChar = leafChar
    )

    protected fun deserialize(
        serializedTree: String,
        separatorChar: Char = ',',
        leafChar: Char = ']',
        nonTerminalChar: Char = '}',
        layerSeparatorChar: Char = '|',
        createNode: (isTerminal: Boolean) -> N
    ){
        val stack = Stack<N>().apply{ push(root) }
        val edges = serializedTree.split(separatorChar)
        for(edge in edges){
            when(edge.last()){
                leafChar -> {
                    stack.peek().addEdge(
                        label = edge.substring(0, edge.lastIndex),
                        target = createNode(true)
                    )
                }
                nonTerminalChar -> {
                    val node = createNode(false)
                    stack.apply{
                        peek().addEdge(
                            label = edge.substring(0, edge.lastIndex),
                            target = node
                        )
                        push(node)
                    }
                }
                layerSeparatorChar -> stack.pop()
                else -> {
                    val node = createNode(true)
                    stack.apply{
                        peek().addEdge(
                            label = edge,
                            target = node
                        )
                        push(node)
                    }
                }
            }
        }
    }
}
