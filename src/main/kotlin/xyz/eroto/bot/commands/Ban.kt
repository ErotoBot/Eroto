package xyz.eroto.bot.commands

import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import xyz.eroto.bot.entities.cmd.*

class Ban : Command() {
    override val description = "Ban a user"
    override val example = "ban @user spam"

    init {
        arguments += argument<Member>("users")
        arguments += argument<String>("reason", optional = true)
        permissions += MemberPermission(Permission.BAN_MEMBERS)
        botPermissions += BotPermission(Permission.BAN_MEMBERS)
    }

    override fun run(ctx: Context) {
        val user = ctx.args["users"] as Member
        val reason = ctx.args["reason"] as String?
        
        if (ctx.member!!.canInteract(user)) {
            ctx.guild!!.controller.ban(user, 7, reason).queue()
        }
    }
}