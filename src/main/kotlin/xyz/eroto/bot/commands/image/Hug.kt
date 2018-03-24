package xyz.eroto.bot.commands.image

import net.dv8tion.jda.core.entities.Member
import xyz.eroto.bot.entities.cmd.Context
import xyz.eroto.bot.entities.cmd.argument
import xyz.eroto.bot.entities.cmd.variant.WolkCommand
import xyz.eroto.bot.utils.WolkType

class Hug : WolkCommand() {
    override val type = WolkType.HUG
    override val description = "Hug someone"
    override val example = "hug @user"

    init {
        arguments += argument<Member>("user")
    }

    override fun title(ctx: Context) : String {
        val target = ctx.args["user"] as Member

        return "${target.nickname ?: target.user.name}, you got a hug from ${ctx.member?.nickname ?: ctx.author.name}"
    }
}