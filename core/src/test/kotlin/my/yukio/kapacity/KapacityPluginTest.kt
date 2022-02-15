package my.yukio.kapacity

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KapacityPluginTest {
    private val bytesInField = 8

    private fun compilation(vararg sources: SourceFile): KotlinCompilation =
        KotlinCompilation().apply {
            this.sources = sources.toList()
            compilerPlugins = listOf(MetaKapacity())
            inheritClassPath = true
            messageOutputStream = System.out
        }

    private fun compileOk(vararg sources: SourceFile): KotlinCompilation.Result =
        compilation(*sources).compile().apply {
            assertEquals(ExitCode.OK, exitCode, messages)
        }

    private fun compileError(vararg sources: SourceFile): KotlinCompilation.Result =
        compilation(*sources).compile().apply {
            assertEquals(ExitCode.COMPILATION_ERROR, exitCode, messages)
        }

    private fun KotlinCompilation.Result.shallowSize(
        className: String, nCtorArgs: Int, classPackage: String = ""
    ): Int {
        val klassKtName = if (classPackage.isEmpty()) "${className}Kt" else "${classPackage}.${className}Kt"
        val klassKt = classLoader.loadClass(klassKtName)
        val method = klassKt.methods.find { it.name == "getShallowSize" }!!
        val klassName = if (classPackage.isEmpty()) className else "${classPackage}.${className}"
        val klass = classLoader.loadClass(klassName)
        val ctor = klass.getDeclaredConstructor(
            *List(nCtorArgs) { Int::class.java }.toTypedArray()
        )
        val instance = ctor.newInstance(*List(nCtorArgs) { 42 }.toTypedArray())
        return method(null, instance) as Int
    }

    private infix fun Int.fields(size: Int) {
        assertEquals(this * bytesInField, size)
    }

    /**
     * Not infix because IDEA highlighting did not work for the infix syntax at the moment of writing.
     */
    private fun Int.fields(@Language("kotlin") code: String) {
        val source = SourceFile.kotlin("File.kt", code)
        compileOk(source).run {
            this@fields fields shallowSize("A", 1)
        }
    }

    @Test
    fun `basic test`() {
        val source = SourceFile.kotlin(
            "Klass.kt", """
                data class Klass(val x: Int)
            """.trimIndent()
        )
        compileOk(source).run {
            1 fields shallowSize("Klass", 1)
        }
    }

    @Test
    fun `many ctor args`() {
        val source = SourceFile.kotlin(
            "File.kt", """
                data class Klass(val x: Int, val y: Int, val z: Int)
            """.trimIndent()
        )
        compileOk(source).run {
            3 fields shallowSize("Klass", 3)
        }
    }

    @Test
    fun `two data classes`() {
        val source = SourceFile.kotlin(
            "File.kt", """
                |data class A(val x: Int, val y: Int)
                |data class B(val x: Int)
            """.trimMargin()
        )
        compileOk(source).run {
            2 fields shallowSize("A", 2)
            1 fields shallowSize("B", 1)
        }
    }

    @Test
    fun `many sources`() {
        val source1 = SourceFile.kotlin(
            "File1.kt", """
                |data class A(val x: Int, val y: Int)
                |data class B(val x: Int)
            """.trimMargin()
        )
        val source2 = SourceFile.kotlin(
            "File2.kt", """
                |data class C(val x: Int, val y: Int, val z: Int)
            """.trimMargin()
        )
        compileOk(source1, source2).run {
            2 fields shallowSize("A", 2)
            1 fields shallowSize("B", 1)
            3 fields shallowSize("C", 3)
        }
    }

    @Test
    fun `many packages`() {
        val source1 = SourceFile.kotlin(
            "File1.kt", """
                |data class A(val x: Int, val y: Int)
                |data class B(val x: Int)
            """.trimMargin()
        )
        val source2 = SourceFile.kotlin(
            "File2.kt", """
                |package other
                |data class A(val x: Int, val y: Int)
            """.trimMargin()
        )
        compileOk(source1, source2).run {
            2 fields shallowSize("A", 2)
            1 fields shallowSize("B", 1)
            2 fields shallowSize("A", 2, classPackage = "other")
        }
    }

    @Test
    fun `val with backing`() {
        2.fields(
            """
            |data class A(val x: Int) {
            |    val answer: Int
            |        get() = 42
            |    val assigned: Int = 12        
            |}
            """.trimMargin()
        )
    }

    @Suppress(
        "PropertyName", "SetterBackingFieldAssignment",
        "MemberVisibilityCanBePrivate", "SuspiciousVarProperty"
    )
    @Test
    fun `default field accessor`() {
        4.fields(
            """
            |data class A(val x: Int) {
            |    var _a = 1
            |    var a: Int = 0
            |        set(value) { _a = 1 }
            |        
            |    var b: Int = 42
            |        get() = _a   
            |}
            """.trimMargin()
        )
    }

    @Test
    fun `no backing fields`() {
        1.fields(
            """
            |data class A(var x: Int) {
            |    val a: Int
            |        get() = x
            |    var b: Int
            |        get() = x
            |        set(value) { x = value }            
            |}
            """.trimMargin()
        )
    }

    @Test
    fun `non-data classes`() {
        val source = SourceFile.kotlin(
            "File.kt", """
                |class C
                |
                |fun test() {
                |    println(C().shallowSize)
                |}
            """.trimMargin()
        )
        compileError(source)
    }

    @Test
    fun `data nested in non-data`() {
        val source1 = SourceFile.kotlin(
            "File.kt", """
                |import c.shallowSize
                |
                |class C {
                |    data class D(val x: Int, var y: Int)
                |}
                |
                |fun test() {
                |    C.D(1, 2).shallowSize
                |}    
            """.trimMargin()
        )
        compileOk(source1)
        val source2 = SourceFile.kotlin(
            "File.kt", """
                |class C {
                |    data class D(val x: Int, var y: Int)
                |}
                |
                |fun test() {
                |    C().shallowSize
                |}    
            """.trimMargin()
        )
        compileError(source2)
    }

    @Test
    fun `data nested in data`() {
        val source = SourceFile.kotlin(
            "File.kt", """
                |import c.shallowSize
                |
                |data class C(var x: Int = 0) {
                |    data class D(val x: Int, var y: Int)
                |}
                |
                |fun test() {
                |    C().shallowSize
                |    C.D(1, 2).shallowSize
                |}    
            """.trimMargin()
        )
        compileOk(source)
    }

    @Test
    fun `non-data nested in data`() {
        val source = SourceFile.kotlin(
            "File.kt", """
                |import c.shallowSize
                |
                |data class C(var x: Int) {
                |    class D(val x: Int, var y: Int)
                |}
                |
                |fun test() {
                |    C().shallowSize
                |    C.D(1, 2).shallowSize
                |}    
            """.trimMargin()
        )
        compileError(source)
    }

    @Test
    fun `nested data same names`() {
        val source = SourceFile.kotlin(
            "File.kt", """
                |import d.shallowSize
                |
                |data class D(var x: Int) {
                |    data class D(val x: Int, var y: Int)
                |}
                |
                |fun test() {
                |    D(1).shallowSize
                |    D.D(1, 2).shallowSize
                |}    
            """.trimMargin()
        )
        compileOk(source)
    }
}
