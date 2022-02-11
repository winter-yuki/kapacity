package my.yukio.kapacity

import arrow.meta.CliPlugin
import arrow.meta.Meta
import arrow.meta.invoke
import arrow.meta.quotes.Transform
import arrow.meta.quotes.classDeclaration

val Meta.addShallowSize: CliPlugin
    get() = "Add shallow size to the data classes" {
        meta(
            classDeclaration(this, { element.name == "AddShallowSize" }) {
                Transform.newSources()
            }
        )
    }
