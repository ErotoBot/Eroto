package xyz.eroto.bot

import xyz.eroto.bot.entities.ArgumentTypeException
import xyz.eroto.bot.entities.StoredCommand
import xyz.eroto.bot.entities.Context
import xyz.eroto.bot.entities.MissingArgumentException
import xyz.eroto.bot.utils.ArgParser
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter

class EventListener : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        var content = event.message.contentRaw
        val mentions = listOf(
                "<@${event.jda.selfUser.id}> ",
                "<@!${event.jda.selfUser.id}> "
        )

        val usedPrefix = xyz.eroto.bot.Eroto.config.prefixes.firstOrNull { content.startsWith(it) }
                ?: mentions.firstOrNull { content.startsWith(it) }
                ?: return
        content = content.removePrefix(usedPrefix)
        val splitted = content.split("\\s+".toRegex())
        val commandName = splitted[0]

        if (commandName == "help") {
            val parts = xyz.eroto.bot.CogManager.help()

            event.author.openPrivateChannel().queue { channel ->
                for (part in parts) {
                    channel.sendMessage("```asciidoc\n$part```").queue()
                }
            }
        } else {
            executeCommand(event, commandName, splitted)
        }
    }

    private fun executeCommand(
            event: MessageReceivedEvent,
            commandName: String,
            splitted: List<String>,
            baseCommand: StoredCommand? = null
    ) {
        val arg = splitted.slice(1 until splitted.size).joinToString(" ")
        val args = ArgParser.untypedParseSplit(ArgParser.tokenize(arg))

        val cmd = if (baseCommand != null) {
            baseCommand.subcommands[commandName] ?: return
        } else {
            xyz.eroto.bot.CogManager.commands[commandName] ?: xyz.eroto.bot.CogManager.commands[xyz.eroto.bot.CogManager.aliases[commandName]] ?: return
        }

        if (args.unmatched.isNotEmpty() && args.unmatched[0] in cmd.subcommands) {
            return executeCommand(event, args.unmatched[0], arg.split(" "), cmd)
        }

        try {
            val ctx = Context(event, cmd, args)

            cmd.run(ctx)
        } catch(e: Exception) {
            when (e) {
                is MissingArgumentException -> event.channel.sendMessage("Missing argument: ${e.arg}").queue()
                is ArgumentTypeException -> event.channel.sendMessage("Invalid argument type: ${e.input} is not of type ${e.type}")
                else -> e.printStackTrace()
            }
        }
    }
}