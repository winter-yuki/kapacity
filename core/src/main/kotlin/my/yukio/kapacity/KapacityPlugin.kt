package my.yukio.kapacity

import arrow.meta.CliPlugin
import arrow.meta.Meta
import arrow.meta.invoke
import arrow.meta.quotes.Transform
import arrow.meta.quotes.classDeclaration
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
                    .filter { it.isNotRootPackage }
                    .joinToString(File.separatorChar.toString())
            )
            Transform.newSources(file)
        }
        meta(classesExtensionPhase)
    }

private fun genFileContent(classPackage: String, className: String, shallowSize: Int): String {
    val pack = if (classPackage.isNotRootPackage) "package $classPackage" else ""
    val import = if (classPackage.isNotRootPackage) "import ${classPackage}.$className" else ""
    val def = """
        |val ${className}.shallowSize: Int
        |    get() = $shallowSize
    """.trimMargin()
    return listOf(pack, import, def).joinToString("\n\n")
}
