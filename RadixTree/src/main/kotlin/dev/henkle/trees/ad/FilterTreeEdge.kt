package dev.henkle.trees.ad

data class FilterTreeEdge(
    var label: String,
    val target: FilterTreeNode
)