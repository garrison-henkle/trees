package dev.henkle.trees.radix

interface IRadixTree{
    fun addString(string: String)
    fun removeString(string: String): Boolean
    fun exists(string: String): Boolean
    fun sprint(): String
    fun print()
}
