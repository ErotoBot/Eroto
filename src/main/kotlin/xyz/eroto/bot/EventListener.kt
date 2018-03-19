package xyz.eroto.bot

import me.aurieh.ares.core.entities.EventWaiter
import me.aurieh.ares.utils.ArgParser
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import xyz.eroto.bot.entities.Command
import xyz.eroto.bot.entities.Context
import xyz.eroto.bot.entities.exceptions.ArgumentRequiredException
import xyz.eroto.bot.entities.exceptions.ArgumentTypeException
import xyz.eroto.bot.entities.exceptions.MemberNotFoundException
import xyz.eroto.bot.extensions.searchMembers
import xyz.eroto.bot.utils.MemberPicker
import java.util.concurrent.CompletableFuture

class EventListener : ListenerAdapter() {
    override fun onGenericEvent(event: Event) = waiter.emit(event)

    override fun onReady(event: ReadyEvent) = println("Ready!")

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.isWebhookMessage || event.author.isBot)
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
        val args = ArgParser.parsePosix(tokenized)

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

        val fut = getTypedArgs(event, cmd, args.unmatched)

        fut.exceptionally { e ->
            when (e) {
                is ArgumentTypeException -> {
                    event.channel.sendMessage("Argument ${e.input} is not of type ${e.type.simpleName!!}!").queue()
                }

                is ArgumentRequiredException -> {
                    event.channel.sendMessage("Argument ${e.name} is required!").queue()
                }

                is MemberNotFoundException -> {
                    event.channel.sendMessage("No members found for ${e.input}!").queue()
                }

                else -> e.printStackTrace()
            }

            return@exceptionally mapOf<String, Any>()
        }

        fut.thenAccept {
            try {
                val ctx = Context(event, args, cmd, it)

                cmd.run(ctx)
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getTypedArgs(event: MessageReceivedEvent, cmd: Command, unmatched: List<String>): CompletableFuture<Map<String, Any>> {
        val fut = CompletableFuture<Map<String, Any>>()
        val args = mutableMapOf<String, Any>()
        var i = 0

        fun next() {
            if (i == cmd.arguments.size) {
                fut.complete(args)
                return
            }

            val arg = cmd.arguments[i]
            val name = arg.name

            if (i == unmatched.size) {
                if (arg.defaultValue != null) {
                    args[name] = arg.defaultValue
                } else if (!arg.optional) {
                    throw ArgumentRequiredException(name)
                }

                return
            }

            var userArg = unmatched[i]

            if (i == cmd.arguments.size - 1) {
                userArg = unmatched.slice(i until unmatched.size).joinToString(" ")
            }

            val typeException = ArgumentTypeException(name, userArg, arg.clazz)

            when (arg.clazz) {
                // standard
                Int::class -> {
                    args[name] = userArg.toIntOrNull()
                            ?: throw typeException
                    i++
                    next()
                }
                Long::class -> {
                    args[name] = userArg.toLongOrNull()
                            ?: throw typeException
                    i++
                    next()
                }
                Float::class -> {
                    args[name] = userArg.toFloatOrNull()
                            ?: throw typeException
                    i++
                    next()
                }
                String::class -> {
                    args[name] = userArg
                    i++
                    next()
                }
                Boolean::class -> {
                    args[name] = userArg.toBoolean() // TODO add converter
                    i++
                    next()
                }

                // standard arrays
                Array<Int>::class -> {
                    args[name] = userArg.split(Regex("\\s?,\\s?")).map {
                        it.toIntOrNull() ?: throw typeException
                    }.toTypedArray()
                    i++
                    next()
                }
                Array<Long>::class -> {
                    args[name] = userArg.split(Regex("\\s?,\\s?")).map {
                        it.toLongOrNull() ?: throw typeException
                    }
                    i++
                    next()
                }
                Array<Float>::class -> {
                    args[name] = userArg.split(Regex("\\s?,\\s?")).map {
                        it.toFloatOrNull() ?: throw typeException
                    }.toTypedArray()

                    i++
                    next()
                }
                Array<String>::class -> {
                    args[name] = userArg.split(Regex("\\s?,\\s?")).toTypedArray()
                    i++
                    next()
                }
                Array<Boolean>::class -> {
                    args[name] = userArg.split(Regex("\\s?,\\s?")).map {
                        it.toBoolean()
                    }.toTypedArray()
                    i++
                    next()
                }

                // TODO add more JDA types
                // jda
                Member::class -> {
                    val mems = event.guild!!.searchMembers(userArg)

                    when {
                        mems.size == 1 -> {
                            args[name] = mems.first()
                            i++
                            next()
                        }
                        mems.size > 1 -> MemberPicker(event.member!!, mems).build(event.message).thenAccept {
                            args[name] = it
                            i++
                            next()
                        }
                        else -> throw MemberNotFoundException(userArg)
                    }
                }
            }
        }

        try {
            next()
        } catch(e: Exception) {
            fut.completeExceptionally(e)
        }

        return fut
    }

    companion object {
        val waiter = EventWaiter()
    }
}