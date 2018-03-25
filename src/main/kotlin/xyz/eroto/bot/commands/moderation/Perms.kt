package xyz.eroto.bot.commands.moderation

import me.aurieh.ares.exposed.async.asyncTransaction
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import org.jetbrains.exposed.sql.update
import xyz.eroto.bot.Eroto
import xyz.eroto.bot.entities.cmd.*
import xyz.eroto.bot.entities.schema.GuildsTable
import xyz.eroto.bot.extensions.searchTextChannel

class AllowPerm : Subcommand("allow") {
    override val description = "Allow permission to a command/feature"
    override val example = "perms allow @user ping pong --channel=#general"

    init {
        guildOnly = true

        permissions += MemberPermission(Permission.MANAGE_SERVER)
        arguments += argument<Member>("user")
        arguments += argument<String>("perm", "command/feature")
    }

    override fun run(ctx: Context) {
        val user = ctx.args["user"] as Member
        val perm = (ctx.args["perm"] as String).removePrefix("eroto").trim()

        if (perm.isBlank()) {
            return ctx.send("Command/feature is blank!")
        }

        var c = "*"

        if ("channel" in ctx.flags || "c" in ctx.flags) {
            val term = ctx.flags["channels"] ?: ctx.flags["c"]

            if (term != null) {
                val channels = ctx.guild!!.searchTextChannel(term)

                c = channels.getOrNull(0)?.id ?: "*"
            }
        }

        val toRemove = mutableListOf<String>()

        for (prm in ctx.storedGuild!!.perms) {
            val parts = prm.split(".")

            val first = parts[0]
            val sec = parts[1]
            val thrd = parts[2]

            val c1 = first.removePrefix("c")
            val u1 = sec.removePrefix("u")

            if (c1 != c && c != "*" || u1 != user.user.id) {
                continue
            }

            val splitted = thrd.split(">")
            val p = perm.replace(" ", ">")

            if (thrd != p && p != "*" && !p.startsWith(splitted[0] + ">*")) {
                continue
            }

            toRemove += prm
        }

        asyncTransaction(Eroto.pool) {
            GuildsTable.update({
                GuildsTable.id.eq(ctx.guild!!.idLong)
            }) {
                it[GuildsTable.perms] = ctx.storedGuild.perms.minus(toRemove).toTypedArray()
            }

            ctx.send("Allowed usage of `$perm` for ${user.user.name}#${user.user.discriminator}!")
        }.execute().exceptionally { ctx.sendException(it) }
    }
}

class BlockPerm : Subcommand("block") {
    override val description = "Allow permission to a command/feature"
    override val example = "perms block @user ping pong --channel=#general"

    init {
        guildOnly = true

        permissions += MemberPermission(Permission.MANAGE_SERVER)
        arguments += argument<Member>("user")
        arguments += argument<String>("perm", "command/feature")
    }

    override fun run(ctx: Context) {
        val user = ctx.args["user"] as Member
        val perm = (ctx.args["perm"] as String).removePrefix("eroto").trim()

        if (perm.isBlank()) {
            return ctx.send("Command/feature is blank!")
        }

        var c = "*"

        if ("channel" in ctx.flags || "c" in ctx.flags) {
            val term = ctx.flags["channel"] ?: ctx.flags["c"]

            if (term != null) {
                val channels = ctx.guild!!.searchTextChannel(term)

                c = channels.getOrNull(0)?.id ?: "*"
            }
        }

        val format = perm.replace(" ", ">")

        asyncTransaction(Eroto.pool) {
            GuildsTable.update({
                GuildsTable.id.eq(ctx.guild!!.idLong)
            }) {
                it[GuildsTable.perms] = ctx.storedGuild!!.perms.plus("c$c.u${user.user.id}.$format").toTypedArray()
            }

            ctx.send("Blocked usage of `$perm` for ${user.user.name}#${user.user.discriminator}!")
        }.execute().exceptionally { ctx.sendException(it) }
    }
}

class Perms : Command() {
    override val description = "Change permissions for users"
    override val example = "perms @user"

    init {
        guildOnly = true

        permissions += MemberPermission(Permission.MANAGE_SERVER)
        arguments += argument<Member>("user", optional = true)
        subcommands += BlockPerm()
        subcommands += AllowPerm()
    }

    override fun run(ctx: Context) {
        val mem = ctx.args["user"] as Member?

        var c = "*"

        if ("channel" in ctx.flags || "c" in ctx.flags) {
            val term = ctx.flags["channels"] ?: ctx.flags["c"]

            if (term != null) {
                val channels = ctx.guild!!.searchTextChannel(term)

                c = channels.getOrNull(0)?.id ?: "*"
            }
        }

        if (mem != null) {
            val perms = ctx.storedGuild!!.perms.filter { perm ->
                val parts = perm.split(".")

                val first = parts[0]
                val sec = parts[1]

                val c1 = first.removePrefix("c")
                val u1 = sec.removePrefix("u")

                (u1 == "*" || u1 == mem.user.id) && (c == "*" || c1 == c || c1 == "*")
            }

            val embed = EmbedBuilder().apply {
                setTitle("Blocked permissions for ${mem.user.name}")

                for (perm in perms) {
                    val parts = perm.split(".")

                    val c1 = parts[0].removePrefix("c")
                    var channel = "all"

                    if (c1 != "*") {
                        channel = ctx.guild!!.getTextChannelById(c1)?.asMention ?: "#invalid-channel"
                    }
                    val cmd = parts[2].replace(">", " ")

                    addField(
                            cmd,
                            "**Channel:** $channel",
                            true
                    )
                }
            }

            ctx.send(embed.build())
        } else {

        }
    }
}