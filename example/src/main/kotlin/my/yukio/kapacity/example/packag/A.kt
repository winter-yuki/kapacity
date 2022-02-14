package my.yukio.kapacity.example.packag

data class A(var x: Int) {
    val a: Int
        get() = 42

    var b: Int
        get() = x
        set(value) {
            x = value
        }
}
