package xyz.eroto.bot.commands

import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import xyz.eroto.bot.entities.*

class Kick : Command() {
    override val description = "Kick a user"
    override val example = "kick @user @otherUser spam"

    init {
        arguments += argument<Array<Member>>("users")
        arguments += argument("reason", defaultValue = "No reason given")
        permissions += MemberPermission(Permission.KICK_MEMBERS)
        botPermissions += BotPermission(Permission.KICK_MEMBERS)
    }

    override fun run(ctx: Context) {
        val users = ctx.args["users"] as Array<Member>
        val reason = ctx.args["reason"] as String
        for (user in users) {
            if (ctx.member.canInteract(user)) {
                ctx.guild.controller.kick(user, reason).queue()
            }
        }
    }
}