package xyz.eroto.bot.entities

import me.aurieh.ares.utils.ArgParser
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class Context(val event: MessageReceivedEvent, parsedArgs: ArgParser.ParsedResult, cmd: Command, val args: Map<String, Any>) {
    val jda = event.jda
    val guild = event.guild
    val channel = event.channel
    val member = event.member
    val msg = event.message
    val author = event.author

    val unmatched = parsedArgs.unmatched
    val flags = parsedArgs.argMap

    fun send(content: String)
            = event.channel.sendMessage(content).queue()
    fun send(content: String, success: (Message) -> Unit)
            = event.channel.sendMessage(content).queue(success)
    fun send(content: String, success: (Message) -> Unit, fail: (Throwable) -> Unit)
            = event.channel.sendMessage(content).queue(success, fail)
}