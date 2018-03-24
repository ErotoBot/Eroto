package xyz.eroto.bot.commands.moderation

import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import xyz.eroto.bot.entities.cmd.*

class Ban : Command() {
    override val description = "Ban a user or users"
    override val example = "ban @user spam"

    init {
        guildOnly = true

        arguments += argument<Array<Member>>("users")
        arguments += argument<String>("reason", optional = true, defaultValue = "No reason given.")
        permissions += MemberPermission(Permission.BAN_MEMBERS)
        botPermissions += BotPermission(Permission.BAN_MEMBERS)
    }

    override fun run(ctx: Context) {
        val users = ctx.args["users"] as Array<Member>
        val reason = ctx.args["reason"] as String?

        if (users.any { !ctx.member!!.canInteract(it) || !ctx.guild!!.selfMember!!.canInteract(it) }) {
            val mems = users.filter {
                !ctx.member!!.canInteract(it) || !ctx.guild!!.selfMember!!.canInteract(it)
            }.joinToString(", ") { "${it.user.name}#${it.user.discriminator}" }

            return ctx.send("I can't ban the following users: $mems")
        }

        for (user in users) {
            ctx.guild!!.controller.ban(user, 7, "${ctx.author.name}#${ctx.author.discriminator} (${ctx.author.id}): $reason").queue()
        }

        ctx.send(":ok_hand:")
    }
}
