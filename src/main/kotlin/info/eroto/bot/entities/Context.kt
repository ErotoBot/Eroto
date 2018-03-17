package info.eroto.bot.entities

import info.eroto.bot.utils.ArgParser
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class Context(val event: MessageReceivedEvent, val cmd: StoredCommand, val args: ArgParser.ParsedResult) {
    private val msg = event.message
    private val channel = event.channel
    private val guild = event.guild
    private val author = event.author
    private val member = event.member
    private val jda = event.jda

    fun send(vararg args: String) {
        event.channel.sendMessage(args.joinToString(" ")).queue()
    }
}