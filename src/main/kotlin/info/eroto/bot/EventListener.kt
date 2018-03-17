package info.eroto.bot

import info.eroto.bot.entities.StoredCommand
import info.eroto.bot.entities.Context
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter

class EventListener : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        var content = event.message.contentRaw
        val mentions = listOf(
                "<@${event.jda.selfUser.id}>",
                "<@!${event.jda.selfUser.id}>"
        )

        val usedPrefix = Eroto.config.prefixes.firstOrNull { content.startsWith(it) }
                ?: mentions.firstOrNull { content.startsWith(it) }
                ?: return
        content = content.removePrefix(usedPrefix)
        val splitted = content.split("\\s+".toRegex())
        val commandName = splitted[0]

        executeCommand(event, commandName, splitted)
    }

    private fun executeCommand(
            event: MessageReceivedEvent,
            commandName: String,
            splitted: List<String>,
            baseCommand: StoredCommand? = null
    ) {
        val args = splitted.slice(1 until splitted.size)

        val cmd = if (baseCommand != null) {
            baseCommand.subcommands[commandName] ?: return
        } else {
            CogManager.commands[commandName] ?: return
        }

        if (args.isNotEmpty() && args[0] in cmd.subcommands) {
            return executeCommand(event, args[0], args, cmd)
        }

        val ctx = Context(event, cmd, args)

        try {
            cmd.run(ctx)
        } catch(e: Exception) {
            e.printStackTrace()
        }
    }
}