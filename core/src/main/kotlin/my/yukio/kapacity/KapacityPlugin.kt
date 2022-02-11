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
            val nFields = value.primaryConstructorParameters.size
            val bytesInField = 8
            val fileContent = genFileContent(classPackage, className, nFields * bytesInField)
            val file = fileContent.file(
                fileName = "${className}.kt",
                filePath = classPackage.replace('.', File.separatorChar)
            )
            Transform.newSources(file)
        }
        meta(classesExtensionPhase)
    }

private fun genFileContent(classPackage: String, className: String, shallowSize: Int): String =
    """
    |package $classPackage
    |
    |import ${classPackage}.$className
    |
    |val ${className}.shallowSize: Int
    |    get() = $shallowSize
    """.trimMargin()
