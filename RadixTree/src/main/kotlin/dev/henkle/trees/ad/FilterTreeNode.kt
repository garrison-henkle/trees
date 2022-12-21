package dev.henkle.trees.ad

import dev.henkle.trees.utils.getOrCreate
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.min

class FilterTreeNode(
    var isTerminal: Boolean = true,
    val exceptions: FilterTree? = null
) {
    companion object{ private val idCounter = AtomicInteger() }
    val id = idCounter.getAndIncrement()

    /**
     * A map of all edges that originate at this node by their first character.
     */
    private var edges: HashMap<Char, FilterTreeEdge>? = null

    /**
     * The number of child edges that originate at this node.
     */
    private val edgeCount: Int get() = edges?.count() ?: 0

    /**
     * Retrieves the edge with the first character [firstChar], if one exists.
     */
    fun getEdge(firstChar: Char): FilterTreeEdge? = edges?.get(firstChar)

    /**
     * Adds an edge with an explicit label and target node. This can only be used if the inserted edge is
     * known to be valid ahead of time.
     */
    fun addEdge(label: String, target: FilterTreeNode){
        edges.getOrCreate{ edges = it }[label[0]] = FilterTreeEdge(
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
    fun addEdge(
        label: String,
        exceptions: FilterTree?,
        start: Int = 0,
        end: Int = label.length
    ): FilterTreeEdge {
        val edgeMap = edges.getOrCreate{ edges = it }
        val firstChar = label[end - 1]
        val existingEdge = edgeMap[firstChar]
        return if(existingEdge == null){
            FilterTreeEdge(
                label = label.substring(start, end).reversed(),
                target = FilterTreeNode(isTerminal = true, exceptions = exceptions)
            ).also{ edgeMap[firstChar] = it }
        } else{
            var splitIndex = -1
            val searchLabelLength = end - start
            for(i in 1 until min(searchLabelLength, existingEdge.label.length)){
                if(existingEdge.label[i] != label[end - i - 1]){ //todo -1 might be an issue
                    splitIndex = i
                    break
                }
            }
            if(splitIndex == -1){
                when{
                    searchLabelLength == existingEdge.label.length -> existingEdge.apply{ target.isTerminal = true }
                    searchLabelLength > existingEdge.label.length ->
                        existingEdge.target.addEdge(label, exceptions, start, end - existingEdge.label.length)
                    else -> {
                        val intermediateNode = FilterTreeNode(isTerminal = true, exceptions = exceptions)
                        val rootEdge = FilterTreeEdge(
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
                val intermediateNode = FilterTreeNode(isTerminal = false)
                val rootEdge = FilterTreeEdge(
                    label = existingEdge.label.substring(0, splitIndex),
                    target = intermediateNode
                )
                intermediateNode.addEdge(
                    label = existingEdge.label.substring(splitIndex),
                    target = existingEdge.target
                )
                intermediateNode.addEdge(
                    label = label.substring(start, end - splitIndex).reversed(),
                    target = FilterTreeNode(isTerminal = true, exceptions = exceptions)
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
    fun removeEdge(
        label: String,
        start: Int = 0,
        end: Int = label.length,
        parentEdge: FilterTreeEdge? = null
    ): FilterTreeEdge? = edges?.run{
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
                    start = start + label.length,
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
    fun print(
        depth: Int = 0,
        nonTerminalChar: Char = '*',
        leafChar: Char = ']'
    ){
        val prefix = "   ".repeat(depth)
        edges?.forEach{ (_, edge) ->
            val label = if(edge.target.isTerminal) edge.label else "${edge.label}$nonTerminalChar"
            val leafIndicator = if(edge.target.edges.isNullOrEmpty()) leafChar else ""
            println("$prefix- $label$leafIndicator <${edge.target.exceptions?.serialize() ?: ""}>")
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
    fun sprint(
        depth: Int = 0,
        chars: FilterTree.SerializationCharacters = FilterTree.SerializationCharacters()
    ): String = edges?.map{ (_, edge) ->
        val leafIndicator = if(edge.target.edges.isNullOrEmpty()) chars.leaf else ""
        val formattedLabel = if(edge.target.isTerminal) edge.label else "${edge.label}${chars.nonTerminal}"
        val exceptions = if(edge.target.exceptions != null){
            edge.target.exceptions.serialize(chars = FilterTree.SerializationCharacters.exceptions)
        } else ""
        "$exceptions${chars.divider}$formattedLabel$leafIndicator${chars.separator}${edge.target.sprint(depth + 1, chars = chars)}"
    }?.joinToString(separator = "", postfix = chars.separator.toString())
        ?.run{ if(depth == 0) substring(0, lastIndex) else this }
        ?: ""

    override fun toString(): String = id.toString()
}
