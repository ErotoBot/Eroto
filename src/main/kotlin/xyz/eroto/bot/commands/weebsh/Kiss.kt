package xyz.eroto.bot.commands.weebsh

import net.dv8tion.jda.core.entities.Member
import xyz.eroto.bot.entities.cmd.Context
import xyz.eroto.bot.entities.cmd.argument
import xyz.eroto.bot.entities.cmd.variant.WolkCommand
import xyz.eroto.bot.utils.WolkType

class Kiss : WolkCommand() {
    override val type = WolkType.KISS
    override val description = "kiss someone :heart:"
    override val example = "kiss @user"

    init {
        arguments += argument<Member>("user")
    }

    override fun title(ctx: Context) : String {
        val target = ctx.args["user"] as Member

        return "${target.user.name}, you got a hug from ${ctx.author.name}"
    }
}