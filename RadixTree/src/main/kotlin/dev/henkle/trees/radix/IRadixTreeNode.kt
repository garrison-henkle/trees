package dev.henkle.trees.radix

interface IRadixTreeNode<N : IRadixTreeNode<N, E>, E: IRadixTreeEdge<E, N>>{
    fun getEdge(firstChar: Char): E?
    fun addEdge(label: String, target: N)
    fun addEdge(label: String, start: Int = 0, end: Int = label.length): E
    fun removeEdge(label: String, start: Int = 0, end: Int = label.length, parentEdge: E? = null): E?
    fun print(depth: Int = 0, nonTerminalChar: Char = '*', leafChar: Char = ']')
    fun sprint(
        depth: Int = 0,
        separatorChar: Char = ',',
        nonTerminalChar: Char = '}',
        layerSeparator: String = "|,",
        leafChar: Char = ']'
    ): String
}