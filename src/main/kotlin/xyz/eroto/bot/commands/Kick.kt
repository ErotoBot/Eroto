package xyz.eroto.bot.commands

import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.exceptions.PermissionException
import xyz.eroto.bot.entities.cmd.*

class Kick : Command() {
    override val description = "Kick a user"
    override val example = "kick @user spam"

    init {
        arguments += argument<Member>("users")
        arguments += argument<String>("reason", optional = true)
        permissions += MemberPermission(Permission.KICK_MEMBERS)
        botPermissions += BotPermission(Permission.KICK_MEMBERS)
    }

    override fun run(ctx: Context) {
        val user = ctx.args["users"] as Member
        val reason = ctx.args["reason"] as String?

        if (ctx.member!!.canInteract(user)) {
            ctx.guild!!.controller.kick(user, reason).queue({
                ctx.send(":ok_hand:")
            }) { e ->
                when (e) {
                    is PermissionException -> {
                        ctx.send("Missing permission: ${e.permission.getName()}")
                    }

                    else -> e.printStackTrace()
                }
            }
        }
    }
}