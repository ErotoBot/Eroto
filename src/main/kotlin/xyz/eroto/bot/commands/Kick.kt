package xyz.eroto.bot.commands

import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.exceptions.PermissionException
import xyz.eroto.bot.entities.cmd.*

class Kick : Command() {
    override val description = "Kick a user or users"
    override val example = "kick @user spam"

    init {
        arguments += argument<Array<Member>>("users")
        arguments += argument("reason", optional = true, defaultValue = "No reason given.")
        permissions += MemberPermission(Permission.KICK_MEMBERS)
        botPermissions += BotPermission(Permission.KICK_MEMBERS)
    }

    override fun run(ctx: Context) {
        val users = ctx.args["users"] as Array<Member>
        val reason = ctx.args["reason"] as String?

        val unable = mutableListOf<Member>()

        for (user in users) {
            if (ctx.member!!.canInteract(user)) {
                ctx.guild!!.controller.kick(user, reason).queue()
            } else {
                unable.add(user)
            }
        }

        if (unable.size > 0){
            val userFmt = "Unable to kick:\n" + unable.map { "${it.user.name}#${it.user.discriminator}" }.joinToString("\n")
            ctx.send(userFmt)
        } else {
            ctx.send(":ok_hand:")
        }
    }
}