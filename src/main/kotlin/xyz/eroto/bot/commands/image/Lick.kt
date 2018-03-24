package xyz.eroto.bot.commands.image

import net.dv8tion.jda.core.entities.Member
import xyz.eroto.bot.entities.cmd.Context
import xyz.eroto.bot.entities.cmd.argument
import xyz.eroto.bot.entities.cmd.variant.WolkCommand
import xyz.eroto.bot.utils.WolkType

class Lick : WolkCommand() {
    override val type = WolkType.LICK
    override val description = "Lick someone, tasty~"
    override val example = "lick @user"

    init {
        arguments += argument<Member>("user")
    }

    override fun title(ctx: Context) : String {
        val target = ctx.args["user"] as Member

        return "${target.nickname ?: target.user.name}, you got a lick from ${ctx.member?.nickname ?: ctx.author.name}"
    }
}