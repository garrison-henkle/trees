package dev.henkle.trees.radix

import dev.henkle.trees.utils.getOrCreate
import kotlin.math.min

/**
 * A node within a [RadixTree]. Contains logic to add and remove edges to other nodes.
 *
 * @property isTerminal Indicates whether the node is a terminal node i.e. a string that was inserted into the tree
 * that terminates at this node. The path from root to a terminal node will always form a string that has been
 * inserted into the tree, while the opposite is always true for a non-terminal node.
 */
class RadixTreeNode(var isTerminal: Boolean = true): IRadixTreeNode<RadixTreeNode, RadixTreeEdge>{
    /**
     * A map of all edges that originate at this node by their first character.
     */
    private var edges: HashMap<Char, RadixTreeEdge>? = null

    /**
     * The number of child edges that originate at this node.
     */
    private val edgeCount: Int get() = edges?.count() ?: 0

    /**
     * Retrieves the edge with the first character [firstChar], if one exists.
     */
    override fun getEdge(firstChar: Char): RadixTreeEdge? = edges?.get(firstChar)

    /**
     * Adds an edge with an explicit label and target node. This can only be used if the inserted edge is
     * known to be valid ahead of time.
     */
    override fun addEdge(label: String, target: RadixTreeNode){
        edges.getOrCreate{ edges = it }[label[0]] = RadixTreeEdge(
            label = label,
            target = target
        )
    }

    /**
     * Adds an edge to this node or one of its descendants, or returns an existing edge if one already exists.
     *
     * Unlike the alternate [addEdge], this method can be used when the proper location of the label is not known.
     *
     * @param label The "search query". It is the entire string that is being inserted into the tree.
     * @param start The index within [label] that determines where the current segment starts.
     * @param end The index within [label] that determines where the current segment ends, exclusive.
     */
    override fun addEdge(
        label: String,
        start: Int,
        end: Int
    ): RadixTreeEdge{
        val edgeMap = edges.getOrCreate{ edges = it }
        val firstChar = label[start]
        val existingEdge = edgeMap[firstChar]
        return if(existingEdge == null){
            RadixTreeEdge(
                label = label.substring(start, end),
                target = RadixTreeNode(isTerminal = true)
            ).also{ edgeMap[firstChar] = it }
        } else{
            var splitIndex = -1
            val searchLabelLength = end - start
            for(i in 1 until min(searchLabelLength, existingEdge.label.length)){
                if(existingEdge.label[i] != label[start + i]){
                    splitIndex = i
                    break
                }
            }
            if(splitIndex == -1){
                when{
                    searchLabelLength == existingEdge.label.length -> existingEdge.apply{ target.isTerminal = true }
                    searchLabelLength > existingEdge.label.length ->
                        existingEdge.target.addEdge(label, start + existingEdge.label.length, end)
                    else -> {
                        val intermediateNode = RadixTreeNode(isTerminal = true)
                        val rootEdge = RadixTreeEdge(
                            label = existingEdge.label.substring(0, searchLabelLength),
                            target = intermediateNode
                        )
                        intermediateNode.addEdge(
                            label = existingEdge.label.substring(searchLabelLength),
                            target = existingEdge.target
                        )
                        edgeMap[firstChar] = rootEdge
                        rootEdge
                    }
                }
            } else{
                val intermediateNode = RadixTreeNode(isTerminal = false)
                val rootEdge = RadixTreeEdge(
                    label = existingEdge.label.substring(0, splitIndex),
                    target = intermediateNode
                )
                intermediateNode.addEdge(
                    label = existingEdge.label.substring(splitIndex),
                    target = existingEdge.target
                )
                intermediateNode.addEdge(
                    label = label.substring(start + splitIndex, end),
                    target = RadixTreeNode(isTerminal = true)
                )
                edgeMap[firstChar] = rootEdge
                rootEdge
            }
        }
    }

    /**
     * Removes an edge from this node or one of its descendants if a match is found.
     *
     * @param parentEdge The edge that terminates at the current node.
     * @param label The "search query". It is the entire string that is being removed from the tree.
     * @param start The index within [label] that determines where the current segment starts.
     * @param end The index within [label] that determines where the current segment ends, exclusive.
     *
     * @return the removed edge or null if no edge was removed.
     */
    override fun removeEdge(
        label: String,
        start: Int,
        end: Int,
        parentEdge: RadixTreeEdge?
    ): RadixTreeEdge? = edges?.run{
        get(label[start])?.let{ edge ->
            val searchLabelLength = end - start
            if(searchLabelLength < label.length) return@let null
            for(i in 1 until label.length){
                if(label[start + i] != edge.label[i]) return@let null
            }
            if(searchLabelLength == label.length){
                if(edge.target.isTerminal){
                    if(edge.target.edgeCount > 1){
                        edge.apply{ target.isTerminal = false }
                    } else{
                        remove(label[start])?.also{
                            parentEdge?.apply{ this.label += edge.target.edges!!.toList().first().second }
                            if(isEmpty()) edges = null
                        }
                    }
                } else null
            } else {
                edge.target.removeEdge(
                    parentEdge = edge,
                    label = label,
                    start = label.length,
                    end = end
                )
            }
        }
    }

    /**
     * Prints the current layer and all layers below it to stdout.
     *
     * @param depth The current depth within the tree, where 0 is the root.
     * @param nonTerminalChar The character to suffix on node labels to indicate non-terminal nodes.
     * @param leafChar The character
     */
    override fun print(
        depth: Int,
        nonTerminalChar: Char,
        leafChar: Char
    ){
        val prefix = "   ".repeat(depth)
        edges?.forEach{ (_, edge) ->
            val label = if(edge.target.isTerminal) edge.label else "${edge.label}$nonTerminalChar"
            val leafIndicator = if(edge.target.edges.isNullOrEmpty()) leafChar else ""
            println("$prefix- $label$leafIndicator")
            edge.target.print(depth + 1)
        }
    }

    /**
     * Prints the current layer and all layers below it to a string.
     *
     * @param depth The current depth within the tree, where 0 is the root.
     * @param separatorChar The character to separate the nodes.
     * @param leafChar The character to suffix on node labels to indicate leaf nodes.
     */
    override fun sprint(
        depth: Int,
        separatorChar: Char,
        nonTerminalChar: Char,
        layerSeparator: String,
        leafChar: Char,
    ): String = edges?.map{ (_, edge) ->
        val leafIndicator = if(edge.target.edges.isNullOrEmpty()) leafChar else ""
        val formattedLabel = if(edge.target.isTerminal) edge.label else "${edge.label}$nonTerminalChar"
        "$formattedLabel$leafIndicator$separatorChar" + edge.target.sprint(depth + 1)
    }?.joinToString(separator = "", postfix = layerSeparator)
        ?.run{ if(depth == 0) substring(0, lastIndex) else this }
        ?: ""
}
