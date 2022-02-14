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
        filename: String, classname: String, vararg ctorParams: Pair<Class<*>, *>
    ): Int {
        val klassKt = classLoader.loadClass(filename)
        val method = klassKt.methods.find { it.name == "getShallowSize" }!!
        val klass = classLoader.loadClass(classname)
        val ctor = klass.getConstructor(
            *ctorParams.map { it.first }.toTypedArray(), Int::class.java,
            DefaultConstructorMarker::class.java
        )
        val instance = ctor.newInstance(*ctorParams.map { it.second }.toTypedArray(), 42, null)
        return method(null, instance) as Int
    }

    private infix fun Int.has(n: Int) {
        val fieldSize = 8
        assertEquals(n * fieldSize, this)
    }

    @Test
    fun `basic test`() {
        val source = SourceFile.kotlin(
            "Klass.kt", """
                data class Klass(val x: Int = 42)
            """.trimIndent()
        )
        compile(source).run {
            shallowSize(
                "KlassKt", "Klass",
                Int::class.java to 1
            ) has 1
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
            shallowSize(
                "KlassKt", "Klass",
                Int::class.java to 1
            ) has 1
        }
    }

    @Test
    fun `two data classes`() {
        val source = SourceFile.kotlin(
            "File.kt", """
                |data class A(val x: Int)
//                |data class B(val x: List<Int>)
            """.trimMargin()
        )
        compile(source).run {
            shallowSize(
                "AKt", "A",
                Int::class.java to 1, // Double::class.java to 2.0
            ) has 2
//            shallowSize(
//                "FileKt", "B",
//                List::class.java to listOf(1)
//            ) has 1
        }
    }
}
