package xyz.eroto.bot.entities.cmd

import me.aurieh.ares.utils.ArgParser
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.JDA
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import xyz.eroto.bot.entities.db.StoredGuild
import kotlin.math.min

class Context(
        val event: MessageReceivedEvent,
        parsedArgs: ArgParser.ParsedResult,
        val cmd: Command,
        val args: Map<String, Any>,
        val storedGuild: StoredGuild?
) {
    val jda: JDA = event.jda
    val guild: Guild? = event.guild
    val channel = event.channel
    val member: Member? = event.member
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

    fun send(content: MessageEmbed)
            = event.channel.sendMessage(content).queue()
    fun send(content: MessageEmbed, success: (Message) -> Unit)
            = event.channel.sendMessage(content).queue(success)
    fun send(content: MessageEmbed, success: (Message) -> Unit, fail: (Throwable) -> Unit)
            = event.channel.sendMessage(content).queue(success, fail)

    fun send(content: String, embed: MessageEmbed)
            = event.channel.sendMessage(content).embed(embed).queue()
    fun send(content: String, embed: MessageEmbed, success: (Message) -> Unit)
            = event.channel.sendMessage(content).embed(embed).queue(success)
    fun send(content: String, embed: MessageEmbed, success: (Message) -> Unit, fail: (Throwable) -> Unit)
            = event.channel.sendMessage(content).embed(embed).queue(success, fail)

    fun sendException(e: Throwable) {
        val mess = e.message

        val list = e.stackTrace.map {
            "\t @ ${it.className}#${it.methodName} (${it.fileName}:${it.lineNumber})"
        }

        val formatted = list.slice(0 until min(list.size, 3)).joinToString("\n")

        event.channel.sendMessage(
                "Oh no! An error occured!\n\n```asciidoc\n*$e*\n$formatted```\n\nPlease report this in my support server!"
        ).queue()
    }
}