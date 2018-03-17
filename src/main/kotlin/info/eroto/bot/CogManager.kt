package info.eroto.bot

import info.eroto.bot.annotations.Command
import info.eroto.bot.entities.Cog
import info.eroto.bot.entities.CommandClass
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

                        klass::class.java.methods.forEach { method ->
                            method.annotations.filterIsInstance<Command>().forEach { ann ->
                                val name = if (ann.name.isNotBlank()) ann.name else method.name

                                commands[name] = CommandClass(name, klass, method)
                            }
                        }
                    }
                }
    }

    companion object {
        val commands = mutableMapOf<String, CommandClass>()
    }
}