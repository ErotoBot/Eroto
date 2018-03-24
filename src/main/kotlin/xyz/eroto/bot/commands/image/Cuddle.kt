package xyz.eroto.bot.commands.image

import net.dv8tion.jda.core.entities.Member
import xyz.eroto.bot.entities.cmd.Context
import xyz.eroto.bot.entities.cmd.argument
import xyz.eroto.bot.entities.cmd.variant.WolkCommand
import xyz.eroto.bot.utils.WolkType

class Cuddle : WolkCommand() {
    override val type = WolkType.CUDDLE
    override val description = "cuddle someone"
    override val example = "cuddle @user"

    init {
        arguments += argument<Member>("user")
    }

    override fun title(ctx: Context) : String {
        val target = ctx.args["user"] as Member

        return "${target.nickname ?: target.user.name}, you got a cuddle from ${ctx.member?.nickname ?: ctx.author.name}"
    }
}