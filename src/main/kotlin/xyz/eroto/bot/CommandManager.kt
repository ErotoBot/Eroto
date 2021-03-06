package xyz.eroto.bot

import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import xyz.eroto.bot.entities.cmd.Command
import xyz.eroto.bot.entities.cmd.Subcommand
import kotlin.reflect.full.isSubclassOf

class CommandManager {
    init {
        Reflections(ConfigurationBuilder().addUrls(ClasspathHelper.forPackage("xyz.eroto.bot.commands")))
                .getSubTypesOf(Command::class.java)
                .forEach {
                    if (!it.isInterface && !it.kotlin.isSubclassOf(Subcommand::class) && !it.kotlin.isAbstract) {
                        println(it.name)
                        val cmd = it.newInstance() as Command
                        val name = cmd.name?.toLowerCase() ?: it.simpleName.toLowerCase()

                        commands[name] = cmd
                    }
                }
    }

    companion object {
        val commands = mutableMapOf<String, Command>()
        val disabled = mutableListOf<String>()

        fun disableCommand(name: String) {
            if (name in commands && name !in disabled)
                disabled += name
        }

        fun enableCommand(name: String) {
            if (name in disabled)
                disabled -= name
        }
    }
}