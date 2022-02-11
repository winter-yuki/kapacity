package my.yukio.kapacity.example

import my.yukio.kapacity.example.shallowSize

data class Tmp(val x: Int = 0) {
    val y: Int
        get() = 42

    val z: Int
        get() = 10
}

data class Q(val x: Int = 1, val y: Long = 2)

fun main() {
    println(Tmp().shallowSize)
    println(Q().shallowSize)
}
