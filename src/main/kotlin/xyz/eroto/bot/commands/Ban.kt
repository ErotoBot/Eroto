package xyz.eroto.bot.commands

import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import xyz.eroto.bot.entities.cmd.*

class Ban : Command() {
    override val description = "Ban a user or users"
    override val example = "ban @user spam"

    init {
        arguments += argument<Array<Member>>("users")
        arguments += argument("reason", optional = true, defaultValue = "No reason given.")
        permissions += MemberPermission(Permission.BAN_MEMBERS)
        botPermissions += BotPermission(Permission.BAN_MEMBERS)
    }

    override fun run(ctx: Context) {
        val users = ctx.args["users"] as Array<Member>
        val reason = ctx.args["reason"] as String?

        val unable = mutableListOf<Member>()

        for (user in users) {
            if (ctx.member!!.canInteract(user)) {
                ctx.guild!!.controller.ban(user, 7, reason).queue()
            } else {
                unable.add(user)
            }
        }

        if (unable.size > 0) {
            val userFmt = "Unable to ban:\n" + unable.map { "${it.user.name}#${it.user.discriminator}" }.joinToString("\n")
            ctx.send(userFmt)
        } else {
            ctx.send(":ok_hand:")
        }

    }
}