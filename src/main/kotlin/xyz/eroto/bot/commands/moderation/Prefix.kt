package xyz.eroto.bot.commands.moderation

import me.aurieh.ares.exposed.async.asyncTransaction
import net.dv8tion.jda.core.Permission
import org.jetbrains.exposed.sql.update
import xyz.eroto.bot.Eroto
import xyz.eroto.bot.entities.cmd.*
import xyz.eroto.bot.entities.schema.GuildsTable

class RemovePrefixes : Subcommand("remove") {
    override val description = "Remove server prefixes"
    override val example = "prefix remove a! b! \"c \""

    init {
        guildOnly = true

        permissions += MemberPermission(Permission.MANAGE_SERVER)
        arguments += argument<Array<String>>("prefixes", delimiter = " ")
    }

    override fun run(ctx: Context) {
        var prefixes = ctx.args["prefixes"] as Array<String>

        if (prefixes[0] == "all")
            prefixes = ctx.storedGuild!!.prefixes.toTypedArray()

        asyncTransaction(Eroto.pool) {
            GuildsTable.update({
                GuildsTable.id.eq(ctx.guild!!.idLong)
            }) {
                it[GuildsTable.prefixes] = ctx.storedGuild!!.prefixes.minus(prefixes).toTypedArray()
            }

            ctx.send(":ok_hand:")
        }.execute().exceptionally {
            it.printStackTrace()
        }
    }
}

class AddPrefixes : Subcommand("add") {
    override val description = "Add server prefixes"
    override val example = "prefix add a! b! \"c \""

    init {
        guildOnly = true

        permissions += MemberPermission(Permission.MANAGE_SERVER)
        arguments += argument<Array<String>>("prefixes", delimiter = " ")
    }

    override fun run(ctx: Context) {
        val prefixes = ctx.args["prefixes"] as Array<String>

        asyncTransaction(Eroto.pool) {
            GuildsTable.update({
                GuildsTable.id.eq(ctx.guild!!.idLong)
            }) {
                it[GuildsTable.prefixes] = ctx.storedGuild!!.prefixes.plus(prefixes).toTypedArray()
            }

            ctx.send(":ok_hand:")
        }.execute().exceptionally {
            it.printStackTrace()
        }
    }
}

class Prefix : Command() {
    override val description = "Change, view or remove prefixes!"
    override val example = "prefix"

    init {
        guildOnly = true

        subcommands += AddPrefixes()
        subcommands += RemovePrefixes()
    }

    override fun run(ctx: Context) {
        val p = ctx.storedGuild!!.prefixes
        val prefixes = if (p.isNotEmpty()) "`${p.joinToString("`, `")}`" else "none"

        ctx.send("Current prefixes: $prefixes")
    }
}