package info.eroto.bot

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

                        klass::class.java.classes.filterIsInstance<Class<ICommand>>().forEach { clazz ->
                            clazz.annotations.filterIsInstance<Command>().forEach { ann ->
                                val name = if (ann.name.isNotBlank()) ann.name else clazz.simpleName.toLowerCase()

                                commands[name] = StoredCommand(name, klass, clazz)
                            }
                        }
                    }
                }
    }

    companion object {
        val commands = mutableMapOf<String, StoredCommand>()
    }
}