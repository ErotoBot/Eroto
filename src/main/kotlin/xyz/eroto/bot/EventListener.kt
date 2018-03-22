package xyz.eroto.bot

import me.aurieh.ares.core.entities.EventWaiter
import me.aurieh.ares.exposed.async.asyncTransaction
import me.aurieh.ares.utils.ArgParser
import me.aurieh.ares.utils.UnclosedQuoteError
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.events.Event
import net.dv8tion.jda.core.events.ReadyEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import xyz.eroto.bot.entities.cmd.Command
import xyz.eroto.bot.entities.cmd.Context
import xyz.eroto.bot.entities.db.StoredGuild
import xyz.eroto.bot.entities.exceptions.*
import xyz.eroto.bot.entities.schema.GuildsTable
import xyz.eroto.bot.extensions.searchMembers
import xyz.eroto.bot.extensions.searchRole
import xyz.eroto.bot.utils.MemberPicker
import xyz.eroto.bot.utils.RolePicker
import java.util.concurrent.CompletableFuture

class EventListener : ListenerAdapter() {
    override fun onGenericEvent(event: Event) = waiter.emit(event)

    override fun onReady(event: ReadyEvent) = println("Ready!")

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.isWebhookMessage || event.author.isBot)
            return

        val content = event.message.contentRaw
        val mentions = listOf("<@${event.jda.selfUser.id}> ", "<@!${event.jda.selfUser.id}> ")

        if (event.guild != null) {
            asyncTransaction(Eroto.pool) {
                val guild = GuildsTable.select { GuildsTable.id.eq(event.guild.idLong) }.firstOrNull()

                val stored = if (guild == null) {
                    GuildsTable.insert {
                        it[id] = event.guild.idLong
                        it[prefixes] = arrayOf()
                        it[mutedRole] = null
                    }

                    StoredGuild(event.guild.idLong, listOf(), null)
                } else {
                    StoredGuild(
                            event.guild.idLong,
                            guild[GuildsTable.prefixes].toList(),
                            guild[GuildsTable.mutedRole]
                    )
                }

                val usedPrefix = Eroto.config.prefixes.firstOrNull { content.toLowerCase().startsWith(it.toLowerCase()) }
                        ?: mentions.firstOrNull { content.startsWith(it) }
                        ?: stored.prefixes.firstOrNull { content.toLowerCase().startsWith(it.toLowerCase()) }
                        ?: return@asyncTransaction

                val prefixLess = content.removePrefix(usedPrefix)
                val cmd = prefixLess.split(" ")[0]

                executeCommand(event, cmd, prefixLess.removePrefix(cmd).trim(), stored)
            }.execute().exceptionally {
                it.printStackTrace()
            }
        } else {
            val usedPrefix = Eroto.config.prefixes.firstOrNull { content.toLowerCase().startsWith(it.toLowerCase()) }
                    ?: mentions.firstOrNull { content.startsWith(it) }
                    ?: return

            val prefixLess = content.removePrefix(usedPrefix)
            val cmd = prefixLess.split(" ")[0]

            executeCommand(event, cmd, prefixLess.removePrefix(cmd).trim())
        }
    }

    private fun executeCommand(
            event: MessageReceivedEvent,
            commandName: String,
            content: String,
            storedGuild: StoredGuild? = null,
            baseCommand: Command? = null
    ) {
        try {
            val cmd = if (baseCommand != null) {
                baseCommand.subcommands.firstOrNull {
                    (it.name ?: it::class.simpleName!!).toLowerCase() == commandName.toLowerCase()
                }
                        ?: return
            } else {
                CommandManager.commands[commandName]
                        ?: CommandManager.commands.values.firstOrNull { commandName in it.aliases }
                        ?: return
            }

            val tokenized = ArgParser.tokenize(content)
            val args = ArgParser.parsePosix(tokenized)

            if (args.unmatched.isNotEmpty() && cmd.subcommands.any {
                        (it.name ?: it::class.simpleName!!).toLowerCase() == args.unmatched[0].toLowerCase()
                    }) {

                return executeCommand(event, args.unmatched[0], content.removePrefix("$commandName "), storedGuild, cmd)
            }

            if (cmd.guildOnly && event.guild == null) {
                return event.channel.sendMessage("This command can only be used in a server!").queue()
            }

            if ("h" in args.argMap || "help" in args.argMap) {
                return event.channel.sendMessage(CommandManager.help(commandName, baseCommand)).queue()
            }

            try {
                checkPermissions(event, cmd)
                checkBotPermissions(event, cmd)
            } catch (e: Exception) {
                return when (e) {
                    is MemberMissingPermissionException -> {
                        event.channel.sendMessage("Missing permission: ${e.perm.getName()}").queue()
                    }

                    is BotMissingPermissionException -> {
                        event.channel.sendMessage("Bot missing permission: ${e.perm.getName()}").queue()
                    }

                    else -> e.printStackTrace()
                }
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

                    is RoleNotFoundException -> {
                        event.channel.sendMessage("No roles found for ${e.input}!").queue()
                    }

                    is MemberMissingPermissionException -> {
                        event.channel.sendMessage("Missing permission: ${e.perm.getName()}").queue()
                    }

                    is BotMissingPermissionException -> {
                        event.channel.sendMessage("Bot missing permission: ${e.perm.getName()}").queue()
                    }

                    else -> e.printStackTrace()
                }

                null
            }

            fut.thenAccept { typedArgs ->
                try {
                    val ctx = Context(event, args, cmd, typedArgs, storedGuild)

                    cmd.run(ctx)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: UnclosedQuoteError) {
            event.channel.sendMessage("Unclosed quote found!").queue()
        }
    }

    private fun checkPermissions(event: MessageReceivedEvent, cmd: Command) {
        val missingPerm = cmd.permissions.firstOrNull {
            !event.member.hasPermission(it.perm) && !event.member.hasPermission(Permission.ADMINISTRATOR)
        } ?: return

        throw MemberMissingPermissionException(missingPerm.perm)
    }

    private fun checkBotPermissions(event: MessageReceivedEvent, cmd: Command) {
        val missingPerm = cmd.botPermissions.firstOrNull {
            !event.guild.selfMember.hasPermission(it.perm) && !event.guild.selfMember.hasPermission(Permission.ADMINISTRATOR)
        } ?: return

        throw BotMissingPermissionException(missingPerm.perm)
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
                    fut.complete(args)
                } else if (!arg.optional) {
                    throw ArgumentRequiredException(name)
                } else {
                    fut.complete(args)
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
                    args[name] = userArg.split(Regex("\\s?${arg.delimiter}\\s?")).map {
                        it.toIntOrNull() ?: throw typeException
                    }.toTypedArray()
                    i++
                    next()
                }
                Array<Long>::class -> {
                    args[name] = userArg.split(Regex("\\s?${arg.delimiter}\\s?")).map {
                        it.toLongOrNull() ?: throw typeException
                    }
                    i++
                    next()
                }
                Array<Float>::class -> {
                    args[name] = userArg.split(Regex("\\s?${arg.delimiter}\\s?")).map {
                        it.toFloatOrNull() ?: throw typeException
                    }.toTypedArray()

                    i++
                    next()
                }
                Array<String>::class -> {
                    args[name] = userArg.split(Regex("\\s?${arg.delimiter}\\s?")).toTypedArray()
                    i++
                    next()
                }
                Array<Boolean>::class -> {
                    args[name] = userArg.split(Regex("\\s?${arg.delimiter}\\s?")).map {
                        it.toBoolean()
                    }.toTypedArray()
                    i++
                    next()
                }

                // TODO add more JDA variant
                // jda
                Member::class -> {
                    val mems = event.guild!!.searchMembers(userArg)

                    when {
                        mems.size == 1 -> {
                            args[name] = mems.first()
                            i++
                            next()
                        }

                        mems.size > 1 -> {
                            val futt = MemberPicker(event.member!!, mems).build(event.message)

                            futt.exceptionally {
                                fut.completeExceptionally(it)
                                null
                            }

                            futt.thenAccept {
                                args[name] = it
                                i++
                                next()
                            }
                        }

                        else -> throw MemberNotFoundException(userArg)
                    }
                }

                Role::class -> {
                    val roles = event.guild!!.searchRole(userArg)
                    when {
                        roles.size == 1 -> {
                            args[name] = roles.first()
                            i++
                            next()
                        }

                        roles.size > 1 -> {
                            val futt = RolePicker(event.member!!, roles).build(event.message)

                            futt.exceptionally {
                                fut.completeExceptionally(it)
                                null
                            }

                            futt.thenAccept {
                                args[name] = it
                                i++
                                next()
                            }
                        }

                        else -> throw RoleNotFoundException(userArg)
                    }
                }

                // jda Arrays
                Array<Member>::class -> {
                    val members = mutableListOf<Member>()
                    val memArgs = userArg.split(Regex("\\s?${arg.delimiter}\\s?"))
                    val futt = CompletableFuture<List<Member>>()
                    var ii = 0

                    fun nextMem() {
                        if (ii == memArgs.size) {
                            futt.complete(members)
                            return
                        }

                        val memArg = memArgs[ii]
                        val mems = event.guild.searchMembers(memArg)

                        when {
                            mems.size > 1 -> {
                                MemberPicker(event.member, mems).build(event.message).thenAccept {
                                    members += it
                                    ii++
                                    nextMem()
                                }
                            }

                            mems.size == 1 -> {
                                members += mems[0]
                                ii++
                                nextMem()
                            }

                            else -> throw MemberNotFoundException(memArg)
                        }
                    }

                    nextMem()

                    futt.thenAccept {
                        args[name] = members.toTypedArray()
                        i++
                        next()
                    }

                    futt.exceptionally {
                        fut.completeExceptionally(it)

                        null
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