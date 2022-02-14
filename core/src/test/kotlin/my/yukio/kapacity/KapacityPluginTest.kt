package my.yukio.kapacity

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.jvm.internal.DefaultConstructorMarker

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
        classname: String, nCtorArgs: Int
    ): Int {
        val klassKt = classLoader.loadClass(classname + "Kt")
        val method = klassKt.methods.find { it.name == "getShallowSize" }!!
        val klass = classLoader.loadClass(classname)
        val ctor = klass.getDeclaredConstructor(
            *List(nCtorArgs + 1) { Int::class.java }.toTypedArray(),
            DefaultConstructorMarker::class.java
        )
        val instance = ctor.newInstance(*List(nCtorArgs + 1) { 42 }.toTypedArray(), null)
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
                data class Klass(val x: Int = 42)
            """.trimIndent()
        )
        compile(source).run {
            1 fields shallowSize("Klass", 1)
        }
    }

    @Test
    fun `basic test 2`() {
        val source = SourceFile.kotlin(
            "File.kt", """
                data class Klass(val x: Int = 42)
            """.trimIndent()
        )
        compile(source).run {
            1 fields shallowSize("Klass", 1)
        }
    }

    @Test
    fun `two data classes`() {
        val source = SourceFile.kotlin(
            "File.kt", """
                |data class A(val x: Int = 1, val y: Int = 2)
                |data class B(val x: Int = 1)
            """.trimMargin()
        )
        compile(source).run {
            2 fields shallowSize("A", 2)
            1 fields shallowSize("B", 1)
        }
    }
}
