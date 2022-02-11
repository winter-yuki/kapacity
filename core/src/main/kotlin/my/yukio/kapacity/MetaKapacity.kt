package my.yukio.kapacity

import arrow.meta.CliPlugin
import arrow.meta.Meta
import arrow.meta.phases.CompilerContext

class MetaKapacity : Meta {
    override fun intercept(ctx: CompilerContext): List<CliPlugin> =
        listOf(addShallowSize)
}
