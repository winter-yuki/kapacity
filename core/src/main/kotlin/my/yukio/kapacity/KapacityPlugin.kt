package my.yukio.kapacity

import arrow.meta.CliPlugin
import arrow.meta.Meta
import arrow.meta.invoke
import arrow.meta.quotes.Transform
import arrow.meta.quotes.classDeclaration
import org.jetbrains.kotlin.psi.KtClass
import java.io.File

val Meta.addShallowSize: CliPlugin
    get() = "Add shallow size to the data classes" {
        val classesExtensionPhase = classDeclaration(this, { element.isData() && element.isTopLevel() }) {
            val classPackage = value.fqName?.parent()?.toString() ?: ""
            val className = name?.identifier ?: error("")
            val bytesInField = 8
            val fileContent = genFileContent(classPackage, className, value.nBackingFields * bytesInField)
            val file = fileContent.file(
                fileName = "${className}.kt",
                filePath = classPackage
                    .split('.')
                    .filter { it.isNotRoot }
                    .joinToString(File.separatorChar.toString())
            )
            Transform.newSources(file)
        }
        meta(classesExtensionPhase)
    }

private val KtClass.nBackingFields: Int
    get() {
        val nCtorBackingFields = primaryConstructorParameters.count { it.hasValOrVar() }
        val nPropsBackingFields = getProperties().count {
            it.isLocal && it.getter != null && it.setter != null
        }
        return nCtorBackingFields + nPropsBackingFields
    }

private fun genFileContent(classPackage: String, className: String, shallowSize: Int): String {
    val pack = if (classPackage.isNotRoot) "package $classPackage" else ""
    val import = if (classPackage.isNotRoot) "import ${classPackage}.$className" else ""
    val def = """
        |val ${className}.shallowSize: Int
        |    get() = $shallowSize
    """.trimMargin()
    return listOf(pack, import, def).joinToString("\n\n")
}

private val String.isRoot: Boolean
    get() = equals("<root>")

private val String.isNotRoot: Boolean
    get() = !isRoot
