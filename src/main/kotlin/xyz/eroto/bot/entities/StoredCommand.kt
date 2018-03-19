package xyz.eroto.bot.entities

import xyz.eroto.bot.annotations.*

class StoredCommand(val name: String, val klass: Cog, clazz: Class<ICommand>) {
    val cmd = clazz.newInstance() as ICommand
    val subcommands = mutableMapOf<String, StoredCommand>()
    val category = klass::class.java.simpleName
    val description = clazz.getAnnotation(Description::class.java)?.description ?: ""
    val example = clazz.getAnnotation(CommandExample::class.java)?.example ?: ""
    val aliases = clazz.getAnnotation(Alias::class.java)?.aliases ?: arrayOf()
    val arguments = clazz.getAnnotationsByType(Argument::class.java)
            .plus(clazz.getAnnotationsByType(Arguments::class.java).firstOrNull()?.args ?: arrayOf())

    init {

        klass::class.java.classes.filterIsInstance<Class<ICommand>>().forEach { c ->
            c.annotations.filterIsInstance<Subcommand>().filter { it.root == name }.forEach { ann ->
                val name = if (ann.name.isBlank()) c.simpleName.toLowerCase() else ann.name
                val cmd = StoredCommand(name, klass, c)

                subcommands[name] = cmd
            }
        }
    }

    fun run(ctx: Context) = cmd.run(ctx)
}