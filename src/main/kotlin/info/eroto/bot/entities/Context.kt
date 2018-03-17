package info.eroto.bot.entities

import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class Context(val event: MessageReceivedEvent, val cmd: CommandClass, val args: List<String>) {
    fun send(vararg args: String) {
        event.channel.sendMessage(args.joinToString(" ")).queue()
    }
}