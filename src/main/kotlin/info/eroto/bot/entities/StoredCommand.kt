package info.eroto.bot.entities

import info.eroto.bot.annotations.CommandExample
import info.eroto.bot.annotations.Description
import info.eroto.bot.annotations.Subcommand
import kotlin.reflect.full.findAnnotation

class StoredCommand(private val name: String, private val klass: Cog, clazz: Class<ICommand>) {
    val cmd = clazz.newInstance() as ICommand
    val subcommands = mutableMapOf<String, StoredCommand>()
    val category = klass::class.java.name
    val description = klass::class.findAnnotation<Description>()?.description ?: ""
    val example = klass::class.findAnnotation<CommandExample>()?.example ?: ""

    init {
        klass::class.java.classes.filterIsInstance<Class<ICommand>>().forEach { c ->
            c.annotations.filterIsInstance<Subcommand>().filter { it.root == name }.forEach { ann ->
                val name = if (ann.name.isBlank()) c.simpleName else ann.name
                val cmd = StoredCommand(name, klass, c)

                subcommands[name] = cmd
            }
        }
    }

    fun run(ctx: Context) = cmd.run(ctx)
}