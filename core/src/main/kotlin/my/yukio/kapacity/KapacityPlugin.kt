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
            val classPackage = value.classPackage
            val outerClasses = value.outerClasses
            val className = name?.identifier ?: error("Class name does not exist")
            val bytesInField = 8
            val fileContent = genFileContent(
                classPackage, outerClasses, className,
                value.nBackingFields * bytesInField
            )
            val file = fileContent.file(
                fileName = "${className}.kt",
                filePath = "generated${File.separatorChar}" + classPackage.plus(outerClasses)
                    .joinToString(File.separatorChar.toString())
            )
            Transform.newSources(file)
        }
        meta(classesExtensionPhase)
    }

private fun genFileContent(
    classPackage: List<String>, outerClasses: List<String>,
    className: String, shallowSize: Int
): String {
    val pack = if (classPackage.plus(outerClasses).isEmpty()) null else "package ${classPackage.plus(outerClasses.map { it.lowercase() }).joinToString(".")}"
    println("!!!!!!!!! = ${classPackage.plus(outerClasses.map { it.lowercase() })}")
    val import =
        if (outerClasses.isEmpty()) null
        else "import ${classPackage.plus(outerClasses).joinToString(".")}.$className"
    val def = """
        |val ${className}.shallowSize: Int
        |    get() = $shallowSize
    """.trimMargin()
    return listOfNotNull(pack, import, def).joinToString("\n\n")
}

val KtProperty.hasBackingField: Boolean
    get() = hasInitializer()

val KtClass.nBackingFields: Int
    get() {
        val nCtorBackingFields = primaryConstructorParameters.count { it.hasValOrVar() }
        val nPropsBackingFields = getProperties().count { it.hasBackingField }
        return nCtorBackingFields + nPropsBackingFields
    }

val KtClass.classPackage: List<String>
    get() = fqName?.pathSegments().orEmpty()
        .map { it.identifier }
        .takeWhile { it.first().isLowerCase() }

val KtClass.outerClasses: List<String>
    get() = fqName?.pathSegments().orEmpty()
        .map { it.identifier }
        .filter { it.first().isUpperCase() }
        .dropLast(1)

