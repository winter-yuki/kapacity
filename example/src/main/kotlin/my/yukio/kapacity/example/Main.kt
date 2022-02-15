package my.yukio.kapacity.example

import my.yukio.kapacity.example.shallowSize
import my.yukio.kapacity.example.packag.shallowSize
import my.yukio.kapacity.example.packag.A as AA
import my.yukio.kapacity.example.b.shallowSize

data class A(val x: Int = 1, val y: Long = 2)

data class B(val x: Int = 0) {
    val y: Int
        get() = 42

    val z: Int
        get() = 10

    data class C(var x: Double)
    data class B(var x: Int, val y: Int)
}

fun main() {
    println(A().shallowSize)
    println(B().shallowSize)
    println(AA(42).shallowSize)
    println(B.C(1.0).shallowSize)
    println(B.B(1, 2).shallowSize)
}
