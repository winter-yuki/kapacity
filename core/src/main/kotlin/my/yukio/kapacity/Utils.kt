package my.yukio.kapacity

import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtProperty

val String.isRootPackage: Boolean
    get() = equals("<root>")

val String.isNotRootPackage: Boolean
    get() = !isRootPackage

val KtProperty.hasBackingField: Boolean
    get() = hasInitializer()

val KtClass.nBackingFields: Int
    get() {
        val nCtorBackingFields = primaryConstructorParameters.count { it.hasValOrVar() }
        val nPropsBackingFields = getProperties().count { it.hasBackingField }
        return nCtorBackingFields + nPropsBackingFields
    }
