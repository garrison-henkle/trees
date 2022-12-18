package dev.henkle.trees.utils

inline fun <K, V> HashMap<K, V>?.getOrCreate(
    assign: (HashMap<K, V>) -> Unit
): HashMap<K, V> = this ?: run{
    HashMap<K, V>().also{ assign(it) }
}