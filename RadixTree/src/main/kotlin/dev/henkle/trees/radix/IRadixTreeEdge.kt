package dev.henkle.trees.radix

interface IRadixTreeEdge<E: IRadixTreeEdge<E, N>, N: IRadixTreeNode<N, E>>{
    var label: String
    val target: N
}