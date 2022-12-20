package dev.henkle.trees.radix

data class RadixTreeEdge(
    override var label: String,
    override val target: RadixTreeNode
) : IRadixTreeEdge<RadixTreeEdge, RadixTreeNode>