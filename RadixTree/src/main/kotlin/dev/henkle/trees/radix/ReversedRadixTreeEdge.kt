package dev.henkle.trees.radix

data class ReversedRadixTreeEdge(
    override var label: String,
    override val target: ReversedRadixTreeNode
) : IRadixTreeEdge<ReversedRadixTreeEdge, ReversedRadixTreeNode>