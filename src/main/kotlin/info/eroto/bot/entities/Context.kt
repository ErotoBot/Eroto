package info.eroto.bot.entities

import info.eroto.bot.utils.ArgParser
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class Context(val event: MessageReceivedEvent, val cmd: StoredCommand, args: ArgParser.ParsedResult) {
    val msg = event.message
    val channel = event.channel
    val guild = event.guild
    val author = event.author
    val member = event.member
    val jda = event.jda
    val unmatchedArgs = args.unmatched
    val flags = args.argMap
    val args = mutableMapOf<String, Any>()

    init {
        val iter = cmd.arguments.iterator().withIndex()

        while (iter.hasNext()) {
            val next = iter.next()
            val index = next.index
            val arg = next.value

            if (index >= unmatchedArgs.size) {
                if (arg.default.isBlank()) {
                    throw MissingArgumentException(arg.name)
                } else {
                    break
                }
            }

            var userArg = unmatchedArgs[next.index]

            if (!iter.hasNext()) {
                userArg = unmatchedArgs.slice(index until unmatchedArgs.size).joinToString(" ")
            }

            this.args[arg.name] = when (arg.type) {
                // standard types
                Int::class -> userArg.toIntOrNull() ?: throw ArgumentTypeException(userArg, "number")
                Long::class -> userArg.toLongOrNull() ?: throw ArgumentTypeException(userArg, "number")
                Float::class -> userArg.toFloatOrNull() ?: throw ArgumentTypeException(userArg, "number")
                String::class -> userArg
                Boolean::class -> Converter.boolean(userArg)

                // standard array types
                Array<Int>::class -> userArg.split(Regex("\\s?,\\s?")).map { it.toIntOrNull() ?: throw ArgumentTypeException(userArg, "number") }
                Array<Long>::class -> userArg.split(Regex("\\s?,\\s?")).map { it.toLongOrNull() ?: throw ArgumentTypeException(userArg, "number") }
                Array<Float>::class -> userArg.split(Regex("\\s?,\\s?")).map { it.toFloatOrNull() ?: throw ArgumentTypeException(userArg, "number") }
                Array<String>::class -> userArg.split(Regex("\\s?,\\s?"))
                Array<Boolean>::class -> userArg.split(Regex("\\s?,\\s?")).map { Converter.boolean(it) }

                else -> userArg
            }
        }
    }

    fun send(message: String, success: Message.() -> Unit = {}) {
        event.channel.sendMessage(message).queue(success) {
            it.printStackTrace() // TODO send error message
        }
    }
}