package xyz.eroto.bot

import me.aurieh.ares.core.entities.EventWaiter
import me.aurieh.ares.utils.ArgParser
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import xyz.eroto.bot.entities.Command
import xyz.eroto.bot.entities.Context

class EventListener : ListenerAdapter() {
    override fun onGenericEvent(event: Event) = waiter.emit(event)

    override fun onReady(event: ReadyEvent) = println("Ready!")

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.isWebhookMessage)
            return

        var content = event.message.contentRaw
        val mentions = listOf("<@${event.jda.selfUser.id}> ", "<@!${event.jda.selfUser.id}> ")
        val usedPrefix = Eroto.config.prefixes.firstOrNull { content.toLowerCase().startsWith(it.toLowerCase()) }
                ?: mentions.firstOrNull { content.startsWith(it) }
                ?: return

        content = content.removePrefix(usedPrefix)
        val splitted = content.split(Regex("\\s+"))

        executeCommand(event, splitted[0], splitted.slice(1 until splitted.size))
    }

    private fun executeCommand(
            event: MessageReceivedEvent,
            commandName: String,
            splitted: List<String>,
            baseCommand: Command? = null
    ) {
        val tokenized = ArgParser.tokenize(splitted.joinToString(" "))
        val args = ArgParser.untypedParseSplit(tokenized)

        val cmd = if (baseCommand != null) {
            baseCommand.subcommands.firstOrNull { (it.name ?: it::class.simpleName!!).toLowerCase() == commandName.toLowerCase() }
                    ?: return
        } else {
            CommandManager.commands[commandName]
                    ?: CommandManager.commands.values.firstOrNull { commandName in it.aliases }
                    ?: return
        }

        if (args.unmatched.isNotEmpty() && cmd.subcommands.any { (it.name ?: it::class.simpleName!!).toLowerCase() == args.unmatched[0].toLowerCase() }) {

            return executeCommand(event, args.unmatched[0], splitted.slice(1 until splitted.size), cmd)
        }

        try {
            val ctx = Context(event, args)

            cmd.run(ctx)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        val waiter = EventWaiter()
    }
}