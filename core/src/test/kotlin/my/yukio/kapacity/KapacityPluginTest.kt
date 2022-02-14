package my.yukio.kapacity

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class KapacityPluginTest {
    private fun compile(vararg sources: SourceFile): KotlinCompilation.Result =
        KotlinCompilation().apply {
            this.sources = sources.toList()
            compilerPlugins = listOf(MetaKapacity())
            inheritClassPath = true
            messageOutputStream = System.out
        }.compile().also { result ->
            assertEquals(ExitCode.OK, result.exitCode, result.messages)
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
        val fieldSize = 8
        assertEquals(this * fieldSize, size)
    }

    @Test
    fun `basic test`() {
        val source = SourceFile.kotlin(
            "Klass.kt", """
                data class Klass(val x: Int)
            """.trimIndent()
        )
        compile(source).run {
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
        compile(source).run {
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
        compile(source).run {
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
        compile(source1, source2).run {
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
        compile(source1, source2).run {
            2 fields shallowSize("A", 2)
            1 fields shallowSize("B", 1)
            2 fields shallowSize("A", 2, classPackage = "other")
        }
    }
}
