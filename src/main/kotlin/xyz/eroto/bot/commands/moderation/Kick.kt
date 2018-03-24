package xyz.eroto.bot.commands.moderation

import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import xyz.eroto.bot.entities.cmd.*

class Kick : Command() {
    override val description = "Kick a user or users"
    override val example = "kick @user spam"

    init {
        arguments += argument<Array<Member>>("users")
        arguments += argument<String>("reason", optional = true)
        permissions += MemberPermission(Permission.KICK_MEMBERS)
        botPermissions += BotPermission(Permission.KICK_MEMBERS)
    }

    override fun run(ctx: Context) {
        val users = ctx.args["users"] as Array<Member>
        val reason = ctx.args["reason"] as String?

        if (users.any { !ctx.member!!.canInteract(it) || !ctx.guild!!.selfMember!!.canInteract(it) }) {
            val mems = users.filter {
                !ctx.member!!.canInteract(it) || !ctx.guild!!.selfMember!!.canInteract(it)
            }.joinToString(", ") { "${it.user.name}#${it.user.discriminator}" }

            return ctx.send("I can't kick the following users: $mems")
        }

        for (user in users) {
            ctx.guild!!.controller.kick(user, reason)
        }

        ctx.send(":ok_hand:")
    }
}