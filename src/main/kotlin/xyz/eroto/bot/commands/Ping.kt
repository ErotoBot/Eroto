package xyz.eroto.bot.commands

import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import xyz.eroto.bot.entities.*

class Pang : Subcommand() {
    override val description = "Peng!"
    override val example = "ping pong pang"

    override fun run(ctx: Context) {
        ctx.send("Peng!")
    }
}

class Pong : Subcommand() {
    override val description = "Ping!"
    override val example = "ping pong"

    init {
        subcommands += Pang()
        arguments += argument<Member>("member")
        permissions += MemberPermission(Permission.ADMINISTRATOR)
        botPermissions += BotPermission(Permission.ADMINISTRATOR)
    }

    override fun run(ctx: Context) {
        val mem = ctx.args["member"] as Member

        ctx.send("Ping! ${mem.asMention}")
    }
}

class Ping : Command() {
    override val description = "Pong!"
    override val example = "ping"

    init {
        subcommands += Pong()
    }

    override fun run(ctx: Context) {
        ctx.send("Pong!")
    }
}