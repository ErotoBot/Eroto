package xyz.eroto.bot.commands

import me.aurieh.ares.exposed.async.asyncTransaction
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Role
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.update
import xyz.eroto.bot.Eroto
import xyz.eroto.bot.entities.cmd.*
import xyz.eroto.bot.entities.schema.GuildsTable

class SetRole : Subcommand(){
    override val description = "Set the muted role for the guild"
    override val example = "mute setrole @myrole"

    init {
        guildOnly = true

        arguments += argument<Role>("role")
        permissions += MemberPermission(Permission.MANAGE_ROLES)
        botPermissions += BotPermission(Permission.MANAGE_ROLES)
    }

    override fun run(ctx: Context) {
        val role = ctx.args["role"] as Role

        asyncTransaction(Eroto.pool) {
            xyz.eroto.bot.entities.schema.GuildsTable.update({
                GuildsTable.id.eq(ctx.guild!!.idLong)
            }) {
                it[mutedRole] = role.id
            }

            ctx.send(":ok_hand:")
        }.execute().exceptionally {
            it.printStackTrace()
        }
    }
}

class Mute : Command() {
    override val description = "Mute a user or users"
    override val example = "mute @user"

    init {
        guildOnly = true

        subcommands += SetRole()
        arguments += argument<Array<Member>>("users")
        permissions += MemberPermission(Permission.MANAGE_ROLES)
        botPermissions += BotPermission(Permission.MANAGE_ROLES)
    }

    override fun run(ctx: Context) {
        val users = ctx.args["users"] as Array<Member>
        val role = ctx.guild!!.roles.filter { it.id == ctx.storedGuild!!.mutedRole }.firstOrNull()

        if (users.any { !ctx.member!!.canInteract(it) || !ctx.guild!!.selfMember!!.canInteract(it) }) {
            val mems = users.filter {
                !ctx.member!!.canInteract(it) || !ctx.guild!!.selfMember!!.canInteract(it)
            }.joinToString(", ") { "${it.user.name}#${it.user.discriminator}" }

            return ctx.send("I can't mute the following users: $mems")
        }

        for (user in users) {
            ctx.guild!!.controller.addRolesToMember(user, role)
        }

        ctx.send(":ok_hand:")
    }
}