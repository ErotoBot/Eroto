package xyz.eroto.bot.entities

import me.aurieh.ares.utils.ArgParser
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import xyz.eroto.bot.entities.exceptions.ArgumentRequiredException
import xyz.eroto.bot.entities.exceptions.ArgumentTypeException

class Context(val event: MessageReceivedEvent, parsedArgs: ArgParser.ParsedResult, cmd: Command) {
    val jda = event.jda
    val guild = event.guild
    val channel = event.channel
    val member = event.member
    val msg = event.message
    val author = event.author

    val unmatched = parsedArgs.unmatched
    val flags = parsedArgs.argMap
    val args = mutableMapOf<String, Any>()

    init {
        val iter = cmd.arguments.iterator().withIndex()

        while (iter.hasNext()) {
            val item = iter.next()
            val i = item.index
            val arg = item.value
            val name = arg.name

            if (i == unmatched.size) {
                if (arg.defaultValue != null) {
                    args[name] = arg.defaultValue
                } else if (!arg.optional) {
                    throw ArgumentRequiredException(name)
                }

                break
            }

            var userArg = unmatched[i]

            if (i == cmd.arguments.size - 1) {
                userArg = unmatched.slice(i until unmatched.size).joinToString(" ")
            }

            val typeException = ArgumentTypeException(name, userArg, arg.clazz)

            when (arg.clazz) {
                // standard
                Int::class -> args[name] = userArg.toIntOrNull()
                        ?: throw typeException
                Long::class -> args[name] = userArg.toLongOrNull()
                        ?: throw typeException
                Float::class -> args[name] = userArg.toFloatOrNull()
                        ?: throw typeException
                String::class -> args[name] = userArg
                Boolean::class -> args[name] = userArg.toBoolean() // TODO add converter

                // standard arrays
                Array<Int>::class -> args[name] = userArg.split(Regex("\\s?,\\s?")).map {
                    it.toIntOrNull() ?: throw typeException
                }.toTypedArray()
                Array<Long>::class -> args[name] = userArg.split(Regex("\\s?,\\s?")).map {
                    it.toLongOrNull() ?: throw typeException
                }
                Array<Float>::class -> args[name] = userArg.split(Regex("\\s?,\\s?")).map {
                    it.toFloatOrNull() ?: throw typeException
                }
                Array<String>::class -> args[name] = userArg.split(Regex("\\s?,\\s?"))
                Array<Boolean>::class -> args[name] = userArg.split(Regex("\\s?,\\s?")).map { it.toBoolean() }

                // TODO add JDA types
            }
        }
    }

    fun send(content: String)
            = event.channel.sendMessage(content).queue()
    fun send(content: String, success: (Message) -> Unit)
            = event.channel.sendMessage(content).queue(success)
    fun send(content: String, success: (Message) -> Unit, fail: (Throwable) -> Unit)
            = event.channel.sendMessage(content).queue(success, fail)
}