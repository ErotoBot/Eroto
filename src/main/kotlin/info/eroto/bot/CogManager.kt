package info.eroto.bot

import info.eroto.bot.annotations.Alias
import info.eroto.bot.annotations.Command
import info.eroto.bot.entities.Cog
import info.eroto.bot.entities.ICommand
import info.eroto.bot.entities.StoredCommand
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder

class CogManager {
    init {
        Reflections(ConfigurationBuilder().setUrls(ClasspathHelper.forPackage("info.eroto.bot.cogs")))
                .getSubTypesOf(Cog::class.java)
                .forEach {
                    if (!it.isInterface) {
                        val klass = it.newInstance() as Cog

                        cogs[klass::class.simpleName!!] = klass

                        klass::class.java.classes.filterIsInstance<Class<ICommand>>().forEach { clazz ->
                            clazz.annotations.filterIsInstance<Command>().forEach { ann ->
                                val name = if (ann.name.isNotBlank()) ann.name else clazz.simpleName.toLowerCase()

                                commands[name] = StoredCommand(name, klass, clazz)

                                for (alias in clazz.getAnnotation(Alias::class.java)?.aliases ?: arrayOf()) {
                                    aliases[alias] = name
                                }
                            }
                        }
                    }
                }
    }

    companion object {
        val cogs = mutableMapOf<String, Cog>()
        val aliases = mutableMapOf<String, String>()
        val commands = mutableMapOf<String, StoredCommand>()

        fun help(): List<String> {
            val lines = mutableListOf<String>()

            for (cog in cogs) {
                val cmds = commands.values.filter { it.klass.hashCode() == cog.value.hashCode() }

                lines += "${cog.key}:"

               for (cmd in cmds) {
                   val spaces = " ".repeat(25 - cmd.name.length)

                   lines += "\t${cmd.name}$spaces${cmd.description}"
               }
            }

            val partSize = 50
            val parts = mutableListOf<String>()
            var curPart = ""
            for (line in lines) {
                if (curPart.split("\n").size >= partSize) {
                    parts += curPart
                    curPart = ""
                }

                curPart += "$line\n"
            }

            if (curPart.isNotBlank() && curPart.split("\n").size < partSize) {
                parts += curPart
            }

            return parts
        }
    }
}