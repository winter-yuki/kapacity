package my.yukio.kapacity

import arrow.meta.CliPlugin
import arrow.meta.Meta
import arrow.meta.invoke
import arrow.meta.quotes.Transform
import arrow.meta.quotes.classDeclaration
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtProperty
import java.io.File

val Meta.addShallowSize: CliPlugin
    get() = "Add shallow size to the data classes" {
        val classesExtensionPhase = classDeclaration(this, { element.isData() }) {
            val segments = value.fqName?.pathSegments().orEmpty().map { it.identifier }
            val prefixNames = segments.dropLast(1)
            val className = segments.last()
            val bytesInField = 8
            val fileContent = genFileContent(
                prefixNames, className,
                value.nBackingFields * bytesInField
            )
            val file = fileContent.file(
                fileName = "${className}.kt",
                filePath = "generated${File.separatorChar}"
                        + prefixNames.joinToString(File.separatorChar.toString())
            )
            Transform.newSources(file)
        }
        meta(classesExtensionPhase)
    }

private fun genFileContent(prefixNames: List<String>, className: String, shallowSize: Int): String {
    val pack =
        if (prefixNames.isEmpty()) null
        else "package ${prefixNames.joinToString(".") { it.lowercase() }}"
    val import =
        if (prefixNames.isEmpty()) null
        else "import ${prefixNames.joinToString(".")}.$className"
    val def = """
        |val ${className}.shallowSize: Int
        |    get() = $shallowSize
    """.trimMargin()
    return listOfNotNull(pack, import, def).joinToString("\n\n")
}

private val KtProperty.hasBackingField: Boolean
    get() = hasInitializer()

private val KtClass.nBackingFields: Int
    get() {
        val nCtorBackingFields = primaryConstructorParameters.count { it.hasValOrVar() }
        val nPropsBackingFields = getProperties().count { it.hasBackingField }
        return nCtorBackingFields + nPropsBackingFields
    }
