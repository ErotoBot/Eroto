package xyz.eroto.bot

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.MessageEmbed
import org.reflections.Reflections
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import xyz.eroto.bot.entities.cmd.Command
import xyz.eroto.bot.entities.cmd.Subcommand
import xyz.eroto.bot.entities.exceptions.CommandNotFoundException
import kotlin.reflect.full.isSubclassOf

class CommandManager {
    init {
        Reflections(ConfigurationBuilder().addUrls(ClasspathHelper.forPackage("xyz.eroto.bot.commands")))
                .getSubTypesOf(Command::class.java)
                .forEach {
                    if (!it.isInterface && !it.kotlin.isSubclassOf(Subcommand::class) && !it.kotlin.isAbstract) {
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

        fun help(command: String, baseCommand: Command? = null): MessageEmbed {
            if (command !in commands)
                throw CommandNotFoundException(command)

            val cmd = if (baseCommand != null) {
                baseCommand.subcommands.firstOrNull {
                    (it.name ?: it::class.simpleName!!).toLowerCase() == command.toLowerCase()
                }
            } else {
                commands[command]
            } ?: throw CommandNotFoundException(command)

            return EmbedBuilder().apply {
                setTitle((cmd.name ?: cmd::class.simpleName!!.toLowerCase()).capitalize())
                val args = cmd.arguments.map {
                    println(it)
                    if (it.optional || it.defaultValue != null) {
                        "[${it.displayName ?: it.name}]"
                    } else {
                        "<${it.displayName ?: it.name}>"
                    }
                }

                descriptionBuilder.appendln("`${(cmd.name ?: cmd::class.simpleName!!)} $args`")

                descriptionBuilder.appendln(cmd.description)
            }.build()
        }
    }
}