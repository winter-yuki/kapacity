package my.yukio.kapacity.example

import my.yukio.kapacity.example.shallowSize
import my.yukio.kapacity.example.packag.shallowSize
import my.yukio.kapacity.example.packag.A as AA

data class A(val x: Int = 1, val y: Long = 2)

data class B(val x: Int = 0) {
    val y: Int
        get() = 42

    val z: Int
        get() = 10
}

fun main() {
    println(A().shallowSize)
    println(B().shallowSize)
    println(AA(42).shallowSize)
}
